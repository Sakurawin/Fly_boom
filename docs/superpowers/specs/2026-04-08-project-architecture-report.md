# AircraftWar Android 项目深度架构报告（面向新开发者）

本文面向新加入开发者，目标是：
- 快速建立对项目整体架构的正确心智模型。
- 理解每个文件夹、关键文件的职责，以及它们如何交互。
- 明确程序入口、主循环、渲染、音频、存储、测试、CI 的实现方式。
- 提供可直接跳转的代码片段，辅助阅读源码。

> 说明：本报告基于仓库文本文件全量阅读整理（源码/配置/测试/docs/workflow/XML 资源），二进制资源（png/jpg/wav）按路径和用途分类说明。

---

## 1. 项目总览

### 1.1 项目定位

这是一个 Java 17 的 Android 竖屏飞机大战项目，采用**双模块结构**：

- `game-core`：平台无关游戏逻辑层（纯 Java，可单测）。
- `app`：Android 适配层（Activity、SurfaceView 渲染、音频、CSV 存储）。

设计目标是将“玩法规则”与“平台实现”解耦，做到：
- 逻辑可测试、可复用、可验证一致性。
- UI/渲染/音频/生命周期在 Android 层独立演进。

### 1.2 技术栈

- 语言：Java 17
- 构建：Gradle Wrapper 8.7
- Android Gradle Plugin：8.5.2
- Android：AndroidX + Material
- 渲染：`SurfaceView` + `GameRenderThread` 独立线程
- 测试：`game-core` 用 JUnit 5；`app` 用 JUnit 4 + Espresso
- CI：GitHub Actions 构建 Debug APK

---

## 2. 架构全景

### 2.1 分层关系

```text
用户触控/Activity
    │
    ▼
app/ui + app/view + app/audio + app/storage
    │                    │
    │                    └── 本地文件 filesDir/scores.csv
    │
    └── 调用 game-core（不依赖 Android SDK）
             ├── engine（GameEngine + Snapshot）
             ├── difficulty / spawn
             ├── model / strategy
             └── effect（TimedEffect + Scheduler）
```

### 2.2 依赖方向

- `:app` 依赖 `:game-core`（`app/build.gradle`）。
- `:game-core` 不依赖 `:app`，不依赖 Android SDK。
- 依赖方向单向，避免 UI 层反向耦合核心规则。

### 2.3 运行时主链路

1. `GameSurfaceView` 接收触控并更新英雄目标坐标。
2. `GameRenderThread` 每 40ms 调用 `engine.tick(40)`。
3. `GameEngine` 执行固定相位（input/update/collision/spawn/cleanup）。
4. `engine.getSnapshot()` 输出只读快照。
5. 渲染线程按快照绘制；`onFrame` 根据事件计数驱动音效与 HUD。
6. 当 `gameOverEvents` 增加时，`GameActivity` 跳转结算页。

---

## 3. 程序入口与启动机制

### 3.1 Android 入口

- 文件：`app/src/main/AndroidManifest.xml`
- 入口 Activity：`com.airwar.android.ui.LauncherActivity`
- 通过 `MAIN + LAUNCHER` intent-filter 注册为应用启动入口。

关键片段：`app/src/main/AndroidManifest.xml`

```xml
<activity
    android:name=".ui.LauncherActivity"
    android:exported="true"
    android:screenOrientation="portrait">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### 3.2 启动链路

1. `LauncherActivity`：仅负责转发到 `MenuActivity`。
2. `MenuActivity`：选择难度，点击开始进入 `GameActivity`。
3. `GameActivity`：绑定 `GameSurfaceView`、音频管理器与 HUD。
4. 结束后 `GameOverActivity`：输入昵称/选择头像/保存成绩。
5. 进入 `LeaderboardActivity`：按难度读取和展示排行榜。

关键片段：`app/src/main/java/com/airwar/android/ui/LauncherActivity.java`

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    startActivity(new Intent(this, MenuActivity.class));
    finish();
}
```

---

## 4. 核心机制详解

### 4.1 输入机制（触控 -> 逻辑坐标）

- 文件：`app/src/main/java/com/airwar/android/view/GameSurfaceView.java`
- 触控坐标先映射到逻辑分辨率（`GameConstants.LOGICAL_WIDTH/HEIGHT`），再写入 `GameEngine`。
- 通过 `engineLock` 同步，避免与渲染线程并发冲突。

关键片段：

```java
int logicalX = toLogicalX(event.getX());
int logicalY = toLogicalY(event.getY());
synchronized (engineLock) {
    engine.setHeroTarget(logicalX, logicalY);
}
```

### 4.2 主循环机制（40ms Tick）

- 文件：`app/src/main/java/com/airwar/android/view/GameRenderThread.java`
- 固定周期：`TICK_MS = 40L`（约 25 FPS 逻辑帧率）。
- 每帧：`tick -> snapshot -> draw -> sleep -> frameListener`。

关键片段：

```java
synchronized (engineLock) {
    engine.tick(TICK_MS);
    snapshot = engine.getSnapshot();
}
```

### 4.3 逻辑相位机制（可测试顺序）

- 文件：`game-core/src/main/java/com/airwar/core/engine/GameEngine.java`
- Tick 相位固定为：`input -> update -> collision -> spawn -> cleanup`。
- 该顺序由测试 `GameEngineTickOrderTest` 校验。

关键片段：

```java
phase("input");
updateHeroAnchor();
phase("update");
updateBattle(deltaMs);
phase("collision");
handleCollisions();
phase("spawn");
...
phase("cleanup");
cleanupEntities();
```

### 4.4 快照机制（逻辑层与渲染层解耦）

- `GameEngine` 内部状态是可变集合。
- 对外通过 `GameStateSnapshot` record 暴露不可变视图。
- `List.copyOf(...)` 防止 UI 层误修改核心状态。

关键片段：`game-core/src/main/java/com/airwar/core/engine/GameStateSnapshot.java`

```java
public GameStateSnapshot {
    heroBullets = List.copyOf(heroBullets);
    enemyBullets = List.copyOf(enemyBullets);
    enemies = List.copyOf(enemies);
    props = List.copyOf(props);
    explosions = List.copyOf(explosions);
}
```

### 4.5 音频机制（事件计数驱动）

- 文件：`app/src/main/java/com/airwar/android/audio/AndroidAudioManager.java`
- BGM：`MediaPlayer`（普通/Boss 两轨）
- SFX：`SoundPool`
- `GameSurfaceView.onFrame` 比较事件计数差值，避免同一事件重复触发。

关键片段：`app/src/main/java/com/airwar/android/view/GameSurfaceView.java`

```java
if (snapshot.heroShotEvents() > lastHeroShotEvents) {
    audioManager.playBulletSfx();
}
if (snapshot.gameOverEvents() > lastGameOverEvents) {
    audioManager.playGameOverSfx();
}
```

### 4.6 存储机制（CSV 持久化 + 兼容旧格式）

