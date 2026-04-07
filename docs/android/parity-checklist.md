# Android 迁移一致性检查清单

## 手动一致性检查

- 难度档位阈值与 `DifficultyConfig` 中的配置一致。
- Boss 生成阈值行为符合确定性分数推进规则。
- 超级子弹效果生效时英雄射击模式切换为环形，时效结束后恢复为直线。
- 触控输入能够更新运行时快照中的英雄目标坐标。
- 游戏结束后分数会写入 CSV，排行榜按分数降序展示。
- 菜单开始按钮可进入游戏，并携带难度参数。

## 验证命令

- `./gradlew :game-core:test --tests com.airwar.core.parity.GameplayParityTest`
- `./gradlew :game-core:test`
- `./gradlew :app:testDebugUnitTest`（需要 Android SDK）
- `./gradlew :app:assembleDebug`（需要 Android SDK）

## 说明

- App 模块验证依赖可用的 Android SDK（`ANDROID_HOME` 或 `local.properties` 中的 `sdk.dir`）。
- 在当前缺少 SDK 的环境中，以 `game-core` 测试作为基础 CI 可信信号。
