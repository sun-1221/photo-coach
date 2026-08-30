# Android 取景器

改 `androidApp/` 的 CameraX、Compose、权限、相机设置、TTS 或 MediaStore 时读本文件；涉及 Face/Pose 再读 `mlkit-signals.md`，涉及风格/连拍/编辑再读 `p1-creative.md`。

## 工程基线

- 当前工程是 Kotlin + Jetpack Compose，`minSdk 26`、`compileSdk/targetSdk 36`、Java 17；实际版本以 `gradle/libs.versions.toml` 为准。
- CameraX 各 artifact 必须使用同一 release 版本。当前目录已固定版本；非升级任务不追随网页上的更新版本。
- 现有预览使用 Compose 中的 `AndroidView(PreviewView)`。不要仅因出现新的 Compose 相机组件就迁移预览实现。
- 第一版只验收小米 14 Pro；标准 API 能力探测和安全回退继续保留，但不得为其他机型新增厂商分支或宣称兼容。

## 绑定与分析

默认（包括 P-1 和关闭 Live 的 P1）同时绑定 `Preview + ImageAnalysis + ImageCapture`，并让 `ProcessCameraProvider` 管理生命周期。三用例使用同一 4:3 或 16:9 `AspectRatioStrategy`；UI 裁切不能伪造成片画幅。

只有用户显式开启已批准的 P1 无声 Live 且硬件组合支持时，才绑定 `Preview + ImageAnalysis + ImageCapture + VideoCapture`。四用例继续共享兼容的分辨率/画幅约束并保留实时指导；Live 强制普通 Photo、禁止 Extensions、不开启音频。四用例绑定、编码器、空间或运行时失败时关闭 Live 状态并可见回退普通 JPEG，不能卸载 Analysis 或显示假 Live。细节读 `p1-creative.md`。

`ImageAnalysis` 使用 `STRATEGY_KEEP_ONLY_LATEST`。自写 analyzer 时：

1. 只从 `ImageProxy` 读取帧；不要关闭包装的 `Media.Image`。
2. 异步检测完成后关闭 `ImageProxy`，异常路径也必须关闭。
3. 不在 analyzer/ML Kit 执行器做长耗时口令、图像导出或评分。
4. 使用 `MlKitAnalyzer` 时让它负责正常关闭路径；外层只在移交前异常时关闭，避免双关。

## Extensions 与标准回退

P-1 的实时指导优先于扩展成片：

1. 对请求的 BOKEH / HDR / NIGHT 先查 `isExtensionAvailable`。
2. 再查 `isImageAnalysisSupported`。
3. 两者为真才用扩展选择器绑定 Preview + Analysis + Capture。
4. 任一查询为假、绑定抛错或运行期失效时，立即回到标准后摄三用例并给可见说明；指导继续。
5. 不开启 `FACE_RETOUCH`。

显式无声 Live 与 Extensions 互斥；进入 Live 前先回标准后摄，退出或回退后恢复标准三用例。不得用扩展效果换取四用例，也不得让 Live 失败破坏普通拍照路径。

CameraX 1.6 的 `ExtensionSessionConfig` 明确不支持 `ImageAnalysis`，不能作为本项目默认扩展绑定。即使未来 API 弃用旧选择器，也只能在目标依赖升级且新方案能够保留分析时迁移；不能为了消除弃用警告卸掉分析。

## 能力驱动控制

- 每次镜头、画幅或模式重绑后重新读取 `ZoomState`、`ExposureState` 和可用扩展。旧 EV 越界则夹紧；旧模式不可用则回标准 Photo；UI 锁定状态恢复为未锁。
- 点按用 `FocusMeteringAction` 设置 AF/AE 并显示成功/失败。长按关闭自动取消以表达 AE/AF 锁定，提供显式 `cancelFocusAndMetering`；重绑后不能保留假的“已锁定”。
- EV 范围和步长来自当前 `ExposureState`；不支持时不显示假滑杆。用户拖动后锁定本次会话选择。
- 双指连续缩放限制在当前 `ZoomState`。快捷焦段从 `availableCameraInfos`、Camera2 `LENS_INFO_AVAILABLE_FOCAL_LENGTHS` 和 CameraX `intrinsicZoomRatio` 构造候选，去掉无元数据和重复项。
- `intrinsicZoomRatio` 只是近似视角；按钮最终必须经小米 14 Pro 预览、成片视角和 EXIF 标定。不要固定写 2x，也不要把连续数码缩放生成光学按钮。
- “对焦优先”映射 `CAPTURE_MODE_MAXIMIZE_QUALITY`，“拍摄优先”映射 `CAPTURE_MODE_MINIMIZE_LATENCY`。两者都不能停分析或因未完成指导锁快门。
- 倒计时只延迟一次用户捕获，支持关/3 秒/10 秒；再次按屏幕或音量键快门取消。P-1 不自动拍、不自动连拍。

## 权限、保存与恢复

- 端侧人脸/身体关键点说明必须先于系统 `CAMERA` 权限弹窗。拒绝、永久拒绝、相机被占用或绑定失败都要有文字和恢复路径，不能停在黑预览。
- P-1 与已批准的无声 Live 都只申请 `CAMERA`；绝不申请 `RECORD_AUDIO`、定位或读取整本相册，Live 录制不启用音轨。
- 捕获先得到临时 JPEG，再由 `ContentResolver.insert` 发布到 `DCIM/拍照教练`。Android 10+ 使用 `RELATIVE_PATH` 与 `IS_PENDING`；发布失败清理未完成行并保留可重试错误。
- P1 每次捕获使用不可复用的 `captureId`，保存 journal 在不可逆阶段前后落盘；已成功阶段不回滚，只重试失败阶段。普通路径默认发布原片并私有保存配方；Motion Photo 路径只发布一个主文件，组装失败回退普通 JPEG且不得重复发布。安全 EXIF、兼容 SDR 副本、恢复清理和拍后操作读 `p1-creative.md`。
- 最近照片只使用本次插入返回的 URI 并交给系统查看器，不扫描相册。
- 相机设置按架构保存在私有 `SharedPreferences`；枚举旧值非法时回默认。重置只覆盖相机设置键，不撤回分析同意、不删除照片。不要在无关任务中迁移到 DataStore。

## UI 与 TTS

- 竖屏为预览区 + 稳定底部操作区，横屏改右侧栏；动作卡一次只显示一个且最多两行。
- 拍摄者动作卡始终可见。被拍者中文语音和字幕是两个独立、持久化开关。
- `TextToSpeech` 只播 `subject`；重复播报受节流。TTS 未就绪、系统静音或播放失败时，字幕和快门仍可用。
- 前摄、只拍景、代拍和“再讲细”属于完整 P0，不得进入 P-1 UI。P1 创意层按独立开关和 reference 落地；无声 Live 默认关闭，失败后 UI 必须回到普通照片状态。