- DAO：`AndroidScoreDao`
- 序列化器：`ScoreCsvSerializer`
- 路径：`filesDir/scores.csv`
- 兼容：支持旧 header 与旧字段数（3/4/5 列），并在读取/重写时迁移。

关键片段：`app/src/main/java/com/airwar/android/storage/ScoreCsvSerializer.java`

```java
if (fields.size() != 3 && fields.size() != 4 && fields.size() != 5) {
    throw new IllegalArgumentException("invalid score csv line: " + line);
}
String difficulty = fields.size() >= 4 ? unescape(fields.get(3)) : "normal";
String avatarId = fields.size() >= 5 ? unescape(fields.get(4)) : "default";
```

---

## 5. 关键时序

### 5.1 启动时序

```text
Manifest
  -> LauncherActivity.onCreate
  -> startActivity(MenuActivity)
  -> MenuActivity 选择难度
  -> startActivity(GameActivity)
```

### 5.2 游戏帧时序

```text
GameRenderThread.run
  -> engine.tick(40)
  -> engine.getSnapshot()
  -> drawFrame(canvas, snapshot)
  -> frameListener.onFrame(snapshot)
      -> GameSurfaceView: 音频事件 + post(snapshot)
      -> GameActivity.updateHud: 分数/HP
      -> 若 gameOverEvents 增加 -> 跳 GameOverActivity
```

### 5.3 结算写榜单时序

```text
GameOverActivity
  -> 读取 intent(score/duration/difficulty)
  -> 玩家输入昵称 + 选择头像
  -> AndroidScoreDao.appendScore(GameScore)
      -> ensureHeader()
      -> serialize + append 到 filesDir/scores.csv
  -> 跳转 LeaderboardActivity
  -> readScoresSortedByDifficulty(difficulty)
  -> UI 展示 Top3 + 列表
```

---

## 6. 目录与文件全量索引

以下按目录给出**可读文本文件全覆盖**说明。

### 6.1 根目录

- `README.md`：项目目标、架构、运行和测试命令说明。
- `settings.gradle`：定义仓库模块 `:app`, `:game-core`。
- `build.gradle`：顶层 AGP 插件版本声明。
- `gradle.properties`：Gradle/AndroidX 全局配置。
- `gradlew`：Linux/macOS Gradle wrapper 脚本。
- `gradlew.bat`：Windows Gradle wrapper 脚本。
- `.gitignore`：忽略规则。
- `.vscode/settings.json`：本地 IDE 设置。

### 6.2 `gradle/wrapper`

- `gradle-wrapper.properties`：Gradle 分发地址（8.7）和 wrapper 参数。

### 6.3 `.github/workflows`

- `build-apk.yml`：CI 流水线；安装 JDK17 + Android SDK 34，执行 `:app:assembleDebug`，上传 APK。

### 6.4 `docs`

- `docs/android/parity-checklist.md`：玩法一致性人工检查清单。
- `docs/superpowers/specs/2026-04-07-ui-unification-design.md`：UI 统一设计文档。
- `docs/superpowers/plans/2026-04-07-ui-unification-implementation.md`：对应实现计划。

### 6.5 `app` 模块

#### 6.5.1 构建与清单

- `app/build.gradle`：应用模块配置（SDK、依赖、测试）。
- `app/src/main/AndroidManifest.xml`：Activity 声明、入口定义、竖屏约束。

#### 6.5.2 `app/src/main/java/com/airwar/android/ui`

- `LauncherActivity.java`：启动转发器，进入菜单页。
- `MenuActivity.java`：难度选择、展示难度参数、启动游戏、跳排行榜。
- `GameActivity.java`：游戏页控制器；绑定 SurfaceView、音频、HUD；侦测游戏结束并导航。
- `GameOverActivity.java`：展示成绩，采集昵称/头像，写入成绩，跳转榜单。
- `LeaderboardActivity.java`：按难度加载成绩，渲染 Top3 和普通列表，提供回菜单。
- `PilotAvatarRegistry.java`：头像 ID 与 drawable 映射。
- `RandomPilotNameGenerator.java`：随机飞行员昵称生成（带防冲突后缀逻辑）。

#### 6.5.3 `app/src/main/java/com/airwar/android/view`

- `GameSurfaceView.java`：触控输入、引擎实例、Surface 生命周期、线程控制、snapshot 分发。
- `GameRenderThread.java`：逻辑 tick + Canvas 渲染循环。
- `SpriteRepository.java`：精灵/背景加载与画笔资源集中管理。

#### 6.5.4 `app/src/main/java/com/airwar/android/audio`

- `AndroidAudioManager.java`：音频抽象层（Backend）、BGM 状态机、SFX 播放、资源释放。

#### 6.5.5 `app/src/main/java/com/airwar/android/storage`

- `GameScore.java`：成绩实体（score/name/duration/difficulty/avatarId）。
- `ScoreCsvSerializer.java`：CSV 的序列化/反序列化，转义/反转义，旧格式兼容。
- `AndroidScoreDao.java`：CSV 文件读写，header 迁移，按规则排序。

#### 6.5.6 `app/src/main/res/layout`

- `activity_launcher.xml`：启动页布局。
- `activity_menu.xml`：菜单页布局（难度选择、开始按钮、预览）。
- `activity_game.xml`：游戏页根布局，包含 `GameSurfaceView` 与 HUD。
- `view_hud_overlay.xml`：HUD 组件（分数/生命）。
- `activity_game_over.xml`：结算页布局（分数、时长、输入、头像、提交）。
- `activity_leaderboard.xml`：排行榜页面布局。
- `comp_top_header.xml`：复用顶部标题组件。
- `comp_bottom_nav.xml`：复用底部导航组件。
- `comp_rank_row.xml`：排行榜普通行组件。

#### 6.5.7 `app/src/main/res/values`

- `strings.xml`：文案与格式化字符串。
- `colors.xml`：基础配色。
- `ui2_colors.xml`：新版 UI 主题配色。
- `ui2_dimens.xml`：新版 UI 尺寸常量。
- `ui2_styles.xml`：新版 UI 样式定义。

#### 6.5.8 `app/src/main/res/drawable`（XML 文本资源）

- `ui2_avatar_border_selected.xml`
- `ui2_avatar_border.xml`
- `ui2_button_primary.xml`
- `ui2_button_chip.xml`
- `ui2_panel_glass.xml`
- `ui2_rank_row_bg.xml`
- `ui2_bg_screen.xml`
- `ui_input_bg.xml`
- `ui_bg_screen.xml`
- `ui_panel.xml`
- `ui_button_primary.xml`
- `ui_button_chip.xml`
- `ic_nav_rocket.xml`
- `ic_trophy.xml`
- `ic_score.xml`
- `ic_heart.xml`
- `pilot_01.xml`
- `pilot_02.xml`
- `pilot_03.xml`
- `pilot_04.xml`
- `pilot_05.xml`
- `pilot_06.xml`

用途：按钮/边框/面板/背景/图标/头像资源别名。

