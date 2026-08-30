# 权威来源

研究快照：2026-08-30。产品范围永远以仓库文档为准；API 语义以官方文档为准。依赖升级前重新查看发布说明，不把本快照当永久“最新版”。

## 仓库依据

- `docs/requirement.md`：产品行为、P-1/P0/P1 范围、两步预算、隐私与验收。
- `docs/architecture.md`：Android 原生选型、三模块边界、Extensions 回退和 P1 创意管线。
- `gradle/libs.versions.toml`、`androidApp/build.gradle.kts`、`coach/build.gradle.kts`、`ExplainApi/ExplainApi.csproj`：实际编译基线。

冲突时，产品/版本取舍以 requirement 为准，技术拆分以 architecture 为准，API 行为以对应官方文档为准。代码和网络文章不能静默覆盖它们。

## Android / CameraX 官方文档

- CameraX 发布说明：<https://developer.android.com/jetpack/androidx/releases/camera>
- Extensions API 与 1.6+ 兼容提醒：<https://developer.android.com/media/camera/camerax/extensions-api>
- `ExtensionsManager.isImageAnalysisSupported`：<https://developer.android.com/reference/androidx/camera/extensions/ExtensionsManager>
- `ExtensionSessionConfig` 不支持 ImageAnalysis：<https://developer.android.com/reference/androidx/camera/extensions/ExtensionSessionConfig>
- ImageAnalysis 背压与关闭规则：<https://developer.android.com/media/camera/camerax/analyze>
- CameraX 配置、对焦与曝光：<https://developer.android.com/media/camera/camerax/configuration>
- `CameraInfo` 的 Zoom/Exposure/`intrinsicZoomRatio`：<https://developer.android.com/reference/androidx/camera/core/CameraInfo>
- `CameraProvider.availableCameraInfos`：<https://developer.android.com/reference/androidx/camera/core/CameraProvider>
- `MlKitAnalyzer` 与 PreviewView 坐标：<https://developer.android.com/media/camera/camerax/mlkitanalyzer>
- MediaStore 共享媒体、`IS_PENDING` 与 `RELATIVE_PATH`：<https://developer.android.com/training/data-storage/shared/media>
- SharedPreferences：<https://developer.android.com/training/data-storage/shared-preferences>
- `RenderEffect`（API 31+）：<https://developer.android.com/reference/android/graphics/RenderEffect>
- `ColorMatrixColorFilter`：<https://developer.android.com/reference/android/graphics/ColorMatrixColorFilter>
- CameraX `VideoCapture` 架构与用例组合：<https://developer.android.com/media/camera/camerax/video-capture>
- Android Motion Photo Format 1.0：<https://developer.android.com/media/platform/motion-photo-format>
- Media3 Transformer 裁剪、去音轨与导出：<https://developer.android.com/media/media3/transformer/transformations>

快照结论：

- 2026-08-30 官方稳定 CameraX 是 1.6.2，仓库固定 1.6.1。普通编码任务不自动升级；升级任务需跑全量 Android 测试和小米 14 Pro 回归。
- 官方说明 2026-11-01 起部分设备会停止向 CameraX 1.5 及更早版本提供 Extensions，因此架构的 1.6+ 下限有依据。
- CameraX 1.7 仍为 alpha，并弃用旧 extension selector；仓库不能追 alpha，也不能改用不支持 ImageAnalysis 的 `ExtensionSessionConfig` 来消除弃用。
- Android 官方对新存储更推荐 DataStore，但架构已经明确使用私有 SharedPreferences 保存这组小型枚举设置；这不构成无关任务的迁移授权。
- Motion Photo v1 是“主图 + 紧密追加的视频”的单文件格式；Camera XMP 声明 Motion Photo/版本/封面时间，Container XMP 负责定位视频。JPEG writer 应使用 `…MP.JPG` 模式；视频项长度必须等于真实追加字节数。
- CameraX 官方说明 `VideoCapture` 能否与其他用例组合取决于设备能力，因此本项目的四用例必须先探测并保留普通 JPEG 回退。Media3 Transformer 可按时间裁剪并显式移除音轨，但具体编码、时长和 HyperOS 相册播放仍需小米 14 Pro 真机验证。

## ML Kit 官方文档

- Face detection Android 与实时性能选项：<https://developers.google.com/ml-kit/vision/face-detection/android>
- Pose detection Android、STREAM_MODE 与 CameraX 背压：<https://developers.google.com/ml-kit/vision/pose-detection/android>
- ML Kit 条款：<https://developers.google.com/ml-kit/terms>

Face detection 是检测，不是身份识别。官方实时建议包括 FAST、减少可选特征以及 CameraX `KEEP_ONLY_LATEST`；contour 与 tracking 不应同时启用。Pose 当前仓库依赖仍为 beta，升级需要重新验证信号阈值和性能。

## ASP.NET Core 官方文档

- ASP.NET Core 10 API 方案选择：<https://learn.microsoft.com/en-us/aspnet/core/fundamentals/apis?view=aspnetcore-10.0>
- .NET 10 Minimal API 教程：<https://learn.microsoft.com/en-us/aspnet/core/tutorials/min-web-api?view=aspnetcore-10.0>

微软建议新 HTTP API 从 Minimal APIs 开始；只有出现模型绑定扩展、复杂验证、OData 等真实需求时才考虑 Controllers。本项目的单端点补充服务不需要这些扩展。
