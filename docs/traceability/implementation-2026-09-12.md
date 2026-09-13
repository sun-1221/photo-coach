# 2026-09-12 新合同实施与验证

**当前结论：第三轮修复已通过[独立复验C3](code-reaudit-2026-09-12-round3.md)，结合C2关闭已发现的8项可修复代码缺口。最终产物与证据以第三轮段及[总控验收记录](completion-2026-09-12.md)为准；O09真实Live仍Blocked，设备、正式研究与产品验收未完成。以下首轮/第二轮/第三轮交付时点的待复验状态保留为历史。**

## 首轮提交快照（C 未通过，保留历史）

基线：`39719dac5b499ff9bba2eb02331df35dcf05d5ae`。工作目录：`E:/Code/Agent/photo-coach`，本地 checkout、单 Writer。依据当前规范、CP-01 用户裁决及[本轮只读审核原文](code-audit-2026-09-12.md)实施，不沿用 9 月 5 日完成结论。

**代码处置：10 个优化组 Fixed、1 个 Blocked、1 个 AlreadySatisfied；这些是源码处置，不是产品验收通过。Live 正常媒体时间映射仍被阻塞，生产路径会保存 JPEG。Instrumented、目标机及全部产品验收仍 NotRun。**

## 范围和变更保护

- Scope：FR-01～14、18～22 InScope；FR-23～38 ApprovedSeparate。FR-15～17 GateLocked，后期 Deferred 不提前。本次不增加权限声明、联网调用、依赖或平台。
- Changed：44 个非文档文件（31 个已跟踪文件修改、13 个新文件），另新增本报告与审核原文归档，共 46 个 Writer 文件。非文档变更包含源码、资源、Gradle 配置、JVM 和 Android 仪器测试。
- 既有 35 个 docs 文件按本轮文档写入前 SHA-256 快照逐一比对，全部保持原样；未覆盖总控的需求、架构、FR 矩阵、研究矩阵和历史证据。审核原文归档来自总控指定 turn 的 AgentMessage 完成事件，时间为 2026-09-12T09:54:24.857Z，只规范化本地文件链接，不把它伪装成修复后复审。
- 无 commit/push、worktree、新任务/子代理、自动化、Skill 修改、设备安装、清数据或设备加热。CameraX 仍为 1.6.1，依赖版本文件未改。

## O-01～O-12 逐项处置