#### 6.5.9 `app/src/main/res/drawable`（二进制图片，按用途）

- 战斗实体：`hero.png`, `mob.png`, `elite.png`, `elite_plus.png`, `boss.png`, `bullet_hero.png`, `bullet_enemy.png`, `prop_bullet.png`, `prop_bullet_plus.png`, `prop_bomb.png`, `prop_blood.png`
- 背景：`bg.jpg`, `bg2.jpg`, `bg3.jpg`, `bg4.jpg`, `bg5.jpg`

#### 6.5.10 `app/src/main/res/raw`（二进制音频）

- `bgm.wav`, `bgm_boss.wav`
- `bullet.wav`, `bullet_hit.wav`
- `bomb_explosion.wav`, `get_supply.wav`, `game_over.wav`

#### 6.5.11 `app/src/test/java/com/airwar/android`

- `audio/AndroidAudioManagerStateTest.java`：音频状态行为测试。
- `storage/ScoreCsvSerializerTest.java`：CSV 序列化与兼容测试。
- `ui/RandomPilotNameGeneratorTest.java`：随机名生成逻辑测试。

#### 6.5.12 `app/src/androidTest/java/com/airwar/android/ui`

- `GameActivityLaunchTest.java`：游戏页启动可见性测试。
- `MenuToGameFlowTest.java`：菜单到游戏导航流程测试。

### 6.6 `game-core` 模块

#### 6.6.1 构建

- `game-core/build.gradle`：Java library 模块，启用 JUnit Platform。

#### 6.6.2 `game-core/src/main/java/com/airwar/core/config`

- `GameConstants.java`：逻辑分辨率、Tick、英雄最大血量等常量。

#### 6.6.3 `game-core/src/main/java/com/airwar/core/difficulty`

- `DifficultyLevel.java`：难度枚举。
- `DifficultyConfig.java`：不同难度下的参数集与参数校验。

#### 6.6.4 `game-core/src/main/java/com/airwar/core/effect`

- `TimedEffect.java`：带生命周期的效果对象。
- `EffectScheduler.java`：效果调度与到期回滚执行。

#### 6.6.5 `game-core/src/main/java/com/airwar/core/model`

- `FlyingObject.java`：飞行物基础模型。
- `aircraft/AbstractAircraft.java`：飞机抽象父类。
- `aircraft/HeroAircraft.java`：英雄机行为模型（射击策略挂载点）。
- `bullet/BaseBullet.java`：子弹模型。
- `prop/BaseProp.java`：道具模型。

#### 6.6.6 `game-core/src/main/java/com/airwar/core/strategy`

- `ShootStrategy.java`：射击策略接口。
- `StraightShootStrategy.java`：直线射击。
- `ScatterShootStrategy.java`：散射策略。
- `CircularShootStrategy.java`：环形射击（道具增强）。

#### 6.6.7 `game-core/src/main/java/com/airwar/core/spawn`

- `EnemySpawner.java`：Boss 阈值桶化生成计数。

#### 6.6.8 `game-core/src/main/java/com/airwar/core/engine`

- `GameEngine.java`：核心战斗循环、碰撞、得分、掉落、快照输出。
- `GameStateSnapshot.java`：给渲染层读取的不可变状态快照。

#### 6.6.9 `game-core/src/test/java/com/airwar/core`

- `difficulty/BossSpawnThresholdTest.java`
- `effect/EffectSchedulerTest.java`
- `engine/GameEngineTickOrderTest.java`
- `engine/GameEngineBattleSnapshotTest.java`
- `model/FlyingObjectCollisionTest.java`
- `parity/GameplayParityTest.java`
- `strategy/ShootStrategyTest.java`

---

### 6.7 文件夹职责与交互矩阵（按目录）

| 文件夹 | 主要职责 | 上游输入 | 下游输出 |
|---|---|---|---|
| `app/src/main/java/com/airwar/android/ui` | 页面导航、参数收集、用户流程组织 | 用户点击、`Intent` 参数、快照回调 | 调用 `view`/`storage`/`audio`，页面跳转 |
| `app/src/main/java/com/airwar/android/view` | 触控处理、逻辑驱动、帧绘制 | 触控事件、`GameEngine` 状态 | 画面输出、音频事件触发、HUD 回调 |
| `app/src/main/java/com/airwar/android/audio` | BGM/SFX 管理与状态切换 | `view` 层事件计数变化、Activity 生命周期 | 实际音频播放/停止 |
| `app/src/main/java/com/airwar/android/storage` | 榜单对象建模、CSV 编解码、文件读写 | `GameOverActivity` 提交成绩、`LeaderboardActivity` 查询 | `filesDir/scores.csv`，排序后列表 |
| `app/src/main/res/layout` | 页面与组件布局骨架 | Activity `setContentView`/`inflate` | View 树与控件 ID |
| `app/src/main/res/values` | 文案、主题色、尺寸、样式 | 代码/布局资源引用 | 统一视觉和文本常量 |
| `app/src/main/res/drawable` | 图标、面板、按钮背景、头像别名、战斗贴图 | 布局属性、`SpriteRepository`、Avatar 注册表 | UI 视觉资源与战斗贴图 |
| `app/src/main/res/raw` | 音频素材 | `AndroidAudioManager` 载入 | BGM/SFX 数据源 |
| `game-core/src/main/java/com/airwar/core/engine` | 游戏循环与状态机核心 | `view` 层 tick/输入 | `GameStateSnapshot` 给渲染层 |
| `game-core/src/main/java/com/airwar/core/difficulty` | 难度参数与校验 | UI 选择的难度字符串映射 | `GameEngine` 参数集合 |
| `game-core/src/main/java/com/airwar/core/spawn` | Boss 阈值生成策略 | 当前 score | Boss 生成计数 |
| `game-core/src/main/java/com/airwar/core/strategy` | 射击模式抽象与实现 | `HeroAircraft` 或道具效果切换 | 子弹发射模式 |
| `game-core/src/main/java/com/airwar/core/effect` | 时效效果调度与回滚 | `GameEngine` 添加效果请求 | 到期前后回调执行 |
| `game-core/src/main/java/com/airwar/core/model` | 飞行体/子弹/飞机/道具基础模型 | `GameEngine` 战斗阶段逻辑 | 碰撞与移动基础数据 |
| `game-core/src/test/java/com/airwar/core` | 规则正确性与一致性验证 | 核心模块代码 | 回归验证结果 |
| `app/src/test` / `app/src/androidTest` | Android 侧单测与界面流程测试 | app 层代码 | 本地与设备端验证结果 |

---

## 7. 关键代码导读（按机制）

### 7.1 难度参数如何从菜单流到核心引擎

`MenuActivity` 负责 UI 选择，`GameSurfaceView.setDifficulty` 负责将字符串转换为 `DifficultyLevel` 并重建引擎。

文件：`app/src/main/java/com/airwar/android/view/GameSurfaceView.java`

