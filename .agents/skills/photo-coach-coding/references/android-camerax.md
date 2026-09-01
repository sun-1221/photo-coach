# Android 取景器执行检查

改 `androidApp/` 的 CameraX、Compose、权限、相机设置、TTS 或 MediaStore 时读本文件。它是代码执行检查表，不是产品或架构权威。

## 强制规范集合

- 总是先读 `docs/requirement.md` 和 `docs/architecture.md`。
- 相机、取景器或端侧感知：读 `docs/architecture/camera-and-perception.md`。
- 用户交互、布局、控件、TTS、文案或保存反馈：读 `docs/requirements/interaction-guidance-and-scenarios.md`。
- 具体 FR、组件、交付或证据：读 `docs/traceability/requirements-matrix.md`。
- UX、质量或目标机结论：读 `docs/acceptance/acceptance-plan.md`。
- P1 保存、`captureId`、MediaStore 或 Motion Photo：再读 `docs/requirements/p1-creative.md` 与 `docs/architecture/creative-and-storage.md`。
- 任务命中 `CP-*` 时读 `docs/traceability/decisions-and-conflicts.md`，不得自行裁决。

## 工程与绑定

- 当前工程基线以版本目录和 Gradle 文件为准；CameraX artifacts 保持同一 release。非升级任务不追网页最新版，也不因新组件改写现有 `PreviewView` 管线。
- 普通路径同时绑定 `Preview + ImageAnalysis + ImageCapture`，使用当前架构定义的共同 ViewPort/画幅策略。只有当前规范批准的无声 Motion Photo 路径才尝试四用例组合。
- `ImageAnalysis` 使用 `STRATEGY_KEEP_ONLY_LATEST`。自写 analyzer 只关闭 `ImageProxy`，并覆盖正常、异常与异步完成路径；不要关闭包装的 `Media.Image`，不要在分析执行器做口令、导出或评分。
- Extensions 必须同时满足可用性和 `ImageAnalysis` 共存条件；绑定或运行失败回标准三用例并保留指导。不要用不支持分析的会话配置换取扩展成片。
- Live 与 Extensions 的组合、HD/SD 预检、JPEG 格式、音轨、XMP 与 fallback 以两个 P1 权威附件为准；失败不能留下假 Live 或重复发布。

## 能力、状态与恢复

- 镜头、画幅或模式重绑后重新读取 Zoom、EV、焦段、扩展和 3A 状态；旧值夹紧或回退，UI 不保留假锁定。
- 快捷焦段来自标准能力和当前目标机标定，不硬编码 2x、不读取厂商传感器 ID、不把数码裁切命名为光学镜头。
- 点按 AF/AE、长按锁定、显式解除、捕获偏好、倒计时和音量键必须按当前交互与相机专题实现；除相机不可用或捕获中外，不让指导、分数、网络或阈值锁快门。
- 权限说明先于系统 `CAMERA` 弹窗；拒绝、相机占用、绑定和保存失败都有文字与恢复路径，不停在黑预览。
- 权限、MediaStore、最近照片、设置持久化与恢复只按当前规范要求操作；不要在无关任务中新增权限、扫描整本相册或迁移存储。
- 语音开启时只播当前稳定的 `shooter` 或 `subject` 口令；被拍者字幕是独立开关。TTS 失败不得阻断动作卡、字幕或快门，也不得擅自改变用户开关。

## 验证

- 代码层优先运行 `.\gradlew.bat :androidApp:testDebugUnitTest`；仪器测试只在环境与任务允许时运行。
- 根据需求追踪矩阵记录具体 FR 的自动化、instrumented 与 device 状态；不要用测试类存在代替执行结果。
- 没有当前小米 14 Pro 证据时，CameraX 绑定、Extensions、3A、焦段/EV、真实画幅、按键、MediaStore、颜色、热量与 Motion Photo 播放等设备项保持 `NotRun`。
