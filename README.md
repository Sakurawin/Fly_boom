# AircraftWar Android

一个基于 Java 的 Android 竖屏飞机大战迁移项目。核心目标是将原 PC 版本的玩法规则拆分为可复用的纯逻辑内核，并通过 Android 原生 UI/渲染层承载。

## 项目目标

- 采用双模块架构：`game-core`（纯逻辑）+ `app`（Android 适配）。
- 保持关键玩法规则可测试、可复现（固定 tick、难度参数、Boss 阈值、时效效果回滚）。
- 提供完整移动端基础流程：启动页、菜单页、游戏页、结算页、排行榜。
- 支持本地 CSV 成绩持久化与读取排序。

## 技术栈

- 语言：Java 17
- 构建：Gradle 8.7（Wrapper）
- Android：AGP 8.x + AndroidX + Material
- 渲染：`SurfaceView` + 独立渲染线程
- 测试：JUnit 5（`game-core`）、JUnit 4 + Espresso（`app`）

## 目录结构（tree）

以下为当前仓库中 Android 迁移相关核心目录（已过滤旧 PC 端资源目录与构建产物）：

```text
app/src/main
├── AndroidManifest.xml
├── java/com/airwar/android
│   ├── audio/AndroidAudioManager.java
│   ├── storage/{AndroidScoreDao,GameScore,ScoreCsvSerializer}.java
│   ├── ui/{LauncherActivity,MenuActivity,GameActivity,GameOverActivity,LeaderboardActivity}.java
│   └── view/{GameSurfaceView,GameRenderThread,SpriteRepository}.java
└── res
    ├── layout/{activity_launcher,activity_menu,activity_game,activity_game_over,activity_leaderboard,view_hud_overlay}.xml
    └── values/{strings,colors}.xml

app/src/test/java/com/airwar/android
├── audio/AndroidAudioManagerStateTest.java
└── storage/ScoreCsvSerializerTest.java

app/src/androidTest/java/com/airwar/android/ui
├── GameActivityLaunchTest.java
└── MenuToGameFlowTest.java

game-core/src/main/java/com/airwar/core
├── config/GameConstants.java
├── difficulty/{DifficultyLevel,DifficultyConfig}.java
├── effect/{TimedEffect,EffectScheduler}.java
├── engine/{GameEngine,GameStateSnapshot}.java
├── model/**
├── spawn/EnemySpawner.java
└── strategy/{ShootStrategy,StraightShootStrategy,ScatterShootStrategy,CircularShootStrategy}.java

game-core/src/test/java/com/airwar/core
├── difficulty/BossSpawnThresholdTest.java
├── effect/EffectSchedulerTest.java
├── engine/GameEngineTickOrderTest.java
├── model/FlyingObjectCollisionTest.java
├── parity/GameplayParityTest.java
└── strategy/ShootStrategyTest.java

docs/android/parity-checklist.md
```

## 各模块职责

### `game-core`（平台无关逻辑层）

用于承载游戏规则与可测试行为，不依赖 Android SDK。

- 固定阶段 Tick 顺序（`input -> update -> collision -> spawn -> cleanup`）。
- 难度参数与 Boss 生成阈值。
- 射击策略与效果调度（含时效回滚）。
- 快照导出（供 UI 渲染层读取）。

### `app`（Android 适配层）

用于承载 Android 生命周期、界面导航、触控输入、渲染线程、音频和本地存储。

- `ui`：Activity 流程与页面组织。
- `view`：`SurfaceView` 渲染循环与 HUD。
- `audio`：BGM/SFX 管理抽象。
- `storage`：CSV 持久化与排行榜读取排序。

## 架构与交互流程

### 1) 页面导航

`LauncherActivity` -> `MenuActivity` -> `GameActivity` -> `GameOverActivity` -> `LeaderboardActivity`

### 2) 运行时交互

- 用户触控由 `GameSurfaceView` 接收。
- 渲染线程 `GameRenderThread` 固定周期调用 `GameEngine.tick(40ms)`。
- `GameEngine` 更新内部状态并输出 `GameStateSnapshot`。
- 渲染层根据快照绘制游戏画面和 HUD。

### 3) 成绩流

- `GameOverActivity` 收集昵称与分数。
- `AndroidScoreDao` 写入 `filesDir/scores.csv`。
- `LeaderboardActivity` 读取并按规则排序显示。

## 本地开发与启动

## 1. 环境准备

- Android Studio（推荐最新稳定版）
- JDK 17
- Android SDK 34（Platform + Build-Tools + Platform-Tools）

## 2. 打开项目

在 Android Studio 中直接打开仓库根目录（包含 `settings.gradle` 的目录）。

若出现 SDK 错误，请在根目录创建 `local.properties`：

```properties
sdk.dir=/path/to/Android/Sdk
```

Windows 示例：

```properties
sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
```

## 3. 运行 App（调试）

- 选择运行配置 `app`
- 选择模拟器或真机
- 点击 Run

命令行方式：

```bash
./gradlew :app:installDebug
```

## 测试与验证

### 可直接运行（不依赖 Android SDK 的部分）

```bash
./gradlew :game-core:test
./gradlew :game-core:test --tests com.airwar.core.parity.GameplayParityTest
```

### 依赖 Android SDK 的部分

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:assembleDebug
```

## APK 打包

## Debug APK

```bash
./gradlew :app:assembleDebug
```

产物路径：

`app/build/outputs/apk/debug/app-debug.apk`

## Release APK

```bash
./gradlew :app:assembleRelease
```

产物路径：

`app/build/outputs/apk/release/app-release.apk`

若用于分发，请在后续补充签名配置（`signingConfigs`）。

## 额外说明

- 仓库中保留了原 PC 端历史源码目录（`src/edu/hitsz/**`）用于对照与迁移参考，但当前 README 只面向 Android 版本。
- 玩法一致性人工检查项见：`docs/android/parity-checklist.md`。