```java
public void setDifficulty(String difficulty) {
    DifficultyLevel parsed = parseDifficulty(difficulty);
    synchronized (engineLock) {
        if (renderThread == null) {
            difficultyLevel = parsed;
            engine = GameEngine.create(difficultyLevel);
        }
    }
}
```

### 7.2 游戏结束判定如何通知页面跳转

逻辑层维护 `gameOverEvents` 计数；UI 层观察计数递增触发导航，避免重复跳转。

文件：`app/src/main/java/com/airwar/android/ui/GameActivity.java`

```java
if (!gameOverNavigated && snapshot.gameOverEvents() > lastGameOverEvents) {
    gameOverNavigated = true;
    Intent intent = new Intent(this, GameOverActivity.class);
    intent.putExtra(GameOverActivity.EXTRA_SCORE, snapshot.score());
    ...
    startActivity(intent);
    finish();
}
```

### 7.3 道具增强如何自动回滚

文件：`game-core/src/main/java/com/airwar/core/engine/GameEngine.java`

```java
effectScheduler.add(new TimedEffect(
    durationMs,
    () -> {
        hero.setShootStrategy(new CircularShootStrategy(12, 7));
        heroShootMode = "CIRCULAR";
    },
    () -> {
        hero.setShootStrategy(new StraightShootStrategy(1));
        heroShootMode = "STRAIGHT";
    }
));
```

### 7.4 CSV Header 迁移保障

文件：`app/src/main/java/com/airwar/android/storage/AndroidScoreDao.java`

```java
if (ScoreCsvSerializer.HEADER.equals(firstLine)) {
    return;
}
if (LEGACY_HEADER.equals(firstLine)) {
    rewriteFileWithCurrentHeader(file);
    return;
}
rewriteFileWithCurrentHeader(file);
```

### 7.5 Boss 触发逻辑

文件：`game-core/src/main/java/com/airwar/core/spawn/EnemySpawner.java`

```java
int currentBucket = score / bossScoreThreshold;
if (currentBucket > maxSpawnBucket) {
    bossCount += (currentBucket - maxSpawnBucket);
    maxSpawnBucket = currentBucket;
}
```

---

## 8. 构建、测试与 CI

### 8.1 本地常用命令

- 安装调试包：`./gradlew :app:installDebug`
- 构建调试包：`./gradlew :app:assembleDebug`
- 构建发布包：`./gradlew :app:assembleRelease`
- 核心逻辑测试：`./gradlew :game-core:test`
- App 单测：`./gradlew :app:testDebugUnitTest`
- App 仪器测试：`./gradlew :app:connectedDebugAndroidTest`

### 8.2 CI 流程

文件：`/.github/workflows/build-apk.yml`

流程：
1. Checkout 代码
2. 安装 JDK 17
3. 安装 Gradle 8.7
4. 安装 Android SDK 34（含 build-tools）
5. 执行 `gradle :app:assembleDebug --stacktrace`
6. 上传 `app-debug.apk` 作为 artifact

---

## 9. 风险与改进建议

1. `app/build.gradle` 的 release 配置引用了 `proguard-rules.pro`，仓库未见该文件，发布构建可能受影响。
2. `AndroidScoreDao` 多处吞异常（`catch ... ignored`），线上/真机问题排查成本较高，建议最少记录日志。
3. `GameEngine` 同时使用 `deltaMs` 与 `System.currentTimeMillis()`（爆炸到期）两套时间源，可考虑统一到 tick 时间提升可重复性。
4. 资源命名存在 `ui_*` 与 `ui2_*` 并行，长期维护建议收敛到单一主题命名体系。

---

## 10. 新开发者 30 分钟上手路径

1. 先读：`README.md`、`settings.gradle`、`app/build.gradle`、`game-core/build.gradle`。
2. 再看启动链：`AndroidManifest.xml` -> `LauncherActivity` -> `MenuActivity`。
3. 主循环：`GameSurfaceView`、`GameRenderThread`、`GameEngine`、`GameStateSnapshot`。
4. 结算与榜单：`GameActivity`、`GameOverActivity`、`AndroidScoreDao`、`ScoreCsvSerializer`、`LeaderboardActivity`。
5. 跑测试：先 `:game-core:test`，再 `:app:testDebugUnitTest`。

---

## 11. 附：本报告覆盖范围

本报告覆盖仓库中以下类型文件：
- 源码：`*.java`
- 构建配置：`*.gradle`, `gradle-wrapper.properties`
- 文档：`*.md`
- CI：`*.yml`
- Android 资源文本：`res/layout/*.xml`, `res/values/*.xml`, `res/drawable/*.xml`, `AndroidManifest.xml`
- 测试：`app/src/test`, `app/src/androidTest`, `game-core/src/test`

二进制文件（`*.png`, `*.jpg`, `*.wav`）按路径与用途归类说明，不做字节内容解析。

---

## 12. 附录：关键调用关系索引

### 12.1 启动与导航调用链

```text
AndroidManifest.xml
  -> LauncherActivity
      -> MenuActivity
          -> GameActivity
              -> GameOverActivity
                  -> LeaderboardActivity
```

### 12.2 游戏运行调用链

```text
GameActivity
  -> GameSurfaceView
      -> GameRenderThread
          -> GameEngine.tick(deltaMs)
          -> GameEngine.getSnapshot()
      -> onFrame(snapshot)
          -> AndroidAudioManager.play*
          -> SnapshotListener (GameActivity.updateHud)
```

### 12.3 排行榜数据调用链

```text
GameOverActivity.submit
  -> AndroidScoreDao.appendScore
      -> ScoreCsvSerializer.serialize
      -> filesDir/scores.csv

LeaderboardActivity.onCreate
  -> AndroidScoreDao.readScoresSortedByDifficulty
      -> ScoreCsvSerializer.deserialize
      -> 排序后回填 UI
```

### 12.4 核心规则调用链

```text
GameEngine.tick
  -> updateBattle
  -> handleCollisions
  -> EnemySpawner.update(score)
  -> cleanupEntities
  -> EffectScheduler.update(deltaMs)
```

---

## 13. 核心源码逐文件交互说明（Java 全量）

本节覆盖 `app/src/main/java` 与 `game-core/src/main/java` 下全部 Java 文件，强调“谁调用它、它调用谁、数据如何流动”。

### 13.1 app/ui

#### `LauncherActivity.java`

- 职责：应用可见入口后的转发器。
- 被谁调用：Android 启动器（Manifest 的 `MAIN/LAUNCHER`）。
- 调用谁：`MenuActivity`。
- 数据流：无业务参数，只做页面切换。

#### `MenuActivity.java`

- 职责：选择难度、展示参数、发起游戏、跳转排行榜。
- 被谁调用：`LauncherActivity`、主页按钮返回。
- 调用谁：`DifficultyConfig.of(...)`、`GameActivity`、`LeaderboardActivity`。
- 数据流：
  - 输入：用户选择 `easy/normal/hard`。
  - 输出：通过 `Intent EXTRA_DIFFICULTY` 传给 `GameActivity`；传给 `LeaderboardActivity` 作为筛选条件。

