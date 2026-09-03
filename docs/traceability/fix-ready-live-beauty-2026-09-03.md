# Ready、Live 与美颜入口修复记录（2026-09-03）

## Scope / Delivery

- InScope：P-1 Ready 最低质量状态及原因刷新、普通相机会话可靠性。
- ApprovedSeparate：已有 P1 Live 与自然上镜入口/处理状态。未增加权限、网络、检测器、机型、P0 能力或美颜强度。
- 本次是已定位代码缺陷的修复，不代表用户反馈已全部在小米真机消除。尤其 HyperOS Live 识别/播放和真实人物美颜观感仍未复验。

## Changed

1. `GuidanceSession` 的 Ready 按 600ms/三帧刷新最低质量及 `ReadinessIssue`；改善和恶化都更新，不因步骤预算用完而丢失具体说明，不新增编号步骤或 TTS。已有暗脸和明显人物运动信号也阻止确认最低质量；原比例/水平等阈值未放宽，快门不锁。
2. 取景器入口明确为“美颜·风格”，启用后为“美颜·自然/柔和”。菜单持续显示互斥原因，提供显式关闭 Live／选择普通模式的操作；不暗改模式或自动开启美颜。
3. `BeautyPreviewState` 来自实际渲染门禁。等待人脸、多人、缺关键点、旧帧、无效变换和热暂停均与处理中区分；默认保存反馈明确“原片已保存；美颜效果需另存副本”。没有扩大磨皮强度或用假时间戳延长旧蒙版。
4. `LiveRecordingSession` 隔离旧录制回调；缓存从编码器 Start 事件开始计算，快门后停止期限不再额外叠加 JPEG 保存耗时；Finalize 有错误但无 cause 时仍处理为失败。
5. `CameraBinder` 只解绑自己登记的 use cases，不再使用进程级 unbindAll，防止旧实例释放影响新相机。具体目标机重绑故障是否全部消除仍需验证。

## Validated / Verification

- `gradlew.bat :coach:test :androidApp:testDebugUnitTest :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest :androidApp:lintDebug --offline --console=plain`：成功。
- JVM：coach 59、Android 139，共 198，0 失败／错误／跳过。新增 4 个指导用例、3 个录制会话用例、3 个美颜实际状态用例。
- lint：0 errors、4 warnings；构建没有改变依赖版本。首次编译发现跨模块可空属性及测试导入问题，已修正后重新通过。
- `git diff --check`：通过。
- 模拟器 UI 仪器：`ViewfinderScreenTest` 21/21 Pass（192.248s），包括美颜可见入口、自然档选择、互斥原因、显式模式操作和中性 Ready 的具体建议。
- 模拟器核心仪器：BeautyRenderer 6、BeautyState 3、BeautyCameraBinding 1、MotionPhotoFile 1、PublishedAssetVerifier 1、BoundedImageDecoder 3，共 15/15 Pass（21.169s）。覆盖 GPU/CPU 合成纹理、三用例捕获/释放、保存格式；不等于真实人物、Live 相册或小米重绑通过。
- 仪器共 36/36 Pass。设备为现有 Medium_Phone_API_36.1 的只读、无窗口实例 emulator-5556，Android 16，fingerprint `google/sdk_gphone64_x86_64/emu64xa:16/BE4B.251210.005/14574095:user/release-keys`。测试后关闭此实例，不保存快照。核心原始输出在忽略的 `androidApp/build/device-fix-20260903/instrumented-core-emulator.log`。
- 新 APK 版本仍为 1.0.0/code 1，以 SHA-256 区分此次新构建，不混用此前安装包：
  - App：`5C2C1495832302A321918099A934993851CC0185FD762D7B339E9B9680AFE046`
  - Test：`C2D59DD79AF4E387A025D607E782E8BDECB1A7D463A8808E1B3163A7D223AC70`

仪器命令（先安装上述两个 APK 到指定模拟器）：

```powershell
adb -s emulator-5556 shell am instrument -w -r -e class com.photocoach.app.ui.viewfinder.ViewfinderScreenTest com.photocoach.app.test/androidx.test.runner.AndroidJUnitRunner
adb -s emulator-5556 shell am instrument -w -r -e timeout_msec 45000 -e class com.photocoach.app.beauty.BeautyRendererTest,com.photocoach.app.beauty.BeautyStateTest,com.photocoach.app.beauty.BeautyCameraBindingTest,com.photocoach.app.camera.MotionPhotoFileInstrumentedTest,com.photocoach.app.camera.PublishedAssetVerifierInstrumentedTest,com.photocoach.app.creative.BoundedImageDecoderInstrumentedTest com.photocoach.app.test/androidx.test.runner.AndroidJUnitRunner
```

代码按用户此前要求分批本地提交：`ae6184b` 指导状态，`d92bff1` Android 美颜入口/状态与 Live 会话隔离；未推送远端。新 APK 包含这两批代码。

## 已授权旧样本核验（不是新包真机结果）

只读取此前已拉取的本 App Live 与用户授权原生参照，不读取整本相册，不上传或把样张加入 Git。App 样本 XMP 视频尾部 2,036,338 bytes，本地 FFmpeg 完整解码成功：115 帧、约 3.86 秒、H.264 720×960、无音轨。原生参照也可完整解码；两者的时长/旋转/颜色元数据并不完全相同。

这只能排除该 App 样本的“视频为空或完全无法解码”，不能证明 HyperOS 相册识别/播放。未伪造小米 MakerNote、未接私有相册 API。临时提取的视频在解码后清理；原样本保留在忽略的 build 目录，不进 Git。

## NotRun / Residual risk

- 本轮多次 `adb devices -l` 未检测到小米 14 Pro；随后列表中的 emulator-5556 只是测试模拟器。尚未安装本轮新 APK 到手机。
- 新包目标机 Ready 反复跳变、当前建议有效性、普通/Live 捕获保存、失败恢复及 HyperOS 相册播放：NotRun。历史用户失败反馈仍然有效，不能以格式/单测通过关闭。
- 新包目标机美颜菜单触控、真实人脸蒙版对齐、预览与另存观感、原片对照、热/延迟/长期稳定性：NotRun。新增状态可定位无效果发生在哪个门禁，但不证明真实人物效果已修复或校准。
- 完整 100 张保存、20 分钟负载、杀进程恢复、.NET 和 P-1 产品对照实验未运行。CP-01/03/04/05 未决，不由本次界面修复裁决。

## Rules

使用 photo-coach-coding，完整读取 requirement.md、architecture.md；interaction-guidance-and-scenarios、p1-creative、p1-beauty；acceptance-plan、beauty-validation；requirements-matrix、decisions-and-conflicts；camera-and-perception、creative-and-storage、guidance-and-explain、beauty；执行 references：android-camerax、mlkit-signals、coach-engine、p1-creative、self-check。

Skill 的原片优先、默认关闭、模式互斥和分层验证要求影响了本次实现与报告：没有通过自动改模式/双保存来制造“已生效”，没有把 JVM 或模拟器结果升级为目标机/产品验收。
