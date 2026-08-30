# 拍照教练

面向手机拍照新手的拍中教练。第一版只验收小米 14 Pro，采用 Android 取景器 + 端侧口令引擎 + 可选讲解 API；其他机型后期再立项。

| 目录 | 职责 |
| --- | --- |
| [docs/requirement.md](docs/requirement.md) | 产品需求 |
| [docs/architecture.md](docs/architecture.md) | 技术架构 |
| `coach/` | 场景口令 JSON 与匹配（无 Android UI） |
| `androidApp/` | CameraX / Compose 取景器 |
| `ExplainApi/` | .NET 10 Minimal API「再讲细」 |

## 构建

- 口令引擎：`./gradlew :coach:test`
- Android 单测：`./gradlew :androidApp:testDebugUnitTest`（需要 Android SDK，`local.properties` 里写 `sdk.dir`）
- 讲解 API：`dotnet test ExplainApi.sln`

Android `minSdk 26`，CameraX `1.6.1`。成片写入 `DCIM/拍照教练`。