#### `GameActivity.java`

- 职责：游戏页生命周期控制与 HUD 更新。
- 被谁调用：`MenuActivity`。
- 调用谁：`GameSurfaceView`、`AndroidAudioManager`、`GameOverActivity`。
- 数据流：
  - 输入：难度参数。
  - 中间：接收 snapshot 回调（分数、HP、事件计数）。
  - 输出：在 `gameOverEvents` 递增时封装 `score/durationSec/difficulty` 跳转结算页。

#### `GameOverActivity.java`

- 职责：收集结算信息并写榜单。
- 被谁调用：`GameActivity`。
- 调用谁：`RandomPilotNameGenerator`、`PilotAvatarRegistry`、`AndroidScoreDao`、`LeaderboardActivity`。
- 数据流：
  - 输入：`score/durationSec/difficulty`。
  - 输出：`GameScore` 对象写入 `scores.csv`；跳转排行榜页面。

#### `LeaderboardActivity.java`

- 职责：读取并展示按难度过滤的排行榜。
- 被谁调用：`MenuActivity`、`GameOverActivity`。
- 调用谁：`AndroidScoreDao.readScoresSortedByDifficulty`、`PilotAvatarRegistry.drawableFor`。
- 数据流：
  - 输入：`difficulty`。
  - 输出：Top3 卡片 + 普通行列表渲染。

#### `PilotAvatarRegistry.java`

- 职责：头像 ID 到 drawable 的静态映射。
- 被谁调用：`GameOverActivity`、`LeaderboardActivity`。
- 调用谁：无（纯映射逻辑）。
- 数据流：输入 `avatarId`，输出 `R.drawable.*`。

#### `RandomPilotNameGenerator.java`

- 职责：生成可读昵称（含时间戳与同毫秒计数后缀）。
- 被谁调用：`GameOverActivity`。
- 调用谁：`TimeProvider.nowMillis()`、`Random`。
- 数据流：输出字符串格式 `飞行员-XXX-########(-n)`。

### 13.2 app/view

#### `GameSurfaceView.java`

- 职责：输入转换、Surface 生命周期、渲染线程管理、帧回调分发。
- 被谁调用：`GameActivity`（setDifficulty/setAudioManager/setSnapshotListener）与系统 Surface 回调。
- 调用谁：`GameEngine`、`GameRenderThread`、`AndroidAudioManager`。
- 数据流：
  - 输入：触控坐标。
  - 变换：映射到逻辑坐标后喂给 `engine.setHeroTarget`。
  - 输出：
    - 音频：基于事件计数差值触发 SFX/BGM。
    - UI：`post` 到主线程回调 snapshot 给 `GameActivity`。

#### `GameRenderThread.java`

- 职责：40ms 固定帧循环，驱动逻辑与绘制。
- 被谁调用：`GameSurfaceView.surfaceCreated` 创建并启动。
- 调用谁：`GameEngine.tick/getSnapshot`、`SpriteRepository`、`FrameListener`。
- 数据流：
  - 输入：引擎状态 + 资源贴图。
  - 输出：Canvas 绘制帧 + snapshot 回调。

#### `SpriteRepository.java`

- 职责：集中装载渲染资源（背景、敌我贴图、子弹、道具、画笔）。
- 被谁调用：`GameRenderThread`。
- 调用谁：`BitmapFactory.decodeResource`。
- 数据流：输入资源 ID，输出 `Bitmap/Paint` 句柄。

### 13.3 app/audio

#### `AndroidAudioManager.java`

- 职责：音频门面 + 后端封装 + 状态管理。
- 被谁调用：`GameActivity`（生命周期控制），`GameSurfaceView.onFrame`（事件触发音效）。
- 调用谁：
  - `RealBackend`：`MediaPlayer`、`SoundPool`
  - `NoOpBackend`：测试/空实现
- 数据流：
  - 输入：BGM 切换命令、事件触发命令。
  - 输出：实际播放/停止，且利用 `activeTrack` 避免重复播放。

### 13.4 app/storage

#### `GameScore.java`

- 职责：成绩不可变数据对象（含难度和头像）。
- 被谁调用：`GameOverActivity`、DAO、Serializer、排行榜 UI。
- 调用谁：`Objects.requireNonNull`。
- 数据流：构造时归一化 `difficulty/avatarId` 到小写并给默认值。

#### `ScoreCsvSerializer.java`

- 职责：CSV 编解码与转义规则。
- 被谁调用：`AndroidScoreDao`。
- 调用谁：内部 `escape/unescape/splitEscaped`。
- 数据流：
  - `GameScore -> String`（写文件）
  - `String -> GameScore`（读文件）
  - 兼容 3/4/5 字段旧数据。

#### `AndroidScoreDao.java`

- 职责：榜单文件访问层。
- 被谁调用：`GameOverActivity`（append）、`LeaderboardActivity`（read）。
- 调用谁：`ScoreCsvSerializer`、`FileReader/FileWriter`、`Context.getFilesDir()`。
- 数据流：
  - 写入：append score -> ensure header -> serialize -> newline。
  - 读取：read line -> deserialize -> difficulty filter -> 排序返回。
  - 迁移：旧 header 检测后重写为新 header。

### 13.5 game-core/config + difficulty + spawn

#### `GameConstants.java`

- 职责：逻辑画布尺寸、Tick、英雄最大 HP 常量。
- 被谁调用：`GameEngine`、`GameSurfaceView`、`GameRenderThread`、模型层。

#### `DifficultyLevel.java`

- 职责：难度枚举（EASY/NORMAL/HARD）。
- 被谁调用：`MenuActivity`、`GameSurfaceView`、`GameEngine.create`。

#### `DifficultyConfig.java`

- 职责：难度参数总表与合法性校验。
- 被谁调用：`MenuActivity`（参数展示）、`GameEngine`（战斗参数）、测试。
- 数据流：输入难度，输出一组不可变参数。

#### `EnemySpawner.java`

- 职责：根据得分阈值桶化计算 boss 生成次数。
- 被谁调用：`GameEngine.tick` 的 spawn 相位。
- 数据流：输入 score，输出 bossCount 递增。

### 13.6 game-core/effect

#### `TimedEffect.java`

- 职责：封装一个“开始动作 + 到期动作 + 时长”。
- 被谁调用：`EffectScheduler.update`。
- 数据流：累计 `deltaMs`，到时返回 `true` 让调度器移除。

#### `EffectScheduler.java`

- 职责：维护激活效果列表，逐 tick 更新并移除到期效果。
- 被谁调用：`GameEngine`。
- 数据流：输入新增效果与 `deltaMs`，输出为回调副作用（策略切换等）。

### 13.7 game-core/model

#### `FlyingObject.java`

- 职责：飞行物公共属性与移动/碰撞基础逻辑。
- 被谁调用：`AbstractAircraft`、`BaseBullet`、`BaseProp`。
- 数据流：位置、速度、尺寸、有效性状态。