| 对象 | 状态 | 最终行为和源码证据 | 实际验证及保留边界 |
|---|---|---|---|
| O-01，FR-09/19、UX-18 | Fixed | [CameraLocks](../../androidApp/src/main/java/com/photocoach/app/camera/CameraLocks.kt) 与 [CameraLockState](../../androidApp/src/main/java/com/photocoach/app/camera/CameraLockState.kt) 分开 AF/AE 支持、请求、确认、解除和失败。AF 成功不确认 AE；AE 使用 `CONTROL_AE_LOCK`，请求 future 完成后还须新 capture result 回执。旧请求、超时、重绑结果拒绝。扩展模式不假定支持 AE interop。UI 分别显示状态并保留解除操作。 | NewRequirementsCameraTest 覆盖 AF 成功/AE 未确认、帧屏障、解除、旧请求、不支持和超时。JVM Pass；真实 3A/亮度变化和扩展降级 Device NotRun，不能称真实锁定验收通过。3 秒超时只是工程初值。 |
| O-02，FR-09/20/21、UX-22/24 | Fixed | [CameraSettings](../../androidApp/src/main/java/com/photocoach/app/camera/CameraSettings.kt) 改为画质优先/速度优先，保留持久化枚举 `FOCUS` 兼容旧值。删除 `prepareQualityCapture` 与 900ms 额外等待，MainActivity 直接提交 ImageCapture；质量/延迟模式映射保持。 | CameraUserSettingsTest 与编译 Pass；实际快门延迟 Device NotRun。 |
| O-03，FR-04/09/10/21、UX-03/24 | Fixed | [GuidanceSession](../../coach/src/main/kotlin/com/photocoach/coach/GuidanceSession.kt) 拍摄新轮次重置动作预算但不解除手选意图；[AppViewModel](../../androidApp/src/main/java/com/photocoach/app/AppViewModel.kt) 不在新照片解除焦段锁，场景初值会话内仅一次。自动 EV 已满足，保留；手动 EV 继续通过确认回执恢复。研究条件的显式每轮归零另列 O-11。 | GuidanceSessionTest 已修正旧的“拍完解除意图锁”反向断言；既有设置/EV 回归 Pass。物理镜头重绑、进程重启 Device NotRun。 |
| O-04，FR-05/07/08、UX-04/06/17/31 | Fixed | Analyzer 传递分析会话、捕获时间；[FrameSequenceGate](../../coach/src/main/kotlin/com/photocoach/coach/FrameSequenceGate.kt)、VM 和 GuidanceSession 拒绝重复、乱序、旧会话与过期输入，间隔过大重置稳定计数。Pose 保留每项已知证据集，关键点失效不作为改善；指导立即撤销失效 subject 动作。TTS 在 enqueue 前取得 ID、stop 前撤销，主线程分发并核对 VM 动作/轮次/播放代次。 | NewRequirementsGuidanceTest 直接向真实 GuidanceSession 注入重复/旧会话帧及最短展示期内肩部证据丢失；SpeechCompletionIntegrationTest 将生产 SpeechCompletionGate 接入真实 GuidanceSession，验证旧 done/error/stop、stop-before-onStart、重复终态。JVM Pass。Android TTS 引擎线程/首次离线、实际相机时钟来源 Device NotRun。缺少 REALTIME 来源时保守拒绝新鲜性证明。有效期仍待设备校准。 |
| O-05，FR-05/07/32/33、SC/CU/PO/TE | Fixed | [scenes.json](../../coach/src/main/resources/scenes.json) 动态语句去掉无证据的窗户、口袋、地标要求。Pose 身体正对只使用可靠二维肩宽/躯干代理，不使用未校准 Pose Z。PhotoTechniqueEngine 接收拍摄意图，人带景不推荐切长焦；新增 TE-06 机位建议，由用户打开参数面板后作为明确“机位灵感”展示，不推断高度或自动完成。研究指定静态卡独立保留。 | PoseSignalClassifierTest、NewRequirementsGuidanceTest、规则场景 fixture 与 ParameterCoachTest Pass。真实可执行性、每口令四类样本、二维阈值和新文案质量 NotRun。 |
| O-06，FR-12/27/28/36、UX-10/29/34/44 | Fixed | [PendingSourceStore](../../androidApp/src/main/java/com/photocoach/app/camera/PendingSourceStore.kt) 保存到 noBackupFilesDir，捕获前检查空间/配额并先写身份与源路径日志，成片与日志同步刷盘；处理目录纳入配额。旧 v1～v3 源先复制刷盘、写新路径，再删旧源，不猜 batchId。无源且无资产保留明确不可恢复日志。已发布 URI 或已 commit 的 pending 身份先核验；[CaptureSaver](../../androidApp/src/main/java/com/photocoach/app/camera/CaptureSaver.kt) 和恢复自动清理只删除确认仍 pending 的行，不回滚已发布或不明状态行。 | 存储空间/配额/字节迁移 JVM Pass；[InterruptedSaveRecoveryTest](../../androidApp/src/androidTest/java/com/photocoach/app/camera/InterruptedSaveRecoveryTest.kt) 新增真实 MediaStore 无源已发布、commit 后日志失败、无源不可恢复分支，连同旧 schema 回归编译 Pass，设备执行 NotRun。512MiB 配额、64/160/192MiB 峰值和 32MiB 余量是可配置工程初值，不是校准结果。Live 视频/包装仍是有界缓存，持久 JPEG 源可用于降级；不承诺缓存丢失后恢复完整 Live。 |
| O-07，FR-02/12/14/28 | Fixed | noBackup 捕获源配合 [data_extraction_rules.xml](../../androidApp/src/main/res/xml/data_extraction_rules.xml)，云备份与设备迁移均显式排除 root/file/database/sharedpref/external 与 device protected domains；Manifest 既有 allowBackup/fullBackupContent=false 保持。 | `aapt2 dump xmltree` 已检查实际 Debug APK 的 Manifest 与编译 XML，排除项存在；OEM 备份/迁移执行 NotRun。用户公开相册仍由系统和用户管理。依赖合并产生的 INTERNET/ACCESS_NETWORK_STATE 等普通权限仍存在，并非本次新增；不能表述为“安装包只有 CAMERA”。 |
| O-08，FR-11/12/25/29、UX-09/27 | Fixed | SaveProgress 带独立 originalUri；原片确认后立即显示缩略图和“原片已保存”，副本/配方失败只提示失败阶段。后台仍持有当前保存操作，UI 不再把后续阶段称为正在捕获；失败可结束，保存重试不会调用相机补拍。结束失败后新捕获重新初始化批次的路径原已满足。 | [OriginalPublicationFeedbackTest](../../androidApp/src/androidTest/java/com/photocoach/app/OriginalPublicationFeedbackTest.kt) 调用真实 VM 检查即时缩略图/文案与部分失败状态，编译 Pass、执行 NotRun。CreativeCaptureSession/BurstSession/SaveWorkflow JVM Pass。内部阶段枚举仍用 Capturing 管理操作互斥；未在已有保存事务中开放并发捕获。 |
| O-09，FR-31、UX-36/37 | **Blocked** | 开启、重绑或滚动分段前置窗口不足立即 JPEG 原已满足。新增实际 JPEG SENSOR_TIMESTAMP 收集和 MotionTimestampMapping；移除用提交快门时间减 Start 通知时间的伪映射。**生产 Recorder 没有已验证的首编码帧 sensor/media 锚点，传入锚点仍为 null，因此即使缓存充足，当前正常 Live 也会明确降级 JPEG。没有宣称 Live 实现完成。** | 映射边界 JVM Pass 只证明给定锚点的算法；不能替代生产接线。既有 Mp4Clipper 同步帧起点补偿保持。解除阻塞需在固定版本/批准范围内提供可核验编码起点，或另行批准媒体管线方案，再冻结封面/时长容差并完成 HyperOS 播放、无音轨及字节保持验收。 |
| O-10，FR-37、UX-45 | Fixed | ThermalPolicy 区分 Critical/Emergency/Shutdown；极端状态在 UI、倒计时、VM、Binder 拒绝新捕获，取消已排程快门、暂停批次；已捕获原片继续保存。降温不会自动继续批次或补拍。 | NewRequirementsCameraTest 含平台状态 5/6、快门禁用及原片保存保留策略，JVM Pass；Activity 取消计时和真实系统撤回 Device NotRun。未进行设备加热。 |
| O-11，FR-13/14、UX-11 | Fixed（工程路径） | 只有可确认的离线中文音色才启用语音；无本地音色可见降级。同一应用/CameraX 管线以 BuildConfig 选择 NONE/STATIC/DYNAMIC、场景和配置 ID；非法值拒绝，默认 NONE。研究关闭 P1/实时初值，每轮回默认镜头、Photo、零 EV 等，等待重绑及 EV 成功回执后记录可操作事件。固定静态卡不消费感知自动完成。事件加条件、场景、配置、版本与 captureId；[ResearchRecomputation](../../androidApp/src/main/java/com/photocoach/app/research/ResearchRecomputation.kt) 接收预登记轮次集合，保留失败/缺失/重复明细。 | STATIC 的 compileDebugKotlin Pass；错拼 STATCI 配置被拒绝；默认 NONE 最终构建 Pass。固定卡与四轮示例复算 JVM Pass。示例为成功/超时失败/缺失/逾期发布各一轮，重复记录不增加成功数，不把缺失从分母删除。首次新装断网、OEM 无音色、正式原始数据导出和研究设备配置执行 NotRun。正式双方盲评、退出排除、D7、样本量/统计及数据访问/保留责任仍 **Blocked/Unknown**，不计算 Go，也不启动正式采样。 |
| O-12，CP-01 与证据日期 | AlreadySatisfied | 既有文档已区分历史未连接、局部设备 Pass/Fail、模拟器和新合同 NotRun，全部保留。ViewfinderScreen 在最外层全屏 BoxWithConstraints 使用 0.20 默认比例，标准 UI 测试按 root 高度检查；大字号已有独立增高策略。 | 本次只核对路径、构建现有 UI 测试；小米全屏/系统栏、字体、TalkBack 和横竖屏执行 NotRun。不是新实现，也不把旧模拟器通过升级为新合同通过。 |

