# API 与研究来源边界

只有核对 API 语义、依赖升级或维护研究依据时读本文件。它不定义产品范围、交付状态或验证结果。

## 仓库权威与非权威材料

| 材料 | 可以决定 | 不能决定 |
| --- | --- | --- |
| `docs/requirement.md` + 相关 `docs/requirements/`、`docs/acceptance/`、`docs/traceability/` 附件 | 当前产品 Scope、FR/UX、验收与冲突控制 | 代码实际依赖版本 |
| `docs/architecture.md` + 相关 `docs/architecture/` 专题 | 当前模块、数据流、技术选择与回退 | 静默扩大产品范围 |
| Gradle、版本目录与 `.csproj` | 当前编译依赖事实 | 产品批准或真机通过 |
| `docs/research/` | 市场、交互和官方依据 | 扩大 Scope、证明 Delivery、把 Verification 改为 Pass |
| `docs/history/` | 迁移追溯 | 当前产品、架构、验收或状态 |
| 本文件的官方链接 | 对应 API 的公开语义 | 仓库 Phase、产品取舍或已验证状态 |

依赖升级任务必须重新核对官方发布说明与仓库实际版本，并按当前架构和验收要求验证；普通修复不追随网页“最新版”。

## Android / CameraX

- CameraX 发布说明：<https://developer.android.com/jetpack/androidx/releases/camera>
- Extensions API：<https://developer.android.com/media/camera/camerax/extensions-api>
- `ExtensionsManager.isImageAnalysisSupported`：<https://developer.android.com/reference/androidx/camera/extensions/ExtensionsManager>
- `ExtensionSessionConfig`：<https://developer.android.com/reference/androidx/camera/extensions/ExtensionSessionConfig>
- ImageAnalysis：<https://developer.android.com/media/camera/camerax/analyze>
- CameraX 配置、对焦与曝光：<https://developer.android.com/media/camera/camerax/configuration>
- `CameraInfo`：<https://developer.android.com/reference/androidx/camera/core/CameraInfo>
- `CameraProvider.availableCameraInfos`：<https://developer.android.com/reference/androidx/camera/core/CameraProvider>
- `MlKitAnalyzer`：<https://developer.android.com/media/camera/camerax/mlkitanalyzer>
- MediaStore：<https://developer.android.com/training/data-storage/shared/media>
- SharedPreferences：<https://developer.android.com/training/data-storage/shared-preferences>
- `RenderEffect`：<https://developer.android.com/reference/android/graphics/RenderEffect>
- `ColorMatrixColorFilter`：<https://developer.android.com/reference/android/graphics/ColorMatrixColorFilter>
- CameraX VideoCapture：<https://developer.android.com/media/camera/camerax/video-capture>
- Android Motion Photo Format：<https://developer.android.com/media/platform/motion-photo-format>
- Media3 Transformer：<https://developer.android.com/media/media3/transformer/transformations>

## ML Kit

- Face detection Android：<https://developers.google.com/ml-kit/vision/face-detection/android>
- Pose detection Android：<https://developers.google.com/ml-kit/vision/pose-detection/android>
- ML Kit 条款：<https://developers.google.com/ml-kit/terms>

## ASP.NET Core

- ASP.NET Core API 方案：<https://learn.microsoft.com/en-us/aspnet/core/fundamentals/apis?view=aspnetcore-10.0>
- .NET Minimal API：<https://learn.microsoft.com/en-us/aspnet/core/tutorials/min-web-api?view=aspnetcore-10.0>

使用官方资料得到的结论只记为 API/平台依据；是否进入产品、代码是否交付、自动化或真机是否通过，仍回到当前仓库规范与实际证据分别判断。