#### `AbstractAircraft.java`

- 职责：飞机基类（HP 管理 + 抽象 `shoot`）。
- 被谁调用：`HeroAircraft`。
- 数据流：`decreaseHp` 触发 `vanish`。

#### `HeroAircraft.java`

- 职责：英雄机射击行为，支持策略注入。
- 被谁调用：`GameEngine`。
- 调用谁：`ShootStrategy.shoot`。
- 数据流：默认直线策略；道具期间可切换环形策略。

#### `BaseBullet.java`

- 职责：子弹抽象类型（含威力与边界失效逻辑）。
- 被谁调用：策略实现中的内部子弹类。

#### `BaseProp.java`

- 职责：道具抽象类型，约束 `active(HeroAircraft)`。
- 被谁调用：具体道具类型（当前核心引擎用轻量字符串状态实现，不直接实例化子类）。

### 13.8 game-core/strategy

#### `ShootStrategy.java`

- 职责：射击策略统一接口。
- 被谁调用：`HeroAircraft`。

#### `StraightShootStrategy.java`

- 职责：按 `shootNum` 发射直线子弹。
- 被谁调用：`HeroAircraft` 默认策略、道具回滚后策略。

#### `ScatterShootStrategy.java`

- 职责：按横向偏移速度形成散射。
- 被谁调用：测试或扩展策略点。

#### `CircularShootStrategy.java`

- 职责：360 度环形发射。
- 被谁调用：`GameEngine.debugGrantSuperBullet` 的增强态。

### 13.9 game-core/engine

#### `GameStateSnapshot.java`

- 职责：渲染层读取的数据契约，保证不可变拷贝。
- 被谁调用：`GameRenderThread`、`GameSurfaceView`、`GameActivity`。

#### `GameEngine.java`

- 职责：战斗领域核心（状态持有、相位推进、碰撞判定、得分、掉落、事件计数、快照导出）。
- 被谁调用：`GameRenderThread`（每帧 tick）、`GameSurfaceView`（设置英雄目标）、测试。
- 调用谁：`EnemySpawner`、`EffectScheduler`、`HeroAircraft`、策略实现。
- 数据流：
  - 输入：`deltaMs` + hero 目标坐标 + 难度参数。
  - 处理：相位推进与集合更新。
  - 输出：snapshot + 各类事件计数（供音频/UI 观察）。

---

## 14. 测试资产逐文件说明（测试目录全量）

### 14.1 app 单元测试

- `app/src/test/java/com/airwar/android/audio/AndroidAudioManagerStateTest.java`
  - 覆盖：启停逻辑、禁用后的阻断、重新启用后恢复。
- `app/src/test/java/com/airwar/android/storage/ScoreCsvSerializerTest.java`
  - 覆盖：CSV round-trip、转义字符、旧格式回退默认值。
- `app/src/test/java/com/airwar/android/ui/RandomPilotNameGeneratorTest.java`
  - 覆盖：昵称格式与同毫秒计数后缀去重。

### 14.2 app 仪器测试

- `app/src/androidTest/java/com/airwar/android/ui/GameActivityLaunchTest.java`
  - 覆盖：游戏页核心控件显示。
- `app/src/androidTest/java/com/airwar/android/ui/MenuToGameFlowTest.java`
  - 覆盖：点击开始后的页面跳转 Intent。

### 14.3 game-core 测试

- `game-core/src/test/java/com/airwar/core/difficulty/BossSpawnThresholdTest.java`
  - 覆盖：Boss 阈值、重复 tick 与回退再返回的去重行为、难度参数表。
- `game-core/src/test/java/com/airwar/core/effect/EffectSchedulerTest.java`
  - 覆盖：效果开始/过期调用次数与移除。
- `game-core/src/test/java/com/airwar/core/engine/GameEngineTickOrderTest.java`
  - 覆盖：相位顺序固定性。
- `game-core/src/test/java/com/airwar/core/engine/GameEngineBattleSnapshotTest.java`
  - 覆盖：默认快照、若干 tick 后实体出现、Boss 激活、碰撞扣血、超级子弹回滚。
- `game-core/src/test/java/com/airwar/core/model/FlyingObjectCollisionTest.java`
  - 覆盖：碰撞箱边界与机体纵向压缩判定。
- `game-core/src/test/java/com/airwar/core/parity/GameplayParityTest.java`
  - 覆盖：增强效果在时效结束后恢复策略。
- `game-core/src/test/java/com/airwar/core/strategy/ShootStrategyTest.java`
  - 覆盖：三类策略发射数量与基础输出。

---

## 15. 资源目录逐项说明（XML 全量 + 二进制分组）

### 15.1 layout 目录

- `activity_launcher.xml`：极简启动页，仅展示 app 名称。
- `activity_menu.xml`：包含顶部头、难度预览、参数表、开始按钮、底部导航。
- `activity_game.xml`：`GameSurfaceView` + HUD 叠层。
- `view_hud_overlay.xml`：分数与生命条显示。
- `activity_game_over.xml`：成绩、时长、昵称输入、头像滑动选择、提交。
- `activity_leaderboard.xml`：Top3 区、列表区、返回主页按钮。
- `comp_top_header.xml`：复用顶部标题。
- `comp_bottom_nav.xml`：复用底部主页/排行榜导航。
- `comp_rank_row.xml`：排行榜普通行样式。

### 15.2 values 目录

- `strings.xml`：完整页面文案、HUD 文案、按钮文案、格式化模板。
- `colors.xml`：旧风格基础配色（如 `ui_text_title`）。
- `ui2_colors.xml`：新版主题色（强调 `ui2_accent`）。
- `ui2_dimens.xml`：间距、圆角、导航高度、头像尺寸。
- `ui2_styles.xml`：简洁文本样式封装（Title/Body/Label）。

### 15.3 drawable XML 目录（逐项）

- `ui2_avatar_border_selected.xml`：头像选中态边框。
- `ui2_avatar_border.xml`：头像默认边框。
- `ui2_button_primary.xml`：新版主按钮背景。
- `ui2_button_chip.xml`：新版难度 chip 背景。
- `ui2_panel_glass.xml`：玻璃态面板背景。
- `ui2_rank_row_bg.xml`：排行榜行背景。
- `ui2_bg_screen.xml`：新版页面背景。
- `ui_input_bg.xml`：输入框背景。
- `ui_bg_screen.xml`：旧版页面背景。
- `ui_panel.xml`：旧版面板背景（HUD 使用）。
- `ui_button_primary.xml`：旧版主按钮背景。
- `ui_button_chip.xml`：旧版 chip 背景。
- `ic_nav_rocket.xml`：主页导航图标。
- `ic_trophy.xml`：排行榜图标。
- `ic_score.xml`：得分图标。
- `ic_heart.xml`：生命图标。
- `pilot_01.xml` ~ `pilot_06.xml`：头像别名入口（用于 UI 选择和排行显示）。