CameraX 官方 API 给出 Recorder 的录制时长统计及 Start 通知，并未在这些公开接口提供首帧传感器时刻；O-09 的阻塞依据是接口能力与当前生产接线，不是猜测设备误差。[RecordingStats](https://developer.android.com/reference/androidx/camera/video/RecordingStats)、[VideoRecordEvent.Start](https://developer.android.com/reference/androidx/camera/video/VideoRecordEvent.Start)。O-01 的请求/能力/回执分别依据 [Camera2CameraControl](https://developer.android.com/reference/androidx/camera/camera2/interop/Camera2CameraControl)、[CameraCharacteristics](https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics)、[CaptureResult](https://developer.android.com/reference/android/hardware/camera2/CaptureResult)；官网后续版本内容不构成依赖升级授权。

总控另行提供固定版本只读核验：缓存 `camera-video-1.6.1.aar`（缓存标识 `45da4b5044f4e96e503b9d036e611c5d68b3abb5`）的 `javap` 显示 `Recorder.writeVideoData` bytecode 170～176 将编码 PTS 减去 `mFirstRecordingVideoDataTimeUs`，278～286 把差值写入 BufferInfo 后交给 muxer。因此该版本文件 PTS 已归零，不能从 MP4 首 PTS 反推出 sensor 原点。此项是总控提供的独立检查，不冒充本 Writer 执行；没有用反射或私有接口绕过阻塞。

## 额外 FR/UX 与未关闭证据

- 全部 228 个审核 ID 的逐项来源和原判定保留在审核原文及总控逐项矩阵。本报告只更新代码处置，不把“路径存在”升级成 228 项通过。
- FR-03/18、UX-12/13/23：保留三/四用例失败降级和已验收焦段门禁；修正锁观察器在同次绑定内部 fallback 时的生命周期失效，真正重绑/释放才使旧绑定结果失效。物理镜头、厂商扩展和真实视场仍待设备证据。
- FR-23、UX-33：参数建议使用真实 AF/AE 能力，面板开关递增播放代次；离开面板后旧播报回调不得影响当前动作。机位灵感不占据自动动作完成证据。
- FR-24/26/30/34/35/38、UX-26/28/30/38/40/41/46～49：既有风格、编辑、推荐、美颜、多人保守和资源策略回归随全套 JVM 运行，未扩大功能。十二风格实拍、每姿势/技巧样本、美颜纹理运动对齐、目标机资源与真实颜色没有新增产品证据，NotRun。
- M-01～M-11 的正式统计、稳定性/保存长期保证、感知有效期/占比阈值、Live 误差和热恢复滞回都未以工程常量代替冻结与校准。示例复算不是正式研究或 Go 结论。

## 验证记录

日期均为 2026-09-12，本 Writer 实际执行；以下计数读取本次 JUnit XML，未沿用旧报告。

| 层 | 命令/证据 | 结果 |
|---|---|---|
| Coach JVM | `gradlew.bat :coach:test --offline`；最后补充关键点失效回归后单独重跑 | **79/79 Pass**，0 failed、0 skipped |
| Android JVM | `gradlew.bat :androidApp:testDebugUnitTest --offline` | **178/178 Pass**，0 failed、0 skipped |
| .NET | `dotnet test ExplainApi.sln --no-restore --verbosity minimal` | **3/3 Pass**；服务代码未改，不代表 GateLocked 客户端已获准 |
| Debug 构建 | `:androidApp:assembleDebug` | Pass，最终 BuildConfig 为 NONE/WINDOW/空配置 ID |
| Instrumented 编译 | `:androidApp:assembleDebugAndroidTest`，包含新增真实恢复和 VM 用例 | Pass；**执行 NotRun** |
| lint | `:androidApp:lintDebug` | Pass，0 errors、9 warnings：8 个 UseKtx 建议及 1 个 UsableSpace 建议；未把保守可用空间预检改成自动清理旧源 |
| 研究配置 | STATIC/WINDOW/engineering-v1 的 `:androidApp:compileDebugKotlin`；`help -PRESEARCH_CONDITION=STATCI` | STATIC 编译 Pass；非法条件按预期配置失败；随后恢复 NONE 完整构建 |
| APK 规则 | aapt2 检查实际 APK 的 Manifest、res/xml/data_extraction_rules.xml | 排除规则打包 Pass；真实迁移/备份 NotRun |
| Device | `adb devices` 只读 | 列表为空；无安装或测试 |
| 产品验收 | P-1、P0、P1、自然上镜、CP-01、新装离线、长期保存 | **NotRun**；本报告不作 Go 或完成率判断 |

最终全套 Gradle 命令为 `:coach:test :androidApp:testDebugUnitTest :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest :androidApp:lintDebug --offline`，BUILD SUCCESSFUL；之后只增补 Coach 测试并以 `:coach:test` 再跑通过，不改变已构建生产代码。初次编译的接线错误已修复，不以失败轮次作为最终证据。

产物 SHA-256：

- `androidApp/build/outputs/apk/debug/androidApp-debug.apk`：`CFF89E3CE13CE451CF25D631711A1E3F9FA85B9F6452591096130C755986BA18`
- `androidApp/build/outputs/apk/androidTest/debug/androidApp-debug-androidTest.apk`：`7FD96DCBC673A9B6146915253ED9A9603FF30BEC154AD078CF3B3AB029F9C916`

## Rules 与后续复审入口

本轮读取集合：仓库 photo-coach-coding SKILL；其 android-camerax、mlkit-signals、coach-engine、p1-creative、version-scope、self-check、sources 附件；`docs/requirement.md`、`docs/architecture.md`；interaction-guidance-and-scenarios、p1-creative、p1-beauty；camera-and-perception、creative-and-storage、guidance-and-explain、beauty、architecture/decisions；acceptance-plan、beauty-validation；requirements-matrix、decisions-and-conflicts、requirements-optimization-2026-09-12；逐项需求审核、源覆盖索引及指定只读代码审核。CP-01 按本轮用户裁决读取，不重新裁决历史冲突。

建议总控从 O-06 的 MediaStore 真实恢复用例、O-04 的实际 reducer 链路、O-11 每轮配置确认，以及 O-09 显式阻塞开始独立复审。仍需设备与研究冻结的项目保持上述状态，不能因构建成功而销项。

## 第二轮提交与验证快照（C2 未通过一项）

本轮接收总控退回的 C 终态 `01a09530-ae15-7733-a459-4a1f724576c9`，确认首轮未通过 7 项，而不是沿用首轮 Fixed 申报；[精确复验原文](code-reaudit-2026-09-12-round1.md)已归档。另接收权限复核 turn `01a09545-233b-7d61-90ab-3e8cf0e5f026` 的[固定依赖权限报告](permission-audit-2026-09-12.md)，将其作为第 8 项修复。未读入无关 turn 作为验收依据。

**Delivery：8/8 可修复项已完成工程修复并可复审，C2 尚未执行；不表示独立复验通过。O-09 仍 Blocked，完整 Live 仍因缺少生产 sensor/media 锚点明确降级 JPEG。设备及产品、正式研究仍 NotRun/Unknown。**

| C 缺口 | 第二轮源码处置 | 有效回归与边界 |
|---|---|---|
| 1，P1 原片后快门占用，O-08/FR-11/12/25/29 | [OriginalFirstSavePipeline](../../androidApp/src/main/java/com/photocoach/app/camera/OriginalFirstSavePipeline.kt) 分开原片和效果队列。CameraBinder 在原片发布/日志确认后，在 main 上移交按 captureId 持有的后台 lease、释放普通捕获占用，再发送 `captureReleased`；效果挂起不占原片队列。新旧 source、pending、recipe、URI 和 lease 分离。VM 收到真实释放后进入 Saved，普通新拍可用；旧完成不会重置新轮次，旧失败只更新后台恢复提示，不把新拍改成 SaveFailed。后台研究事件保留原捕获上下文。批次使用 `holdBatchCapture` 保留连拍互斥和显式恢复规则。 | SecondRepairPipelineTest 让效果队列不执行，先完成两个原片，再验证第一效果失败与第二成功身份不混淆，JVM Pass。[OriginalPublicationFeedbackTest](../../androidApp/src/androidTest/java/com/photocoach/app/OriginalPublicationFeedbackTest.kt) 使用生产 pipeline、真实 CaptureSaver/MediaStore 与 VM，验证原片后第二个 captureId 可建立，旧失败不锁新拍；编译 Pass、执行 NotRun。后台失败源保留日志，从恢复记录重试，不伪装成新的拍摄重试。 |
| 2，P1 Saved 快速快门绕过研究准备 | GuidanceSession 自身管理 `researchPreparationRequired`：Saved 后再次快门先建立新轮次并返回未捕获；准备期间 session 快门为 false。VM 在未接受时也同步新轮并发出参数准备请求，屏幕和音量路径共用 `beginCapture`。不会等下一次 100ms tick 才发现已捕获的新轮。参数确认后需要用户再次主动按快门，没有排队自动补拍。 | ResearchPreparationTest 覆盖保存后 1/100/1499ms 的快门、准备前重复快门和确认后接受，JVM Pass。真实 VM 仪器用例覆盖快速再拍、旧参数代次拒绝及新 capture 入口；编译 Pass、设备执行 NotRun。 |
| 3，P2 准备等待侵占指导时间 | 准备状态进入 GuidanceSession，tick、候选、播报、跳过及附加请求均不消耗指导；`completeResearchPreparation(now)` 在实际参数确认时重设 Observing.sinceMs/Action.shownAtMs 与播放状态。VM 使用同一时钟更新指导快照及 round_operable，不只改事件时间。 | ResearchPreparationTest 对 STATIC/DYNAMIC 注入 10～12 秒等待，确认后才开始 1.5 秒观察/8 秒动作预算，JVM Pass。真实 VM 的慢确认仪器用例检查 Action.shownAtMs，编译 Pass、设备执行 NotRun。 |
| 4，P2 STATIC 被实时遮挡输出覆盖 | ViewfinderUi 明确区分 STATIC；最终 `hasActiveLensWarning` 在 STATIC 为 false，标题、主要文案和提示块共同遵守。STATIC 固定卡继续忽略实时候选；DYNAMIC 保留真实感知指导。 | ViewfinderScreenTest 新增真实 Compose 渲染用例：固定卡 + lensWarning 同时输入，卡片仍显示、遮挡文字不出现、快门可用；编译 Pass、执行 NotRun。不是仅检查引擎候选数组。 |
| 5，P2 超时未通知 UI | [LockTimeoutDispatch](../../androidApp/src/main/java/com/photocoach/app/camera/LockTimeoutDispatch.kt) 是 CameraLocks 实际使用的定时回调；先验证原 token，再 timeout 使旧 future 失效，然后发送新失败状态，不再拿过期 token 阻止通知。 | SecondRepairPipelineTest 执行该实际调度回调，验证失败通知、重复过期定时器及迟到 focus/AE 结果，JVM Pass。[CameraLockTimeoutInstrumentedTest](../../androidApp/src/androidTest/java/com/photocoach/app/camera/CameraLockTimeoutInstrumentedTest.kt) 用真实 main Handler 延迟执行同一回调，编译 Pass、设备执行 NotRun。真实相机 AF/AE 仍未验收。 |
| 6，P2 OPEN_EYES/TURN_TO_WINDOW 证据失效遗漏 | FaceDetailClassifier 新增 `eyesKnown`，概率缺失、非有限值、无可靠朝向均不当睁眼改善；SignalFactory 保留 knownFaceSignals。GuidanceSession 对 OPEN_EYES 检查眼分类证据，对 TURN_TO_WINDOW 检查 body-angle 必需证据，丢失时立即撤销，不等待最短展示时间。 | ResearchPreparationTest 直接向真实 GuidanceSession 注入两类证据丢失并保留旧候选，验证仍立即撤销；FaceDetailClassifierTest 检查已知睁眼和 Unknown 的区别，JVM Pass。ML Kit 真实分类和关键点质量仍 Device NotRun。 |
| 7，P2 导出/恢复绕过配额 | PendingSourceStore 以目录为键共享进程内 reservation，配额同时计入现有持久文件与进行中的生成预算。捕获、自动副本、显式导出、恢复生成和旧源迁移使用同一预算入口；不足时在生成前拒绝，不删除旧恢复源。已有副本文件可直接重试发布，不因无需生成而再占一个生成峰值。导出/恢复放入效果队列，不阻塞新原片队列。 | SecondRepairPipelineTest 覆盖跨 store 实例的并发 reservation、配额拒绝和旧源字节保留，JVM Pass。InterruptedSaveRecoveryTest 调用真实导出/恢复路径，验证满额拒绝新生成/重试生成但保留旧源，既有 JPEG 仍可发布；编译 Pass、设备执行 NotRun。工程配额初值仍未替代校准。 |
| 8，FR-02 实际 APK 额外权限 | **纠正首轮依据源 Manifest 的 CAMERA-only 判断；依赖带入不等于符合 FR-02。** 按权限复核的固定版本方案，用 merge remove 排除 INTERNET、ACCESS_NETWORK_STATE、WAKE_LOCK、RECEIVE_BOOT_COMPLETED、FOREGROUND_SERVICE，以及指定 3 个 DataTransport/3 个未用 WorkManager 入口。保留类依赖、WorkManagerInitializer/SystemJobService、ML Kit 初始化/组件发现/remote worker、CameraX 元数据、TTS 查询、自有 signature 声明及使用。 | Gradle 新增 Debug/Release 实际合并清单门禁，并接入 assemble/JVM；PMinusOneCameraPolicyTest 改读实际合并 Manifest。两个最终 APK 经 aapt2 permissions/xmltree 检查：系统权限仅 CAMERA，自有签名保护存在，6 个移除入口均不在，9 个必要入口仍在。**静态产物符合性 Pass；首次离线真实 Face/Pose、覆盖旧 WM/DataTransport 数据安装、后台返回、重启/SDK 基准任务的运行安全仍 NotRun**，不把候选方案编译通过写成运行安全已验收。 |

第二轮未改权威 FR/UX、Skill 或依赖版本。累计 Writer 非文档变更为 57 个文件（39 个已跟踪修改、18 个新增）；文档为首轮审核归档、两份新增复验/权限归档、实施报告及授权更新的 controller，共 5 个 Writer 文档，累计 62 个文件。首轮文档快照中的 35 个既有文件，仅 controller 按总控本轮授权更新，其余 34 个 SHA-256 保持不变。Controller 明确保留 A 前文档基线、首轮 B/C、第二轮 B 和待执行 C2，消除“当前仍仅修改文档”的误读。

### 第二轮最终执行证据

- 最终命令：`gradlew.bat :coach:test :androidApp:testDebugUnitTest :androidApp:assembleDebug :androidApp:assembleRelease :androidApp:assembleDebugAndroidTest :androidApp:lintDebug --offline`。本 Writer 实际执行，**BUILD SUCCESSFUL**（136 tasks，37 executed/99 up-to-date）；这次生产源码编译、JVM 和新增仪器测试编译均有实际执行。默认配置保持 NONE。
- Coach JVM **82/82 Pass**；Android JVM **182/182 Pass**，总 **264**，失败/跳过均 0。本轮比首轮新增 7 个 JVM 测试，直接覆盖上述状态机、调度和预算。计数取最终 JUnit XML。
- Debug、Release unsigned、Debug AndroidTest APK 构建 Pass；lintDebug **0 errors/9 warnings**，Release lintVital Pass。Release 是未签名工程产物，未安装/发布。
- `verifyDebugManifestPolicy`、`verifyReleaseManifestPolicy` 均实际执行 Pass；aapt2 对两个最终主 APK 的权限、signature 及组件逐项核验 Pass。原始 dump 在 `androidApp/build/reports/permissions-debug-apk.txt`、`permissions-release-apk.txt`，是产物检查，不是设备运行记录。
- Instrumented：新增真实 MediaStore + VM 管线、VM 研究准备、Compose STATIC 隔离、main Handler、配额恢复用例均编译 Pass；**全部执行 NotRun**。
- .NET：第二轮未改服务，**本轮 NotRun**；首轮 Writer 实际执行的 3/3 Pass 保留为历史，不冒充本轮重跑。
- `adb devices` 再次为空；没有安装、清数据、设备加热、权限弹窗测试或覆盖安装。权限裁剪后的 ML Kit/WorkManager 运行验证尤其不能由 JVM 通过代替。
- HEAD 仍为 `39719dac5b499ff9bba2eb02331df35dcf05d5ae`，无提交/推送、worktree、新任务或代理。最终 diff 空白检查、报告链接与受保护文档哈希检查通过。

第二轮产物 SHA-256（替代首轮产物，首轮哈希保留在历史段）：

| 产物 | SHA-256 |
|---|---|
| Debug APK | `44B1E7B93381E922C3A05B909B2EAC1BF45874A0ACE8ADAC19ACF72E5F3F6E9F` |
| Release unsigned APK | `C408B58EE60BF36E6C4F69D2BDBB7F3BBC430A2F90B43529D510E4925C001609` |
| Debug AndroidTest APK | `5E57207451F5D18A7761D64BAE7564264854E1A5B3796D55852E660D059AA7D7` |

Rules：沿用首轮已完整读取的当前总纲、架构及相机/感知/指导/创意保存/验收附件，本轮再读 photo-coach-coding SKILL、当前 controller、精确 C final 和固定依赖权限 final，并按真实生产调用链复核上述 8 项。CP-01 保持整个屏幕高度约 20% 的用户裁决，未重新询问或改写。O-09、设备、正式实验责任/统计/数据保留以及其他产品校准边界没有因本轮代码修复解除。

## 第三轮有界修复与最终验证

C2 的完整复验 turn `01a09559-c280-7ab0-962c-3c8fd493d80b` 和补充确认 turn `01a09560-53b4-7db0-b0f1-63991833a905` 均为 completed；两份精确 final 已归档到 [code-reaudit-2026-09-12-round2.md](code-reaudit-2026-09-12-round2.md)。原文包括 `/E:/` 链接保持不变，正确当前入口在归档外另列。C2 未发现第二个恢复 lease 并发缺陷，本轮不改恢复互斥、配额、研究、相机锁或权限策略。

**本轮 Delivery：确认的一个 P1 已修复，工程交付待 C3；不声明 C3 或产品验收通过。**

| 缺陷和触发链 | 第三轮生产修复 | 实际回归 |
|---|---|---|
| A 原片发布失败 → 重试 A → A 原片发布成功并释放捕获 → 用户拍 B → A 副本失败，旧 retrySave 失败回调默认取 B 身份，可能使 B 进入 SaveFailed 并把最近照片改回 A | [MainActivity](../../androidApp/src/main/java/com/photocoach/app/MainActivity.kt) 在启动重试时读取 Binder 的 pendingCaptureId，交给 [retryCapturedSave / CaptureSaveFailureHandler](../../androidApp/src/main/java/com/photocoach/app/CaptureSaveCallbacks.kt)。handler 在排程前固定该 ID；首次保存、重试保存、重试同步拒绝和捕获失败共用身份明确的失败处理及恢复列表刷新。Binder.retrySave 要求 expected captureId 并核对待保存事务。VM.onSaveFailed 删除动态 current-ID 默认参数；所有异步失败必须显式归属。已记录完毕的批次回调只允许匹配当前批次最近照片，不把空 activeSpec 当新捕获身份。 | CaptureSaveCallbacksTest 的两个 JVM 测试通过真实重试分发接线验证 A/B 交错的回报身份、恢复刷新、同步拒绝、页面销毁，以及非 SaveFailed 的过期重试不进入保存；**2/2 Pass**。不是绕过重试入口直接给 VM 传正确 A ID。 |
| 要求 A 可恢复，B 阶段、身份和最近照片不被 A 覆盖 | 保存源、journal 和 lease 继续按原身份管理；旧 A 失败进入既有后台失败分支，只展示 A 的恢复提示，不改变 B。失败刷新恢复记录也统一用于 retrySave，修复其原有漏刷新。 | OriginalPublicationFeedbackTest 新增 `retryCallbackForAAfterBStartsCannotOverwriteBAndRefreshesARecovery`：真实 VM + 生产 retryCapturedSave + 原片优先 pipeline + MediaStore；先制造 A 原片失败，再经实际重试 callback 完成 A 原片、进入 B，随后 A 副本失败。断言 B 阶段/captureId/真实 URI 不变、A 源文件与失败 journal 保留、恢复列表含 A，并检查两次失败均触发刷新。**编译 Pass，执行 NotRun**；没有手工绕过回调传入 A 身份。 |

本轮仅触碰 6 个非文档文件：MainActivity、AppViewModel、CameraBinder、新 CaptureSaveCallbacks、新 CaptureSaveCallbacksTest、既有 OriginalPublicationFeedbackTest；另更新实施报告/controller并新增 C2 原文归档，合计 9 个本轮文件。累计 Writer 变更为 59 个非文档文件（39 个已跟踪修改、20 个新增）和 6 个 Writer 文档，共 65 个。既有权威需求/FR/UX/Skill/依赖不改；原 35 个文档快照中，除授权 controller 外的 34 个哈希保持不变。HEAD 仍为 `39719dac5b499ff9bba2eb02331df35dcf05d5ae`，未提交/推送、未创建 worktree/任务/代理。

### 第三轮最终验证

- 实际执行完整命令：`gradlew.bat :coach:test :androidApp:testDebugUnitTest :androidApp:assembleDebug :androidApp:assembleRelease :androidApp:assembleDebugAndroidTest :androidApp:lintDebug --offline`。最终源码的 Android JVM、Debug 和 AndroidTest 构建通过；Release 增量打包发生一次未展开原因的异常。随后**未改源码**执行 `:androidApp:assembleRelease :androidApp:lintDebug --offline --stacktrace`，**BUILD SUCCESSFUL**（80 tasks，10 executed/70 up-to-date）。本轮未清理源码或以旧 Release 产物冒充成功；该一次打包异常的具体原因未确认。
- Android JVM **184/184 Pass**，本轮实际执行；Coach **82/82 Pass** 为未改模块的有效既有 XML，本轮 task 为 UP-TO-DATE。合计266项，失败/跳过均0，不把缓存模块说成本轮重新执行。
- Debug APK、Release unsigned APK、Debug AndroidTest APK 均与最终源码一致并构建成功；lintDebug **0 errors/9 warnings**，Release lintVital Pass。新增真实 Android 重试交错用例只编译，没有设备执行。
- Debug/Release 合并 Manifest 门禁均 Pass；对本轮两个主 APK 再执行 aapt2 permissions/xmltree，确认系统权限仅 CAMERA、自有 signature 保留、6 个移除组件不回流、9 个必要入口仍在。权限静态符合性不替代首次离线、覆盖安装/旧 SDK 数据/后台返回的运行验证。
- .NET 未改，第三轮 NotRun，首轮3/3保留历史。Instrumented、目标机、产品和正式实验均 NotRun/Unknown；没有安装、清数据、设备加热或正式采样。
- diff 空白检查、修改报告链接、受保护文档 SHA-256 和两个 C2 final 原文完整性核对通过。C2 原文的历史 `/E:/` 引用按授权保留，不做改写。

| 第三轮产物 | SHA-256 |
|---|---|
| Debug APK | `A49090FF9A974DFB73509402DCE4513AA21B34BC880C7B41063CD3983FAD56CA` |
| Release unsigned APK | `C7043A275E5F986ED4353A754A67140073F30759F18C8D72803B160CBB189952` |
| Debug AndroidTest APK | `EEFD3835AC5BDBFA07BD283A425DBA8AB3226CC4E5D2F053CFCC5532D8D958E2` |

Rules：沿用已完整读取且未变的 photo-coach-coding Skill、当前需求/架构与保存/指导/相机/验收附件，本轮增读精确 C2 两份 final，并只核查/修改上述重试回调生产路径和相关测试。CP-01 仍为整个屏幕约20%；O-09 真实 Live 时间轴仍 Blocked，不使用私有反射或伪造锚点；设备、研究冻结和产品校准没有随本轮修复解除。