### 15.4 二进制资源分组

- `drawable/*.png`：战斗实体、道具、子弹贴图（`SpriteRepository` 读取）。
- `drawable/*.jpg`：背景图（菜单预览和战斗背景）。
- `raw/*.wav`：BGM 与 SFX（`AndroidAudioManager.RealBackend` 读取）。

---

## 16. 按功能反查文件（维护索引）

这一节用于“先有需求，再反查代码入口”。每一项都给出推荐阅读顺序。

### 16.0 一键跳转链接（相对路径 href）

以下链接均使用相对路径，可在支持 Markdown 链接跳转的阅读器中直接点击：

- 排行榜：
  [`LeaderboardActivity`](../../../app/src/main/java/com/airwar/android/ui/LeaderboardActivity.java) |
  [`AndroidScoreDao`](../../../app/src/main/java/com/airwar/android/storage/AndroidScoreDao.java) |
  [`ScoreCsvSerializer`](../../../app/src/main/java/com/airwar/android/storage/ScoreCsvSerializer.java) |
  [`GameScore`](../../../app/src/main/java/com/airwar/android/storage/GameScore.java) |
  [`PilotAvatarRegistry`](../../../app/src/main/java/com/airwar/android/ui/PilotAvatarRegistry.java) |
  [`activity_leaderboard.xml`](../../../app/src/main/res/layout/activity_leaderboard.xml) |
  [`comp_rank_row.xml`](../../../app/src/main/res/layout/comp_rank_row.xml)

- 结算页：
  [`GameOverActivity`](../../../app/src/main/java/com/airwar/android/ui/GameOverActivity.java) |
  [`RandomPilotNameGenerator`](../../../app/src/main/java/com/airwar/android/ui/RandomPilotNameGenerator.java) |
  [`activity_game_over.xml`](../../../app/src/main/res/layout/activity_game_over.xml)

- 菜单与难度：
  [`MenuActivity`](../../../app/src/main/java/com/airwar/android/ui/MenuActivity.java) |
  [`DifficultyConfig`](../../../game-core/src/main/java/com/airwar/core/difficulty/DifficultyConfig.java) |
  [`DifficultyLevel`](../../../game-core/src/main/java/com/airwar/core/difficulty/DifficultyLevel.java) |
  [`activity_menu.xml`](../../../app/src/main/res/layout/activity_menu.xml)

- 主循环与输入：
  [`GameRenderThread`](../../../app/src/main/java/com/airwar/android/view/GameRenderThread.java) |
  [`GameSurfaceView`](../../../app/src/main/java/com/airwar/android/view/GameSurfaceView.java) |
  [`GameEngine`](../../../game-core/src/main/java/com/airwar/core/engine/GameEngine.java) |
  [`GameConstants`](../../../game-core/src/main/java/com/airwar/core/config/GameConstants.java)

- 战斗规则/Boss：
  [`GameEngine`](../../../game-core/src/main/java/com/airwar/core/engine/GameEngine.java) |
  [`EnemySpawner`](../../../game-core/src/main/java/com/airwar/core/spawn/EnemySpawner.java) |
  [`BossSpawnThresholdTest`](../../../game-core/src/test/java/com/airwar/core/difficulty/BossSpawnThresholdTest.java)

- 射击策略与道具时效：
  [`ShootStrategy`](../../../game-core/src/main/java/com/airwar/core/strategy/ShootStrategy.java) |
  [`StraightShootStrategy`](../../../game-core/src/main/java/com/airwar/core/strategy/StraightShootStrategy.java) |
  [`ScatterShootStrategy`](../../../game-core/src/main/java/com/airwar/core/strategy/ScatterShootStrategy.java) |
  [`CircularShootStrategy`](../../../game-core/src/main/java/com/airwar/core/strategy/CircularShootStrategy.java) |
  [`TimedEffect`](../../../game-core/src/main/java/com/airwar/core/effect/TimedEffect.java) |
  [`EffectScheduler`](../../../game-core/src/main/java/com/airwar/core/effect/EffectScheduler.java)

- 音频：
  [`AndroidAudioManager`](../../../app/src/main/java/com/airwar/android/audio/AndroidAudioManager.java) |
  [`GameSurfaceView`](../../../app/src/main/java/com/airwar/android/view/GameSurfaceView.java) |
  [`AndroidAudioManagerStateTest`](../../../app/src/test/java/com/airwar/android/audio/AndroidAudioManagerStateTest.java)

- HUD：
  [`view_hud_overlay.xml`](../../../app/src/main/res/layout/view_hud_overlay.xml) |
  [`GameActivity`](../../../app/src/main/java/com/airwar/android/ui/GameActivity.java) |
  [`strings.xml`](../../../app/src/main/res/values/strings.xml)

- 导航流程：
  [`AndroidManifest.xml`](../../../app/src/main/AndroidManifest.xml) |
  [`LauncherActivity`](../../../app/src/main/java/com/airwar/android/ui/LauncherActivity.java) |
  [`MenuActivity`](../../../app/src/main/java/com/airwar/android/ui/MenuActivity.java) |
  [`GameActivity`](../../../app/src/main/java/com/airwar/android/ui/GameActivity.java) |
  [`GameOverActivity`](../../../app/src/main/java/com/airwar/android/ui/GameOverActivity.java) |
  [`LeaderboardActivity`](../../../app/src/main/java/com/airwar/android/ui/LeaderboardActivity.java)

- 主题样式：
  [`ui2_colors.xml`](../../../app/src/main/res/values/ui2_colors.xml) |
  [`ui2_dimens.xml`](../../../app/src/main/res/values/ui2_dimens.xml) |
  [`ui2_styles.xml`](../../../app/src/main/res/values/ui2_styles.xml)

- 构建与 CI：
  [`build.gradle`](../../../build.gradle) |
  [`settings.gradle`](../../../settings.gradle) |
  [`app/build.gradle`](../../../app/build.gradle) |
  [`game-core/build.gradle`](../../../game-core/build.gradle) |
  [`gradle-wrapper.properties`](../../../gradle/wrapper/gradle-wrapper.properties) |
  [`build-apk.yml`](../../../.github/workflows/build-apk.yml)

### 16.1 改排行榜（展示逻辑、排序规则、头像）

1. `app/src/main/java/com/airwar/android/ui/LeaderboardActivity.java`
2. `app/src/main/java/com/airwar/android/storage/AndroidScoreDao.java`
3. `app/src/main/java/com/airwar/android/storage/ScoreCsvSerializer.java`
4. `app/src/main/java/com/airwar/android/storage/GameScore.java`
5. `app/src/main/java/com/airwar/android/ui/PilotAvatarRegistry.java`
6. `app/src/main/res/layout/activity_leaderboard.xml`
7. `app/src/main/res/layout/comp_rank_row.xml`
8. `app/src/main/res/values/strings.xml`

关注点：
- 排序在 DAO 中（分数降序 + 时长升序）。
- 难度筛选在 `readScoresSortedByDifficulty`。
- Top3 卡片由 `LeaderboardActivity.createTopRankCard` 动态创建。

### 16.2 改结算页（昵称、头像、提交行为）

1. `app/src/main/java/com/airwar/android/ui/GameOverActivity.java`
2. `app/src/main/java/com/airwar/android/ui/RandomPilotNameGenerator.java`
3. `app/src/main/java/com/airwar/android/ui/PilotAvatarRegistry.java`
4. `app/src/main/java/com/airwar/android/storage/AndroidScoreDao.java`
5. `app/src/main/res/layout/activity_game_over.xml`
6. `app/src/main/res/values/strings.xml`

关注点：
- 提交按钮中完成写入和跳转。
- 随机名与同毫秒去重在 `RandomPilotNameGenerator`。
- 头像选中态边框使用 `ui2_avatar_border_selected.xml`。

### 16.3 改菜单与难度参数展示

1. `app/src/main/java/com/airwar/android/ui/MenuActivity.java`
2. `game-core/src/main/java/com/airwar/core/difficulty/DifficultyConfig.java`
3. `game-core/src/main/java/com/airwar/core/difficulty/DifficultyLevel.java`
4. `app/src/main/res/layout/activity_menu.xml`
5. `app/src/main/res/values/strings.xml`

关注点：
- 菜单显示参数来自 `DifficultyConfig.of(level)`。
- 实际战斗参数也来自同一配置，UI 与逻辑可保持一致。

### 16.4 改游戏主循环/帧率/输入

1. `app/src/main/java/com/airwar/android/view/GameRenderThread.java`
2. `app/src/main/java/com/airwar/android/view/GameSurfaceView.java`
3. `game-core/src/main/java/com/airwar/core/engine/GameEngine.java`
4. `game-core/src/main/java/com/airwar/core/config/GameConstants.java`

关注点：
- Tick 周期在 `GameRenderThread.TICK_MS`。
- 输入坐标映射在 `toLogicalX/toLogicalY`。
- 逻辑相位顺序在 `GameEngine.tick`。

### 16.5 改战斗规则（碰撞、得分、掉落、Boss）

1. `game-core/src/main/java/com/airwar/core/engine/GameEngine.java`
2. `game-core/src/main/java/com/airwar/core/spawn/EnemySpawner.java`
3. `game-core/src/main/java/com/airwar/core/difficulty/DifficultyConfig.java`
4. `game-core/src/test/java/com/airwar/core/engine/GameEngineBattleSnapshotTest.java`
5. `game-core/src/test/java/com/airwar/core/difficulty/BossSpawnThresholdTest.java`

关注点：
- 碰撞、得分、道具处理都在 `GameEngine`。
- Boss 阈值桶化在 `EnemySpawner.update(score)`。

### 16.6 改射击模式/道具增强时效

1. `game-core/src/main/java/com/airwar/core/strategy/ShootStrategy.java`
2. `game-core/src/main/java/com/airwar/core/strategy/StraightShootStrategy.java`
3. `game-core/src/main/java/com/airwar/core/strategy/ScatterShootStrategy.java`
4. `game-core/src/main/java/com/airwar/core/strategy/CircularShootStrategy.java`
5. `game-core/src/main/java/com/airwar/core/effect/TimedEffect.java`
6. `game-core/src/main/java/com/airwar/core/effect/EffectScheduler.java`
7. `game-core/src/main/java/com/airwar/core/engine/GameEngine.java`
8. `game-core/src/test/java/com/airwar/core/strategy/ShootStrategyTest.java`
9. `game-core/src/test/java/com/airwar/core/parity/GameplayParityTest.java`

关注点：
- 增强态开启/回滚通过 `TimedEffect` 回调实现。
- 策略入口在 `HeroAircraft.setShootStrategy(...)`。

### 16.7 改音频（BGM 切换、SFX 触发、静音）

1. `app/src/main/java/com/airwar/android/audio/AndroidAudioManager.java`
2. `app/src/main/java/com/airwar/android/view/GameSurfaceView.java`
3. `app/src/main/res/raw/*.wav`
4. `app/src/test/java/com/airwar/android/audio/AndroidAudioManagerStateTest.java`

关注点：
- BGM 切换触发点在 `GameSurfaceView.onFrame` 的 boss 状态判断。
- SFX 触发由事件计数差值驱动。

### 16.8 改 HUD（分数/生命显示）

1. `app/src/main/res/layout/view_hud_overlay.xml`
2. `app/src/main/java/com/airwar/android/ui/GameActivity.java`
3. `app/src/main/res/values/strings.xml`
4. `app/src/main/res/drawable/ui_panel.xml`

关注点：
- 文案模板在 `hud_score_value`、`hud_hp_value`。
- 实时更新在 `GameActivity.updateHud(...)`。

### 16.9 改页面导航（流程变更）

1. `app/src/main/AndroidManifest.xml`
2. `app/src/main/java/com/airwar/android/ui/LauncherActivity.java`
3. `app/src/main/java/com/airwar/android/ui/MenuActivity.java`
4. `app/src/main/java/com/airwar/android/ui/GameActivity.java`
5. `app/src/main/java/com/airwar/android/ui/GameOverActivity.java`
6. `app/src/main/java/com/airwar/android/ui/LeaderboardActivity.java`
7. `app/src/androidTest/java/com/airwar/android/ui/MenuToGameFlowTest.java`

关注点：
- 入口由 Manifest 决定；页面流转由各 Activity 中 `startActivity` 决定。

### 16.10 改主题样式（颜色、尺寸、按钮视觉）

1. `app/src/main/res/values/ui2_colors.xml`
2. `app/src/main/res/values/ui2_dimens.xml`
3. `app/src/main/res/values/ui2_styles.xml`
4. `app/src/main/res/drawable/ui2_*.xml`
5. 各页面布局：`activity_menu.xml`、`activity_game_over.xml`、`activity_leaderboard.xml`

关注点：
- 当前存在 `ui_*` 与 `ui2_*` 并行，改动时注意同名旧资源影响。

### 16.11 改构建/依赖/SDK 版本

1. `build.gradle`（根 AGP 版本）
2. `settings.gradle`（模块）
3. `app/build.gradle`（App SDK 与依赖）
4. `game-core/build.gradle`（Java 与测试平台）
5. `gradle/wrapper/gradle-wrapper.properties`（Gradle 版本）
6. `.github/workflows/build-apk.yml`（CI 构建环境）

关注点：
- 本地和 CI 版本需要同步验证，避免“本地能过、CI 失败”。

### 16.12 改测试/补测试

1. 先改对应生产代码目录（`app` 或 `game-core`）
2. 然后补对应测试：
   - 逻辑规则 -> `game-core/src/test`
   - Android 纯逻辑 -> `app/src/test`
   - 页面流程 -> `app/src/androidTest`

建议命令：
- `./gradlew :game-core:test`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:connectedDebugAndroidTest`
