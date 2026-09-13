# 非真机补充只读审查原文

归档已完成的 Reader 终报；其并发检查结论不替代 Writer 停止写入后的最终复验。

## 01a0962a-45af-7740-a28f-f3b3c970f259 / msg_071c4d1c622436d1016aa56c6bf57487d08f8b6e21f2308809

~~~~text
**这 4 项失败均有证据支持属于测试夹具或定位方式滞后，目前不需要据此修改生产行为。** Writer 正在修改并复跑，以下是中间审查结论，不是最终通过判定。

| 失败用例 | 独立判断 |
|---|---|
| `optionalAppearsOnlyWhenReadyAndOnlyVerifiedFocalButtonsAreShown` | **修夹具正确。** 正式交互规范第 139、165 行规定，仅“可以拍了”提供再优化；生产 `canRequestOptional` 要求 `qualityConfirmed`。补充 `qualityConfirmed=true` 恢复了测试必要前提，没有降低要求。 |
| `creativeMenuExposesTwelveStylesExplicitBurstAndParameterAdvice` | **修访问路径正确。** P1 需求及 UX-33 要求用户主动打开独立参数面板。应点击“打开参数建议”，经状态回调显示面板后检查建议；不能要求建议仍直接出现在创意菜单。当前修改保留了风格、连拍和建议内容断言。 |
| `beautyMenuOffersExplicitPresetsWithoutBlockingShutter` | **修文案定位正确。** UX-46 要求默认 OFF、明确选择和快门可用，没有规定必须保留旧入口文案。改查“美颜·关闭”合理；自然档选择回调及快门断言仍保留。 |
| `manualZoomRespectsTheBoundCameraCapability` | **修断言范围正确，但建议继续增强。** `zoomBy` 返回 null 表示本次未应用变焦，不能推导界面不得显示任何状态提示。应检查没有新增虚假倍率。非 null 分支仍需等待本次返回倍率对应的精确文本，避免读到旧状态提示；单纯断言 `>1f` 也受显示取整影响。已告知 Writer。 |

核查文件包括 [ViewfinderScreenTest.kt](E:/Code/Agent/photo-coach/androidApp/src/androidTest/java/com/photocoach/app/ui/viewfinder/ViewfinderScreenTest.kt)、[ViewfinderActivityTest.kt](E:/Code/Agent/photo-coach/androidApp/src/androidTest/java/com/photocoach/app/ViewfinderActivityTest.kt)、生产 ViewfinderScreen、MainActivity、AppViewModel、CameraBinder 和 GuidanceSession，并对照正式交互、P1、美颜及验收规范。

**实际覆盖与遗漏**

- 本轮 [instrumented-baseline.txt](E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/instrumented-baseline.txt) 确认为 **64 项：60 Pass、4 Fail**。现存 connected XML 是 **9 月 4 日的旧 26 项结果**，不能混入本轮。
- 已实际通过：4 项真实 VM/保存回调测试、9 项 MediaStore 恢复测试、STATIC 固定卡隔离、研究慢确认、真实 Handler 超时、相机三用例与 Effect 重绑、模拟器 TTS 等。
- **真实 MainActivity 后台返回／重建仍有覆盖缺口。** 当前 BeautyCameraBindingTest 使用自建 LifecycleOwner 反复绑定、解绑，不能替代页面生命周期中分析恢复、快门恢复及保存回调隔离。
- 参数面板夹具可证明显示与操作路由，不能证明真实 Activity 打开面板后 TTS 停止、后台分析继续、关闭后不补播旧句。
- 新增 [OfflineModelInstrumentedTest.kt](E:/Code/Agent/photo-coach/androidApp/src/androidTest/java/com/photocoach/app/OfflineModelInstrumentedTest.kt) 检查无 INTERNET 权限下 Face/Pose 初始化及空白图处理，**不能扩称为正样本检测、干净首次离线、旧 WorkManager/DataTransport 数据覆盖升级全部通过**。
- 新研究日志及离线模型用例不在 baseline 64 项内；读取时第二轮 67 项运行尚未结束，不能提前计 Pass。

高影响遗漏和变焦测试建议已发送给唯一 Writer。建议保留原始失败记录，再以最终源码和完整复跑结果关闭这 4 项；无法完成的生命周期、首次状态及升级路径须分别标注，不以 JVM 或空白模型输入替代。

本次全程只读，未构建、运行测试或操作设备。
~~~~

## 01a09634-1928-7d01-a20a-e8a7d4f59a0d / msg_071c4d1c622436d1016aa56e882e1487d0a063d0d71520ac06

~~~~text
**补充审查发现研究复算存在可修复缺陷，已直接交给唯一 Writer。当前源码仍在修改，不能据此宣布研究数据合同验收通过。**

| 发现 | 触发与影响 | 最小修复要求 |
|---|---|---|
| **保存恢复按轮次归因错误** | A 捕获失败后重新拍 B 成功，会因“该轮曾失败且后来发布”被计为恢复成功；多个接受请求被压成一个保存分母，退出排除还可能删除保存失败。 | 按 `captureId` 建立请求账本；仅同 ID 保存失败后发布算恢复。保存分母独立于完拍时间及退出排除策略。 |
| **迟到发布覆盖主动退出** | `session_exit` 后原片发布，复算优先判成功，丢失退出事实，后续退出策略无法正确执行。 | 退出、发布、超时分别保留，再由明确策略决定时间指标；保存事实不删除。 |
| **整条缺失的盲评被遗漏** | 仅统计显式 `MISSING`。某 pair 的 subject 整条未返回时，缺评数为 0，该票也从分母消失。 | 预登记 pair×角色槽位，保留完全缺失的答卷和配对；不能只在样例中手工补 `MISSING`。 |
| **跨进程恢复缺少生产研究事件** | 恢复入口只返回剩余日志、刷新 UI；成功恢复没有携带原捕获研究身份写入 JSONL。内存中的 CaptureEventLedger 无法跨进程提供关联。 | 持久化必要匿名上下文，记录原 capture 的真实恢复结果；无法关联时明确证据不完整，不能归到当前轮或新增 accepted。 |
| **未接受的捕获失败被误判为日志损坏** | 热保护、空间预检、未就绪等可在 `capture_accepted` 前失败；VM 写 `capture_failed`，Reader 却要求已有 accepted，导致合法失败使整份导入不完整。 | 区分捕获请求拒绝与接受后的捕获失败；保留轮次失败，不伪造保存分母。 |
| **研究与普通日志未隔离** | 默认 NONE 也写入同一 JSONL，但 Reader 只接受 STATIC/DYNAMIC；历史普通记录会污染整份研究导入，并共享容量上限。 | 明确研究写入或导出会话隔离，同时保留选定会话完整性检查。 |

主要位置为 [ResearchRecomputation.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchRecomputation.kt)、[ResearchMetricCalculation.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchMetricCalculation.kt)、[ResearchDataContract.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchDataContract.kt) 及 [AppViewModel.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/AppViewModel.kt)。

**并发修改说明：**最后一次读取时，Writer 已开始将保存统计改为逐 capture 账本，并从全部 trials 计算保存分母；这对应第一项的修复方向。我尚未完整复验该修改及其测试，不能将初始缺陷原样视为终版残留，也不能提前销项。

其余判断：

- JSONL 已对解析错误、序列缺口、写入/队列丢失、元数据及部分身份不一致进行失败关闭；不是所有异常都会被当成有效样本。
- D7 当前按匿名参与者去重，失访不增加复用分子；是否将失访留在分母由显式策略控制。该策略只能作为合成计算参数，不能冒充已冻结研究规则。
- `ResearchFreeze` 字符串非空仅能证明字段填写；不能证明双方盲评汇总、失败时间处理、保留期限等规则已实现。当前计算输出仍是描述性工程结果，没有 Go 判定。
- 现有真实 VM→JSONL 仪器用例覆盖同进程失败、重试和发布，不能替代跨进程恢复、退出后迟到发布、整条缺评等反例。

本次沿用需求 3.3.1/3.3.2 和既有架构，只读检查源码及测试，**未写文件、构建或运行模拟器**。需待 Writer 停止写入后，对最终实现及新增反例证据统一独立复验。
~~~~

## 01a09634-1928-7d01-a20a-e8a7d4f59a0d / msg_071c4d1c622436d1016aa56f37162c87d09e03ba3d7e13d54f

~~~~text
核查结论：**失败后退出仍有统计排除风险；正常研究单拍的后台配方／副本失败没有污染首试保存率的生产链证据。** 已将具体结果交给 Writer。

1. **确定缺陷：失败或超时后退出，会被当成可排除退出。**  
   真实路径是捕获已接受 → 捕获／保存失败，或轮次超时 → 用户关闭页面。当前 [ResearchRecomputation.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchRecomputation.kt) 优先判 `EXITED`；[ResearchMetricCalculation.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchMetricCalculation.kt) 又按 `excludeExits` 排除该轮。这可能删除需求明确要求计为未达标的失败或超时。应分别保留失败、超时和退出时刻，区分“主动退出”与“失败后关闭页面”，不能仅凭退出早于发布排除。

2. **后台效果失败：正常生产路径已隔离。**  
   研究配置关闭连拍、Live 和风格等 P1 能力。普通单拍原片发布并释放捕获后，VM 将身份加入 `releasedOriginals`；后续配方／副本失败记录为 `background_save_failed`，而非普通 `save` 失败。当前复算不把该事件计入原片首试失败。因此，不应仅凭构造一个发布后的普通失败事件，断言正常效果失败污染统计。

3. **另有同毫秒顺序边界需要修。**  
   新逐 capture 实现用 `elapsedMs <= publishedAtMs` 判断发布前失败。原片阶段完成后日志第一次写失败、补写成功再抛错，可能先发发布事件、再发普通保存错误；两者处于同一毫秒时仍会错误归入“发布前”。应按事件序列／位置判断，而非仅比较毫秒。

4. **保存分母已有修复进展，尚待终版复验。**  
   最新代码已改为从全部 `trials.flatMap(captures)` 计算保存率，退出排除不再删除 accepted 请求；正常新拍与同 ID 重试也开始分开。这是正确方向，但 Writer 仍在修改，本次不作最终通过判定。

已要求补真实 VM 的“失败→退出”“超时→退出”，以及同毫秒发布后错误的反例。全程只读，未运行测试或修改文件。
~~~~

## 01a09648-2e1b-7a61-8706-0846ee205bb9 / msg_071c4d1c622436d1016aa572e5d4c887d0861d81f205692228

~~~~text
本轮只读结论：**发现首次帧等待指标与验收目标存在偏差；尚未证实相机恢复失败，也未定位 ComposeNotIdle 的具体根因。**

1. **失败发生位置已确认。** [instrumented-3.txt](E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/instrumented-3.txt:13) 记录 69 项、3 项失败：
   - 生命周期用例在 `prepare:29` 等待首帧超时，尚未执行后台切换或 `recreate`。
   - 参数面板用例在 `prepare:26` 查询语义节点时发生 ComposeNotIdle，尚未打开面板或检查语音。
   - 变焦用例在 `ViewfinderActivityTest:43` 检查快门可用时发生 ComposeNotIdle，不能据此认定变焦功能失败。

2. **首次帧计数存在条件性根因。** 失败时测试等待 `acceptedAnalysisFrames>0`，但该计数仅在 [AppViewModel.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/AppViewModel.kt:278) 的帧门禁通过后增加，并不代表分析器实际交付帧数。[CoachAnalyzer.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/analysis/CoachAnalyzer.kt:157) 对非 REALTIME 时间源使用 `elapsedRealtime()-10000`；[FrameSequenceGate.kt](E:/Code/Agent/photo-coach/coach/src/main/kotlin/com/photocoach/coach/FrameSequenceGate.kt:4) 只接受年龄不超过 1500ms 的帧。因此该条件下，即使分析持续完成，计数仍不会增加。**本轮日志没有记录 AVD 的时间源，是否实际命中此条件仍为 Unknown。** 当前 Writer 已将等待改为 `deliveredAnalysisFrames` 并添加诊断输出；旧失败日志不能验证此修改。

3. **ComposeNotIdle 与清理边界。** 日志只证实 Compose–Espresso 空闲等待超时，未提供具体重组原因。连续重组、异步相机任务未结束、前用例资源残留均为 **Unknown**。[ViewfinderScreen.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/ui/viewfinder/ViewfinderScreen.kt:412) 的 `onPreviewReady` 位于 AndroidView factory，不能推断每次重组都会重新绑定相机。[MainActivity.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/MainActivity.kt:294) 有取消重绑、释放相机及关闭 TTS 的清理逻辑；源码存在清理入口不等于本轮运行已证实清理完成。测试 `prepare()` 位于用例 `try/finally` 外，但 Activity 仍由 Compose Rule 管理，不能单凭这一点认定泄漏。

**已反馈给 Writer 的最小建议：**记录时间源、分析入口数、结果交付数、门禁拒绝原因及帧年龄；分别验证分析持续交付和质量证据保守降级。不要把到达时间伪装为采集时间，也不要通过延长超时、关闭分析或删除断言掩盖问题。

本轮未修改文件、未构建、未操作设备。
~~~~

## 01a09652-f8b0-7932-98a3-aad7383fe1c2 / msg_071c4d1c622436d1016aa576320ea487d08e97a31a113a3b69

~~~~text
本轮有界复核结论：**逐 capture 保存统计、缺评槽位、请求拒绝和 NONE 隔离已有修复；退出相关计算仍有残留。跨进程恢复已持久化研究身份及凭据，但不能据此宣布研究 JSONL 导出复算全链通过。** 以下对应 **2026-09-12 23:56:21 +08:00** 所检快照；Writer 仍在修改。具体残留已直接反馈 Writer。

| 原缺陷 | 本轮判断与证据 |
|---|---|
| 逐 capture 保存分母、A 失败后新拍 B 被误算恢复 | **源码已修复。** [ResearchRecomputation.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchRecomputation.kt:39) 按 captureId 建账，区分捕获失败、保存失败、重试和发布；[ResearchMetricCalculation.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchMetricCalculation.kt:84) 从全部 trials 的 captures 计算保存分母，不随退出排除删除。新增反例覆盖 A 失败/B 成功不算 A 恢复。 |
| 迟到发布覆盖退出、失败后退出被排除 | **部分修复，仍有确定残留。** 复算已保留 exitAtMs，并将失败/超时后退出判 FAILED。但指标仅凭非空 latency 选择成功样本，30 秒分子只比较 latency；因此“失败→重试→退出→5ms 时发布”仍可能计为 30 秒成功。配对时间也优先使用该 latency，绕过 `EXCLUDE_PAIR`。现有测试只断言保存指标和时间分母，漏检时间分子。 |
| 整条缺失盲评 | **提供预登记名单时已修复。** [ResearchDataContract.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchDataContract.kt:117) 按 pair×shooter/subject 补齐 MISSING；测试覆盖缺一个角色及整对无答卷。没有名单时 `inspect` 仍报告 blocker，不能宣称缺评完整性已验证。 |
| 跨进程恢复缺研究关联 | **持久化链及复算 API 已接上，导出端到端仍未证实。** 详见下文。 |
| 未 accepted 请求污染日志 | **源码已修复，真实 VM 回归不足。** [AppViewModel.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/AppViewModel.kt:1208) 按 accepted 账本区分 `capture_rejected` 与 `save`；前者保留轮次失败，不增加保存分母。现有新增测试直接构造事件，未覆盖实际预检拒绝→Logger→Reader 全链。 |
| NONE 普通日志污染研究 | **写入入口已隔离。** [AppViewModel.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/AppViewModel.kt:1375) 在研究未启用时直接返回；Reader 支持 selectedSessions。未发现专门断言 NONE 不写文件、不占研究容量的新增测试。 |

**同毫秒边界有一处修好、一处仍残留：**

- 发布后保存错误：现按排序后的 publicationIndex 截取发布前事件，避免同毫秒后发错误改写首试保存结果；已有对应测试。
- 退出后发布：仍以 `exit < published.elapsedMs` 判断。退出先发生、发布后发生但同毫秒时，会遗漏退出先后关系。应使用已验证的事件序列/位置；此问题已反馈 Writer。

**恢复研究链的实际覆盖：**

`activeCaptureSpec.researchContext` → `PendingCapture.toJournal` → `SaveJournal` 序列化 → CameraBinder 注入 ResearchRecoveryStore → [InterruptedSaveRecovery.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/InterruptedSaveRecovery.kt:244) 在原片发布证据建立后写 receipt。凭据保存原 session、round、capture 及配置，发布时间明确为 null。

复核期间 Writer 新增了 `replay(log, expected, deadline, recoveryReceipts)`：校验关联后仅更新 capture 的 recovered，不制造 latency、不新增 accepted。**因此此前“receipt 没有复算消费者”的发现已部分修正，不能继续原样报告。**

但仍未查到生产 receipt 目录的正式导出组合及导入调用。当前：

- 跨进程 fixture 校验原片、身份、receipt 和幂等性，**不经过原会话 JSONL→Reader→replay→calculate**。
- `ResearchLogReader` 对缺 `session_exit` 的崩溃会话仍判不完整；新 replay 保持 INCOMPLETE。这是正确的保守边界，不能用独立 receipt 把该 JSONL 宣称为完整。
- 恢复的原会话发布时间保持 **Unknown**，不能转换为“30 秒内成功”。

已有 [phase1](E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/process-recovery-phase1.txt)、[stop](E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/process-recovery-stop.txt)、[phase2](E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/process-recovery-phase2.txt) 分别记录两阶段通过和中间进程停止；fixture 断言两次 PID 不同。**仅覆盖“原片已提交、journal 未记完成”这一跨进程窗口。** 其他恢复阶段的同进程/新实例测试不能扩大为全部跨进程窗口通过。

关键文件 SHA-256 如下，用于识别本轮快照：

| 文件 | SHA-256 |
|---|---|
| AppViewModel.kt | `326CB5F34447097ACF3D09404D3B2FF65D60A244065791EED45C6222ECA676B2` |
| ResearchRecomputation.kt | `B175F24DD3589997A832D80699232BEC3D77109B24AC358FE4D7B36D693758EB` |
| ResearchMetricCalculation.kt | `DC58A0C03C4E6099B80AB6985522F9440ED864A2202BE03856056465B1154E3C` |
| ResearchDataContract.kt | `0173DA5440DA7114A0B56AC64E09346615F3BA7A3CC40B28448981C3633ED9EF` |
| ResearchRecoveryStore.kt | `483851041061A93ED5A619655A93D087F99C94044B814C3640414D9338CE1321` |
| InterruptedSaveRecovery.kt | `992DC1B2B315DC5BE24EDA32A6DE2BEF3B015AAA011BC2684D4AA88FF24219B8` |
| SaveJournalStore.kt | `6E9CAC8459993427D8886293805825257463F1A4C4FFCBCF52A7204EE9464D9F` |
| ProcessRecoveryAcceptanceTest.kt | `7FD1C01A05B8F174765E1D9F5C97072E1D4078178AA2DF2C434712BB1755C9B6` |

已有 JVM XML 在 **23:53:39** 记录 CaptureRecomputationTest **5/0**、ResearchRecoveryAssociationTest **1/0**；不将这些结果绑定到之后继续修改的源码。**本轮 Changed：none；仅执行只读搜索、文件读取和 SHA 计算，未构建、运行测试或操作设备。** 沿用此前已读需求 3.3.1/3.3.2、架构及研究验收规范；这是 InScope 修复复核，不是最终全局或正式研究验收。
~~~~

## Reader 原文归档 01a0967c-b939-7450-8f96-59ee49f9d513

~~~~text
本阶段源码审查结论：**前轮指出的统计错误已修复；新的 EvidenceBundle 已把日志、恢复凭据和复算入口接通。未发现上述范围内新的确定性计算缺陷，但跨进程新接线仍需当前版本的运行证据。**

| 核查项 | 已关闭依据 |
|---|---|
| 失败后迟到发布被计为 30 秒成功 | [ResearchRecomputation.kt:64](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchRecomputation.kt:64) 保留发布前失败；[ResearchMetricCalculation.kt:44](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchMetricCalculation.kt:44) 和第 88 行要求 SUCCESS 且未先退出。保存恢复事实仍单独保留。 |
| FAILED 的配对时间绕过策略 | [ResearchMetricCalculation.kt:74](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchMetricCalculation.kt:74) 仅对成功且未先退出的轮次使用 latency；其余执行 `USE_DEADLINE` 或 `EXCLUDE_PAIR`。两种策略已有测试断言。 |
| 同毫秒退出／发布顺序 | [ResearchRecomputation.kt:68](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchRecomputation.kt:68) 改按排序后的事件位置判断，`exitedBeforePublication` 同步保留该顺序。[CaptureRecomputationTest.kt:42](E:/Code/Agent/photo-coach/androidApp/src/test/java/com/photocoach/app/research/CaptureRecomputationTest.kt:42) 覆盖退出先于同毫秒发布。发布后同毫秒错误也仍按 publicationIndex 隔离。 |
| NONE／未 accepted 请求 | [AppViewModel.kt:1377](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/AppViewModel.kt:1377) 保持 NONE 写入隔离；第 1208 行区分拒绝与接受后失败。[ResearchLogPipelineTest.kt:17](E:/Code/Agent/photo-coach/androidApp/src/androidTest/java/com/photocoach/app/ResearchLogPipelineTest.kt:17) 新增真实 VM→Logger→Reader 的拒绝测试；第 43 行覆盖 NONE 不创建、不追加研究文件。拒绝故障仍是注入，不能扩大为所有实际预检条件通过。 |

**恢复链现在不再只是孤立 receipt：**

- [ResearchEvidenceBundle.kt:16](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchEvidenceBundle.kt:16) 从实际日志文件和 RecoveryStore 导出；导入经过 JSONL 校验和恢复身份关联。
- [ResearchRecomputation.kt:22](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchRecomputation.kt:22) 消费 receipts，仅补充原 capture 的 recovered，不新增 accepted、不制造 publication latency。
- [ProcessRecoveryAcceptanceTest.kt:23](E:/Code/Agent/photo-coach/androidApp/src/androidTest/java/com/photocoach/app/camera/ProcessRecoveryAcceptanceTest.kt:23) 第一阶段已使用真实 VM 生成研究上下文及 accepted/failure 日志；第二阶段恢复后导出 bundle、重新导入并复算，同时检查不同 PID、原 capture/session 关联、原片唯一和源文件清理。
- 缺少 `session_exit` 仍由 [ResearchDataContract.kt:38](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/research/ResearchDataContract.kt:38) 判为不完整；bundle 不覆盖这一结论，replay 返回 INCOMPLETE、latency 为 null。**恢复发布已确认与研究会话完整是两个不同结论。**

仍需保留的验证边界：

1. 跨进程 fixture 已接到 **bundle→Reader→replay**，但没有继续断言 `MetricCalculation` 拒绝 INCOMPLETE；建议补此终端断言，并明确校验缺失原因包含 session closure。
2. bundle 的生产实现是研究侧文件导出/导入 API；本轮没有发现 App 内调用入口，不能描述成用户已可在界面完成导出。
3. 两阶段进程测试仍只覆盖“原片已提交、journal 未记完成”窗口；此前旧版本日志不能证明新增 bundle 接线通过。

需执行的当前用例为：`CaptureRecomputationTest`、`ResearchMetricCalculationTest`、`ResearchRecoveryAssociationTest`、`ResearchLogPipelineTest` 三项，以及 **phase1→确认进程停止→phase2** 的新版 `ProcessRecoveryAcceptanceTest`。本 Reader 未运行这些测试。

快照时间 **2026-09-13 00:40:06 +08:00**，关键 SHA-256：

| 文件 | SHA-256 |
|---|---|
| ResearchEvidenceBundle.kt | `D4B7C1087ECF90F495F043B5F6050F232F567B0E964393E80D68E501EFC4EA2A` |
| ResearchRecomputation.kt | `A395E752B868DDB0B1692A2BBB7E850539F2FC12E66873A7C5A139F0072DE446` |
| ResearchMetricCalculation.kt | `ACBB318B8DE3F6144BBE227D42D5F4E5945EF82255C1679C5E689DA539A910E3` |
| ProcessRecoveryAcceptanceTest.kt | `15AEDAE3D339C0A64D1F709DAAFA7634F530DBB05F746DFF8C1F06013A7800F5` |

Changed：none。仅只读检查源码、测试和规范定位；未构建、操作设备、写文件或派发任务。这是阶段源码判断，不是最终冻结验收。
~~~~

## Reader 原文归档 01a09681-6234-79f2-b4e4-5c82b12a2c30

~~~~text
审查时点：**2026-09-13 00:46 +08:00**。确认矩阵包含 **228 个唯一 ID**，引用的测试类文件均存在；抽查的 catalog/pose 动态用例名称与测试源码、报告一致。发现 **2 个具体非真机证据缺口，以及 3 类映射问题**。

| 问题 | 条款与缺口证据 | 最小充分处理 |
|---|---|---|
| **UX-35 拍后操作没有对应验证** | [验收计划:56](E:/Code/Agent/photo-coach/docs/acceptance/acceptance-plan.md:56) 要求打开、分享、收藏、回收站及失败反馈。[矩阵:81](E:/Code/Agent/photo-coach/docs/traceability/non-device-acceptance-2026-09-12.md:81) 却引用资产校验、图像处理、保存恢复测试；这些不执行 [MainActivity.kt:587](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/MainActivity.kt:587) 的四条操作链。测试目录未找到对应 Intent、收藏更新或回收站结果验证。 | 使用本 App 创建的一个 MediaStore URI，验证打开/分享的 URI 与授权标志、收藏更新成功/失败反馈、回收站取消/确认结果；无处理器分支可注入失败。无需验证 HyperOS 观感或第三方分享服务。 |
| **UX-20／N-22 缺实际倒计时与音量键接线证据** | [验收计划:33](E:/Code/Agent/photo-coach/docs/acceptance/acceptance-plan.md:33) 明确要求 3/10 秒、逐秒显示、屏幕/音量键触发和取消。[矩阵:66](E:/Code/Agent/photo-coach/docs/traceability/non-device-acceptance-2026-09-12.md:66) 引用 gate、画幅及设置测试；它们不执行 Activity 的倒计时协程和 `onKeyDown`。现有平台测试主动设置 timer OFF；“common entry for screen and volume shutter”只是 VM 测试注释。 | 一个参数化 Activity 用例覆盖 3/10 秒的到零单次捕获与中途取消，分别经过屏幕和真实 KeyEvent 入口；核对取消后不拍、按键被消费、不额外改变系统音量。 |
| **UX-27、UX-36 Scope 误归为 InScope** | [矩阵:73](E:/Code/Agent/photo-coach/docs/traceability/non-device-acceptance-2026-09-12.md:73)、[矩阵:82](E:/Code/Agent/photo-coach/docs/traceability/non-device-acceptance-2026-09-12.md:82) 分别对应明确三张连拍、Live 正常路径。其所属 FR-25、FR-31 在[权威矩阵:60](E:/Code/Agent/photo-coach/docs/traceability/requirements-matrix.md:60)、[第 66 行](E:/Code/Agent/photo-coach/docs/traceability/requirements-matrix.md:66) 均为 ApprovedSeparate。引用基础快门 FR 不改变增强功能的阶段归属。 | 修正为 ApprovedSeparate；无需新增测试。 |
| **UX-28 的未覆盖边界串到布局条目** | [矩阵:74](E:/Code/Agent/photo-coach/docs/traceability/non-device-acceptance-2026-09-12.md:74) 写“1080×2400、底部480px=20%”；实际 [UX-28](E:/Code/Agent/photo-coach/docs/acceptance/acceptance-plan.md:51) 是七项编辑、对比、撤销/重做、重置和非破坏另存。该比例属于 UX-15。 | 将布局证据移回 UX-15；UX-28 分别标注已有模型/保存测试覆盖与编辑 UI 接线未核实边界。 |
| **UX-47／48 未映射已有专项证据** | [矩阵:93](E:/Code/Agent/photo-coach/docs/traceability/non-device-acceptance-2026-09-12.md:93) 和第 94 行仅填“依据 UX 原文／ViewfinderScreenTest”。UI 测试不能证明 GPU 保护区、OES 输入、重绑释放、捕获配方固定及恢复身份。仓库实际已有 BeautyRendererTest、BeautyCameraBindingTest、BeautyPersistenceTest 等。 | 填 ApprovedSeparate，并按具体方法补现有证据及边界；优先修映射，不泛化要求新增测试。 |

上述两个验证缺口是**现有引用未覆盖要求的入口或分支**，不是将真机 NotRun 重新包装成缺陷，也不代表矩阵已经虚报整体通过。

未列为新缺陷的项目：

- FR-31 明确区分已验证媒体组件与实现中的双编码器接线，没有把组件通过写成生产 Live 通过。
- 风格实际 JPEG 新用例已注明待运行；SG/CU/PO 明确保留离散信号、模型及真实质量边界。
- 研究指标明确是合成复算，不是用户实验或 Go。
- 原始 `instrumented-3.txt` 的 69 项、3 失败与矩阵记录一致。
- N 类大量引用仍是概括索引，不能据此称为逐分支覆盖；但本轮未将其全部泛化为新增测试要求。

所检矩阵 SHA-256：`7FDE16941125F79B5B489DCF9FFD723F705B27FA829F3EBF3799C1035D563246`。

本轮仅只读核对文档、源码、测试引用和报告；**未改动、构建、运行设备或派发任务**。结论属于执行中矩阵审查，最终冻结后仍需复验。
~~~~

## Reader 原文归档 01a09690-a3ef-7132-a73d-c92b7b4f6e33

~~~~text
本轮发现 **1 个可复现生产缺陷**；另有一处确定的测试定位错误，会造成失败而非假通过。

1. **长按音量键会反复取消、重启倒计时。**  
   [MainActivity.kt:275](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/MainActivity.kt:275) 对每个音量键 ACTION_DOWN 都调用 `capture()`，未区分 `repeatCount`。开启倒计时后，同一次按住产生的重复 DOWN 会依次执行“取消→重新开始”；释放时若留下待执行请求，之后仍会拍摄。Android 的重复按键计数可区分同一次长按与新按下。[KeyEvent 合同](https://developer.android.com/reference/android/view/KeyEvent#getRepeatCount())

   最小修复：消费重复 DOWN，但仅首次 DOWN 触发快门。新增测试只需发送同一 downTime、递增 repeatCount 的事件，确认不取消或重启原倒计时；独立第二次按下仍可取消。当前测试的 DOWN/UP 均是短按，不覆盖此问题。

2. **回收站确认按钮在测试中必然匹配失败。**  
   [ActivityLifecycleAcceptanceTest.kt:29](E:/Code/Agent/photo-coach/androidApp/src/androidTest/java/com/photocoach/app/ActivityLifecycleAcceptanceTest.kt:29) 将节点文字删除空白后与未经处理的 label 比较；第 151 行调用 `click("Move to trash")`，实际比较为 `"Movetotrash" == "Move to trash"`。两个查找分支都有相同问题。

   最小修复：节点文字和目标 label 使用相同规范化。这是测试自身的确定错误，不能解释成生产回收站失效，也不能将此前 Cancel 分支通过当作确认分支覆盖。

其余所检路径未发现确定假通过：

- 新用例确实通过触摸、KeyEvent、四个 UI 按钮进入 Activity；打开/分享仅拦截外部 Activity 启动，并校验同一实际捕获 URI 和读取授权。
- 收藏及回收站检查真实 MediaStore 列；失效 URI 的收藏失败有明确断言。
- 短按取消后等待超过原倒计时，并检查捕获计数和 recentPhoto 未变，没有仅凭“倒计时消失”判断取消成功。

审查约截至 **2026-09-13 01:02 +08:00**。未写文件、构建、运行设备或派发任务；以上为源码判断，不是平台运行结果。
~~~~

## Reader 原文归档 01a0969b-5928-7c62-a3b5-93d98f715d8e

~~~~text
截至 **2026-09-13 01:14:24 +08:00**，发现两处部件级问题；未把未完成接线列为缺陷。

1. **Motion 的 EGLDisplay 计数无法保护其他渲染器。**  
   [MotionGlRenderer.kt:124](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionGlRenderer.kt:124) 在最后一个 Motion owner 关闭时调用 `eglTerminate(DEFAULT_DISPLAY)`，但计数不包含 [BeautyGlRenderer.kt:164](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/beauty/BeautyGlRenderer.kt:164) 或 CameraX 的 EGL 使用者。

   最小复现场景：两个线程分别创建 Beauty、Motion renderer；关闭其中一个，再让另一个切换 surface、绘制。两者取得同一 display，而 EGL 规范不把重复 `eglInitialize` 当引用计数；terminate 会使该 display 的其他资源失效。[EGL 1.5 §3.2](https://registry.khronos.org/EGL/specs/eglspec.1.5.pdf)

   Live/美颜开关互斥不能证明异步释放期间无重叠。最小处理是避免局部 renderer 擅自终止共享 display，或建立覆盖全部使用者的所有权方案；不能只扩大 Motion 自己的计数。回归覆盖两个关闭顺序，并检查存活 renderer 继续绘制。

2. **`verifyMux` 会批准明显失真的时间轴。**  
   [MotionFrameTimeline.kt:86](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionFrameTimeline.kt:86) 只检查数量、单调性、首零点、关键帧标志和总时长，未验证 mux PTS 与 `encoded()` 返回的写入 PTS 的对应误差。

   可直接复现：提交并精确关联四帧，sensor/编码时间为 `0、1、2、3秒`，首帧关键帧；传入 mux 时间 `0、2、4、6秒`。当前会返回 Verified，随后对 sensor `1.5秒` 调用 `fullWindow()` 得到 **6 秒窗口**，把两倍时长变化当作“mux 量化”。

   最小修复：校验实际 mux 时间相对写入时间的转换依据与量化误差；没有可靠依据时拒绝验证。回归保留合法量化样例，同时拒绝上述倍速失真。这里不应自行冻结产品封面容差。

其余所检结论：

- [MotionPixelPool.kt:33](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionPixelPool.kt:33) 已为各消费者创建独立只读 Buffer 视图；position 修复成立。引用释放和关闭后禁止 acquire 的现有实现未发现新的确定缺陷。
- 当前 GL 三角形采样、RGBA read/upload 本身未发现确定旋转或镜像错误；最终是否正确应用输入及输出变换取决于后续调用，不能提前判通过或报错。
- 未发现需要另行报告的确定帧池并发缺陷。

所检 SHA-256：

| 文件 | SHA-256 |
|---|---|
| MotionFrameTimeline.kt | `7ECC14AC18A21469A310C20083C901E4812ACA15D383E913CA648A02C174A456` |
| MotionPixelPool.kt | `FD10675E837623DC4E5D4A6FE2CD7BE76E33A67EF419CA94668A167AA2D590A0` |
| MotionGlRenderer.kt | `E813250F607ABEDB6CD75C799C58FB58A67F2B2670D924225CC3567DEB8A3753` |

仅只读源码、测试和官方规范；未修改、构建、操作设备或派发任务。上述复现场景尚未由本 Reader 执行，属于阶段源码审查。
~~~~

## Reader 原文归档 01a096ad-74eb-7160-b405-f561e28aea27

~~~~text
截至 **2026-09-13 01:33 +08:00**，本轮未发现可按现有调用合同确定复现的新缺陷；上轮两处问题已在源码中修复。

| 核查项 | 结论与依据 |
|---|---|
| 局部 `eglTerminate` | **已关闭。** [CameraEglDisplay.kt:7](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraEglDisplay.kt:7) 统一取得进程共享 display；Motion、Beauty 关闭时仅释放自己的 context/surface，不再 terminate。新增 `MotionEglOwnershipTest` 覆盖两种关闭顺序，并读取存活 renderer 的像素结果。 |
| mux 倍速仍被判 Verified | **已关闭原反例。** [MotionFrameTimeline.kt:86](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionFrameTimeline.kt:86) 现在逐样本比较实际 mux PTS 与写入 PTS；无 timescale 时要求精确一致，有实测 timescale 时限定量化误差。新增测试明确拒绝 `0/1/2/3秒→0/2/4/6秒`，并保留合法量化样例。 |
| EOS／输出 drain | [MotionSilentEncoder.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionSilentEncoder.kt) 排除 codec-config、空输出，首个非关键帧不写入；输出 buffer 在 `finally` 释放。成功要求收到 EOS、已执行输入关闭且未 abandon；随后停止 mux、读取实际 MP4 并验证时间线。未发现提前报成功路径。 |
| 异常／permit／native owner | watchdog 只报告失败，不并发释放仍被 GL 使用的 Surface。drain 等待 `finishInput()` 确认后清理 codec、Surface、muxer，最后释放 permit；阻塞的 native owner 持续占用配额，符合有界失败隔离设计。调用者必须在销毁 EGL window 后执行 `finishInput()`，这仍是明确的外部合同。 |
| MP4 时基读取 | [MotionMp4Inspection.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionMp4Inspection.kt) 从唯一 `vide` track 的 `mdhd` 读取 timescale；v0/v1 偏移正确，box 大小及数量有界。Extractor 再确认唯一视频轨、非空样本及实际时间。没有用声明帧率或 Recorder 回调代替时基。 |

新增 `MotionEncoderInstrumentedTest` 确实使用生产 EGL Surface 编码器，故意丢弃首个输入，再验证保留起点、完整窗口并实际解码一帧；其 sensor 关联明确为合成数据，未冒充 CameraX 同域证明。

**验证限制：**本 Reader 未执行测试，不能将上述源码修复及新增用例记为运行 Pass。完整接线仍在执行中，本轮未重复列为缺陷。全程未写文件、构建、操作设备或派发任务。
~~~~

## Reader 原文归档 01a096ba-bca6-7202-aa36-a086f5337b79

~~~~text
**已定位到可复现的量化机制：当前“一 tick＋1μs”规则会拒绝符合 AOSP 时间表生成行为的样本。target-31 本身不足以确定分支；新生成的 target-32 已提供关键实测值。**

1. **实际拒绝证据**

   [target-31.txt](/E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/target-31.txt:13) 只有 `unverified retained mux timeline`，没有 count、PTS 或 timescale。

   [target-32.txt](/E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/target-32.txt:13) 明确记录：

   ```text
   mux PTS discrepancy at 52:
   actual=1733800, expected=1733816, timescale=90000
   ```

   偏差为 **−16μs**，当前阈值为 `ceil(1_000_000/90000)+1 = 13μs`。按 [verifyMux](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionFrameTimeline.kt:86) 的分支顺序，这次已经通过数量一致、首帧零点/关键帧、sensor 关联、单调性和关键帧标志检查；**完整数量及 PTS 序列仍未记录**。

2. **官方实现与推导**

   AOSP 先计算 `Q(tᵢ)−Q(last)`，其中 `Q(t)=floor((t×S+500000)/1000000)`；随后允许小于 100μs 的帧间隔合并，并把调整后的时间写入 `last`。因此，未合并时舍入差可相消；合并后整数微秒换算与 tick 再舍入可能留下累计残差。它既不是单纯绝对 PTS 舍入，也不是逐帧独立舍入间隔。视频还需考虑 DTS、`ctts` 与编辑表。[AOSP MPEG4Writer.cpp，4154–4211 行及视频时间处理](https://android.googlesource.com/platform/frameworks/av/+/master/media/libstagefright/MPEG4Writer.cpp#4154)

   我用内存 JavaScript 按该整数运算规则，重放测试的输入序列，假设保留原输入 1–119、无额外丢帧且 DTS=PTS，得到：

   | 保留样本索引 | 输入归零 PTS（μs） | 推导容器时间（μs） |
   |---|---:|---:|
   | 51 | 1699983 | 1699977 |
   | **52** | **1733816** | **1733800** |
   | 53 | 1766149 | 1766133 |

   **索引 52 与实测完全吻合**，支持上述机制解释。尚未取得实际 `stts/ctts/elst` 和运行系统对应源码版本，不能宣称完整文件已逐项证明。

3. **建议的可证实校验规则**

   - 保留现有帧数量、顺序、关键帧、精确 sensor 关联和丢首帧归零约束。
   - 补齐完整 written PTS、实际样本 PTS、timescale 和时间表证据；当前 [MotionMp4Inspection](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionMp4Inspection.kt:11) 只读取 timescale 与 extractor 样本。
   - 对已确认适用的 muxer 行为，用**输入账本推导容器时间序列**并逐样本核对；不能只证明容器内部自洽。不要直接改成 `N×tick` 或任意固定容差。
   - 验证后继续使用实际 mux PTS 计算窗口；量化规则不能代替产品封面容差决策。

4. **必须保留的回归**

   [MotionFrameTimelineTest](/E:/Code/Agent/photo-coach/androidApp/src/test/java/com/photocoach/app/camera/MotionFrameTimelineTest.kt:20) 的 `0/1/2/3秒 → 0/2/4/6秒` 必须继续拒绝；增加本次 VFR 全序列的独立容器证据 fixture，以及时间拉伸、样本错配负例。继续保留丢首输入、首保留关键帧归零和 sensor 身份断言。现有生产测试使用相同颜色帧，只证明可解码，不能单独证明逐帧视觉身份。

**Changed：none。Validated：只读源码/日志、官方源码核对及内存数值重放。NotRun：构建、设备测试、完整 MP4 时间表核对、目标机与产品验收。**
~~~~

## Reader 原文归档 01a096ba-bca6-7202-aa36-a086f5337b79

~~~~text
已纳入补交报告。结论不是由 −16μs 单点直接推断：

- 按 AOSP 的 `stts` 合并及整数舍入规则，重放测试输入，索引 52 恰好得到 **1733800μs**，与实测吻合。
- 该重放假设无额外丢帧、DTS=PTS；完整 written/actual 序列及实际 `stts/ctts/elst` 尚缺，故目前是**有数值复现支持的机制解释，尚未完成根因闭环**。
- 不建议据此直接放宽容差；应先补齐序列与容器证据，并保留倍速拒绝、丢首帧和身份关联回归。
~~~~


## Reader exact final 01a096bf-b0b4-7c12-9e19-8049bf71191a

~~~~text
只读快照：**2026-09-13 01:53（北京时间）**。发现两项确定的恢复逻辑缺陷；未发现正常发布路径直接覆盖已公开原片。

1. **[P1] 完整 pending 文件会被误判为已发布，恢复随后删除必要源和日志。**

   [PublishedAssetVerifier.kt:99](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/PublishedAssetVerifier.kt:99) 的元数据查询没有读取 `IS_PENDING`，因此 `verifyPublished()` 只能证明内容和名称符合要求，不能证明已经公开。

   [CaptureSaver.kt:94](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CaptureSaver.kt:94) 在这项检查成功后直接返回，跳过后面的 `commit()`。

   确定触发序列：完整 Motion 文件已写入 pending 行 → commit 前进程终止 → 恢复读取该 URI → 误判已发布 → 后续阶段成功后，[InterruptedSaveRecovery.kt:406](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/InterruptedSaveRecovery.kt:406) 标记完成并删除原始 JPEG、打包源及日志。此时资产仍是 `IS_PENDING=1`，没有完成要求的公开发布。

   **修复要求：**发布校验必须确认 `IS_PENDING=0`；内容有效但仍 pending 的资产应续做 commit，再验证公开状态。回归需覆盖完整 Motion pending、普通 JPEG pending，断言最终状态、唯一 URI 和源清理顺序。

2. **[P2] 已提交 Motion 资产的恢复类型错误地依赖临时打包文件是否存在。**

   [InterruptedSaveRecovery.kt:93](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/InterruptedSaveRecovery.kt:93) 用 `packaged != null` 决定 `motionPhoto`。但打包文件位于 [CameraBinder.kt:799](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraBinder.kt:799) 的 cache 目录。

   确定触发条件：Motion 行已经 commit，日志仍保留 `pendingUri`，原始 JPEG 仍在，但打包文件已丢失。恢复将已公开 Motion 当普通 JPEG 校验；[PublishedAssetVerifier.kt:129](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/PublishedAssetVerifier.kt:129) 要求文件末尾是 JPEG EOI，正常 MP4 尾部不满足。之后 pending 续写和自动删除都拒绝操作公开行，恢复反复失败。

   **修复要求：**已知 URI 的资产类型应由持久化发布选择及实际容器核验确定，不能由临时文件存活决定。只有确需重新发布且原 Motion 行已安全处置时，才持久化 JPEG fallback。回归应覆盖“commit 后中断＋打包源丢失”，保留原 URI且不重复发布。

其他检查结果：

- **原片及单一主资产：**正常路径先完成打包，再发布一个 pending 主资产；没有先公开静态 JPEG 再覆盖为 Motion。失败回退前要求清理原 pending，公开行不会被自动覆盖或删除。
- **JPEG 压缩数据：**[MotionPhoto.kt:108](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionPhoto.kt:108) 按字节复制 JPEG，仅处理允许替换的 XMP 段，再追加 MP4；没有 Bitmap 重编码。
- **capture A/B 隔离：**保存闭包持有独立 `PendingCapture`，释放拍摄入口后仍按 captureId 保持后台租约，清理当前对象前有引用比较；本次未发现保存尾处理污染 B 的确定缺陷。新 backend 回调接线未作为完成项评判。
- **现有测试边界：**已检查的恢复测试包含损坏 Motion 回退及普通 JPEG commit 后恢复，尚未覆盖上述两个正常 Motion 中断窗口。

**Changed：none。Scope：FR-31/36，ApprovedSeparate。验证：源码与现有测试只读检查；未运行构建、instrumented、设备或产品验收。**
~~~~


## Reader exact final 01a096ce-962a-7791-b393-99782c98a630

~~~~text
只读复核快照：**2026-09-13 02:09–02:10（北京时间）**。

**1. 编码时间模型：119 样本证据闭环，2×漂移仍拒绝。**

独立读取 [原始诊断](/E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/own-encoder-timing-34.txt) 并在内存重算，结果与 [回放报告](/E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/mux-model-replay-34.txt) 一致：

| 检查 | 结果 |
|---|---:|
| 样本 / stts 数量 | 119 / 119 |
| 模型与实际 stts 不匹配 | 0 |
| 模型与实际 PTS 不匹配 | 0 |
| 实际 PTS 全部乘 2 后不匹配 | 118 |
| 实际 stts 全部乘 2 后不匹配 | 119 |

实际 `ctts` 全零、`elst` 为空。测试 fixture 与原始诊断 SHA-256 完全一致：`D184415B…BB08F2D6`。

[MotionMuxTimingModel.kt:30](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionMuxTimingModel.kt:30) 同时要求完整时间表和逐样本 PTS 匹配；[MotionFrameTimeline.kt:104](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionFrameTimeline.kt:104) 使用该模型前仍检查数量、sensor 关联、关键帧和单调性。生产编码器已传入实际 tables。未发现此处新增确定缺陷。

**边界：**原始诊断记录的是旧校验器失败现场；数值闭环不能代替修改后生产编码器的 instrumented 通过结果。本次未运行 Kotlin 测试或设备测试。

**2. pending 误判已公开：源码已修复。**

[PublishedAssetVerifier.kt:77](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/PublishedAssetVerifier.kt:77) 现在要求 `IS_PENDING=0`，缺失或未知状态也拒绝。新增 [commitVerifiedPending](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CaptureSaver.kt:121) 会先核对内容及身份，再 commit，最后验证公开状态。恢复不再仅因完整文件可读而认定已发布。

**3. 打包文件丢失导致类型误判：源码已修复。**

[InterruptedSaveRecovery.kt:94](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/InterruptedSaveRecovery.kt:94) 已移除 `packaged != null` 对资产类型的影响；缺打包源时先核验已知 URI，成功后保留原 URI。JPEG fallback 仍须先安全处置未提交行并持久化选择。

**4. [P2] 两个新增 Motion 恢复测试存在确定的准备阶段错误，尚不能提供闭环证据。**

[InterruptedSaveRecoveryTest.kt:257](/E:/Code/Agent/photo-coach/androidApp/src/androidTest/java/com/photocoach/app/camera/InterruptedSaveRecoveryTest.kt:257) 的 fixture 将 `packaged` 创建为非空的三字节损坏文件。新增测试在第 22、48 行直接把它传给 assembler，而 [MotionPhoto.kt:81](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionPhoto.kt:81) 明确要求输出不存在或为空。

因此两个测试的 Motion 分支会在打包时抛错，尚未进入恢复断言。应为正常 Motion 场景准备新的空输出，保留损坏 fixture 给原有损坏回退测试；无需放宽 assembler 保护。

**Changed：none。两项恢复缺陷源码已修复，测试闭环待上述准备错误修正并实际运行。未执行构建、设备或全量验收。**
~~~~


## Reader exact final 01a096da-2299-7000-b71e-0eff09049e40

~~~~text
只读快照：**2026-09-13 02:21–02:22（北京时间）**。确认旧 pbuffer 泄漏，并发现另外三项确定的边界缺陷。

1. **[P1] 输出重协商丢失旧 pbuffer 句柄。**

   [MotionCameraEffect.kt:136](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionCameraEffect.kt:136) 在非 closing 的输出关闭回调中只清空 `output/pool`，没有销毁 `copySurface`。下一次输出在第 133 行直接覆盖该字段；最终关闭也只能销毁最后一个句柄。

   **触发：**同一 processor 完成一次旧输出关闭，再接收新输出。重复协商会累计未销毁 pbuffer。

   **修复边界：**在 input GL owner 上停止旧输出取帧，销毁并清空对应 pbuffer，再允许安装新输出；分配失败也应清理本次已创建资源。

2. **[P1] 输出尺寸变化后，新帧仍可能送入旧尺寸编码器。**

   [输出关闭回调](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionCameraEffect.kt:134) 没有结束当前 encoder 或使 timeline 失效。新输出建立新尺寸 pool 后，第 161 行继续向原 encoder 提交帧；[renderEncoder:179](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionCameraEffect.kt:179) 却按旧 encoder 的宽高上传。

   `MotionPixelPool.Lease` 只有 `sensorNs/pixels`，没有尺寸或输出代次，消费者无法拒绝这种错配。**能确定的是像素缓冲解释尺寸不一致；具体 GL 错误或画面表现未运行验证。**

   **修复边界：**最小方案是在输出重协商时使当前录制失败并结束旧编码所有权，重新建立录制。若支持无缝切换，则帧必须携带尺寸和代次，旧队列帧必须被明确拒绝并释放。

3. **[P2] 编码器异步初始化失败会遗漏本次 `done`。**

   [start:73](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionCameraEffect.kt:73) 把初始化投递到 encoder worker，但外层 `try/catch` 捕获不到该任务的异常。若 renderer 创建、`encoder==null` 检查或 `MotionSilentEncoder` 构造失败，[MotionGlWorker.kt:65](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionGlWorker.kt:65) 只调用全局失败处理，不调用该次 `done`；此时尚无 encoder 实例负责完成回调。

   **修复边界：**给每次 start 建立一次性完成所有权，在 worker 内覆盖全部初始化失败；等待输出的 `pendingStart` 被 finish/close 取消时，也应完成取消结果。上层是否另有补偿尚未检查，不能直接宣称产品永久挂起。

4. **[P1] closing 时的拒绝执行兜底可能在外线程执行 GL 释放。**

   [executor:45](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionCameraEffect.kt:45) 对任意 `RejectedExecutionException`，只要 closing 就直接 `task.run()`。但 worker 的拒绝原因还包括 **8 个控制任务配额耗尽**，并不代表 input owner 已终止。

   **触发：**closing 已设置、input worker 控制队列满，此时 CameraX 的 provideSurface 完成回调被拒绝。兜底直接执行 [releaseInput:191](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionCameraEffect.kt:191)，其中包含 `inputGl.current()/deleteTexture()`，破坏 GL 单线程所有权。

   **修复边界：**关键归还回调需要保证投递到 owner 的关闭通道；不能把可能执行 GL 的 Runnable 作为通用外线程兜底。

已核对的正向边界：三个 renderer 分属独立 worker；帧队列采用单在途加最新待处理帧；替换、拒绝和关闭路径会释放 lease；pool 关闭不会提前释放仍被消费者持有的字节。现有 worker 测试覆盖关闭顺序及 lease 回收，未覆盖上述 processor 重协商和初始化失败。

**Changed：none。未构建、未运行测试或设备；不评价尚未完成的生产接线与最终验收。**
~~~~


## Reader exact final 01a096e2-55c8-7852-962a-90f07620a204

~~~~text
只读快照：**2026-09-13 02:30–02:32（北京时间）**。确认 ViewPort 裁剪遗漏，并发现失败分类会丢失普通 JPEG 回退或保存重试机会。

1. **[P1] Live JPEG 路径忽略 `cropRect`，不能保证成片符合已选画幅及 ViewPort。**

   [CameraBinder.kt:280](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraBinder.kt:280) 建立 `FILL_CENTER` ViewPort，实际绑定也将它应用于全部用例。但 [CameraJpegCapture.kt:21](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraJpegCapture.kt:21) 只复制完整 JPEG plane，从未读取 `image.cropRect`。

   CameraX 文件保存 API 会处理适用的 ViewPort 裁剪；切换为内存回调后，不能假定完整 JPEG 已完成相同裁剪。[官方 ImageCapture 文档](https://developer.android.com/reference/androidx/camera/core/ImageCapture)

   **确定触发条件：**平台返回非全幅 cropRect。此时 helper 保存完整区域，偏离现有 UX-21“成片画幅匹配选择”的要求，也可能使封面与取景范围不一致。分辨率比例选择不能证明 cropRect 必然全幅。**尚无目标机数据证明每次 16:9 都触发，不能如此宣称。**

   修复必须同时满足既有 ViewPort 与原片压缩数据保留约束，不能直接以 Bitmap 重编码或忽略裁剪作为默认解决方案。

2. **[P2] 时间锚点无效会丢弃已捕获图像，而非仅取消 Live。**

   [CameraJpegCapture.kt:19](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraJpegCapture.kt:19) 把 `sensorNs<=0` 与 JPEG 不可保存合并处理，在复制前报错；随后 [CameraBinder.kt:545](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraBinder.kt:545) 删除文件和 journal，报告捕获失败。

   对“JPEG 有效、时间戳不可用于 Live”的输入，代码确定不会保存普通照片。这与当前“时间关联未知回退 JPEG”合同不符。应将静态图保存结果与 Live 时间锚点有效性分开，沿用本次 captureId，不重新拍摄。该异常输入未在设备上观察，属于明确的失败路径缺陷。

3. **[P2] 已落盘 JPEG 的后处理失败仍走捕获失败清理。**

   完整 JPEG 已写入并同步后，EXIF 处理仍可能失败；helper 将所有异常统一转为 `onError`，上述 Binder 分支随即删除源和恢复记录。

   应区分“尚无完整源”与“已有可恢复源但保存处理失败”。后者应保留必要源并提供保存重试。EXIF 写入本身可能破坏文件时，也不能未经核验就把修改中的文件当作唯一恢复源。

其他核对结论：

- **时间来源正确：**字节和 timestamp 来自同一个 ImageProxy，优于猜测 STILL 回调。官方允许比较同一相机实例所绑定用例的图像时间戳；仍需保留当前相机/录制代次关联。[官方回调契约](https://developer.android.com/reference/androidx/camera/core/ImageCapture.OnImageCapturedCallback)
- **方向：**将 `rotationDegrees` 映射为 JPEG Orientation 符合该回调的旋转语义，未发现确定的双重旋转问题。当前没有镜像输出要求或前摄路径证据，不新增镜像缺陷。
- **资源：**复制和 EXIF 异常被捕获后会执行 `image.close()`，成功路径关闭文件并沿用原 captureId。时间戳读取位于保护块外、close 也不是 finally，生命周期保障仍可收紧。

最小平台验证应包括：

- **两画幅 × 四方向**：记录实际尺寸、cropRect、rotationDegrees、EXIF；用边缘标记核对保存区域与 ViewPort，覆盖至少一个非全幅 cropRect。
- **同一 JPEG 的 Live 成功与回退**：核对 timestamp/代次、JPEG 压缩图像数据保留、最终只有一个主资产。
- **失败注入**：无效时间戳、复制失败、EXIF 阶段失败；断言 ImageProxy 关闭、必要源保留、同 captureId 重试且不重新捕获。
- **捕获中重绑/退出后再拍 B**：旧回调不得将时间戳或源归给 B，恢复不自动补拍。

**Changed：none。未构建、未运行平台或设备测试；上述源码结论不代表 Live 或目标机验收通过。**
~~~~


## Reader exact final 01a096ea-da68-7ef0-823b-e807d05df373

~~~~text
只读快照：**2026-09-13 02:39–02:41（北京时间）**。GL 四项问题已在源码层修复；JPEG 首次处理已接通，但资源与恢复尚未闭环。

**GL 四项修复**

- 旧输出关闭时在 input owner 上销毁并清空 `copySurface`，不再被新句柄覆盖。
- `hadOutput` 拒绝复用 processor 接受第二次输出协商，避免新尺寸像素进入旧编码器。
- start 使用一次性完成回调，encoder worker 内初始化异常及等待输出期间取消均有完成路径。
- Surface 归还与 close 使用独立 `ownershipExecutor`，不受普通控制队列的 8 项配额影响；普通执行器的外线程兜底要求 owner 已终止。

对应 [MotionCameraEffect.kt](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionCameraEffect.kt:49)、[MotionGlWorker.kt:49](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionGlWorker.kt:49)。这些是源码结论，未运行生命周期测试。

**JPEG 已落实的部分**

`CameraJpegCapture` 先持久化准备记录，再保存 raw；`JpegPreparation` 在首次原片保存阶段应用 cropRect、方向并原子替换输出。Motion assembler 没有再次解码或重编码。无效时间戳现在变为 `null`，不再阻止 JPEG 保存。异常路径通过 finally 关闭 ImageProxy，准备失败保留 raw。

仍有以下确定缺陷：

1. **[P1] 裁剪解码无像素上限，恢复准备也绕过存储预算。**

   [JpegPreparation.kt:34](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/JpegPreparation.kt:34) 直接全尺寸 `decodeRegion`，只验证矩形合法，没有像素/内存上限；64MB 压缩文件限制不能约束解码内存。

   [InterruptedSaveRecovery.kt:82](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/InterruptedSaveRecovery.kt:82) 在申请任何 `PendingSourceStore` 预算之前调用 `prepare()`。恢复时原拍摄 reservation 已不存在，仍会创建准备输出和解码缓冲。

   应在首次及恢复处理共用明确资源准入；超过上限须保留 raw 并报告可恢复失败，不能静默降采样原片。

2. **[P2] 清除准备记录后、删除 raw 前中断，会留下永久失联源。**

   [CameraJpegCapture.kt:33](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraJpegCapture.kt:33) 和恢复路径均先写 `jpegPreparation=null`，随后删除 raw。若两步之间进程终止，journal 不再保存 rawPath；[cleanupRecoveredRecord:425](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/InterruptedSaveRecovery.kt:425) 也不清理 raw。

   `jpeg-prepare-*` 临时文件同样只靠 finally 删除，进程终止时没有记录可供恢复清理。它们位于持久暂存目录，会持续占用配额。需要可恢复的清理状态或明确的事务文件归属规则。

3. **[P2] 有恢复源的准备失败仍报告为捕获失败。**

   [CameraBinder.kt:542](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraBinder.kt:542) 已保留准备记录，但第 550 行仍调用 `onCaptureError`；没有将本次对象接入 `pendingCapture`，普通 `retrySave` 也无法接手。

   因此，“源不再被删除”已修复，“保存失败分类及原 captureId 保存重试入口”尚未闭环。磁盘恢复可以重做准备，不能据此认定当前失败交互正确。

4. **[P2] 裁剪分支丢弃原 XMP，可能绕过既有 Motion 保守回退检查。**

   裁剪后 JPEG 是新编码文件，[JpegPreparation.kt:42](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/JpegPreparation.kt:42) 只复制列出的 EXIF 标签，没有保留或记录原 XMP。若 raw 带普通或混合 Motion XMP，准备成功后 raw 被删除，assembler 只看到已无该元数据的 JPEG，原定“检测后回退”的条件消失。

   应在首次处理前保留这项兼容性判断结果，不能通过裁剪丢掉元数据后将其视为可安全封装。

**兼容与验证边界**

- 新增 `jpegPreparation=null` 默认值支持读取缺字段的旧记录；当前 schema 仍为 3。未发现新版本读取旧记录的确定故障，但未运行迁移测试。
- 正常生产 raw/output 都由 capture 专属持久路径生成，未发现跨 capture 路径混用。
- 新测试覆盖两种裁剪比例、四方向、EXIF 注入失败及“输出替换后、清除记录前”恢复；未覆盖上述预算、失联清理和 XMP 分支。

**Changed：none。未构建、未运行测试或设备；不作最终验收结论。**
~~~~


## Reader exact final 01a096ef-2545-7673-9cc7-b7323083137e

~~~~text
**第 42 轮尚未执行快门，不能把测试失败归因于 JPEG 保存或拍后完成回调。** 日志证明录制完成超时，但不足以确定底层阻塞原因。

**观测事实**

[测试失败](/E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/live-42.txt) 指向 `ActivityLifecycleAcceptanceTest.kt:338`，等待的是：

```kotlin
deliveredAnalysisFrames > before + 5 && guidance.shutterEnabled
```

点击快门在第 341 行。因此本轮没有覆盖 `onImageSaved` 前后的视频完成竞态，也没有验证 JPEG fallback 发布成功。

[logcat-42](/E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/live-logcat-42.txt) 中的设备日志时间如下：

| 时间 | 事件 |
|---|---|
| 18:37:57.124 | Recorder 发出 Start |
| 18:38:05.639 | Recorder 进入 STOPPING |
| 18:38:07.269 | Recorder 等待 source 非 streaming 超时，强制停止 codec |
| 18:38:09.039 | Controller 报“Live 完成超时” |
| 18:38:09.358 | Recorder 报编码正常结束 |
| 18:38:10.354 | Recorder 发出 Finalize |

说明 Recorder **最终能够结束，但晚于应用截止时间约 1.3 秒**。不是“设备不支持”的直接证据。日志另有 MPEG4Writer 写出 7 帧并完成文件，但没有足够的应用层标识证明当时 `completedOwn` 的具体状态。

**源码确定的问题与时序**

1. **停止定时器及完成看门狗与 Recorder 共用单线程执行器。**

   [LiveRecordingController.kt:41](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/LiveRecordingController.kt:41) 创建单线程 scheduled executor，同时交给 Recorder，并承载 8 秒停止、快门后停止和 12 秒完成超时。

   所以 Recorder 工作占用该线程时，这些期限任务也不能运行，**看门狗不能独立保障被监控任务的期限**。这是源码可证实的调度耦合；本轮没有线程栈和任务入队/执行时间，不能把全部延迟归因于它。

   最小修复边界：独立调度停止与超时，在主线程按 token 核验后改变状态；分别记录任务计划、实际触发、主线程处理时刻。不能仅扩大超时时间掩盖耦合。

2. **正常完成必须等两路结果，Recorder Finalize 延迟足以阻止完成。**

   [completeOwned:182](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/LiveRecordingController.kt:182) 同时要求 `completedOwn != null` 和 `recorderEnded`。第 42 轮在应用超时前尚无 Recorder Finalize，所以即使自有编码器已成功，也不会交付成功。

   这是已证实的等待关系，**不是单凭日志就能判定应删除的门禁**。需补记超时时两路状态，区分等待协商、等待 EOS、等待 mux 检查、等待 Recorder Finalize。

3. **JPEG 回调前的视频完成会被丢弃并转为 JPEG fallback，但不会据此永久等待。**

   [CameraBinder.kt:783](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraBinder.kt:783) 只查 `pendingCapture`，该对象在 `onImageSaved` 才建立。若视频先完成，文件进入删除分支；成功时还会尝试滚动下一段，而 `startRollingRecording()` 只检查 `pendingCapture`，没有检查 `captureInProgress`。

   随后 JPEG 回调因旧 marker 已非 current，选择普通 JPEG。这会丢弃可能可用的旧段并启动无用新段；**当前代码仍有明确 JPEG 发布分支，不能把它认定为第 42 轮超时原因。** 若要保留先完成的视频，应按快门 captureId/录制 generation 暂存有界结果，再与 JPEG 汇合。

**仍为 Unknown**

- 第 338 行未满足的是分析计数、快门状态，还是两者。
- 超时时 `pendingStart`、自有编码器完成结果和各 owner 状态。当前新增 `diagnostic()` 不在第 42 轮证据中，不能倒填。
- Recorder 为何晚结束，以及独立执行器是否足以解决。
- 第 41 轮 fallback Pass 不能证明第 42 轮这些分支通过。

当前 `pendingStart` 的 finish/close 取消和初始化异常已有一次性完成路径，本次未发现仍会遗漏 `done` 的确定新证据。

**Changed：none。只读日志和源码；未构建、未操作设备，未作整体验收。**
~~~~


## Reader exact final 01a096f9-0fa9-7a80-be46-921cea31edb1

~~~~text
只读快照：**2026-09-13 02:55–02:57（北京时间）**。主要修复已落实；仍有两处 JPEG 边界未闭环。第 45 轮 **18/18 Pass** 不能覆盖全部新路径。

| 项目 | 当前源码结论 | 测试证据边界 |
|---|---|---|
| 裁剪内存与恢复配额 | 已增加像素准入；恢复 `prepare()` 包在 live 存储 reservation 内。当前保存失败保留 `pendingCapture`，原 reservation 不释放，重试可以继续使用 | 测试验证了指定像素上限拒绝；“满配额恢复”用例测试的是派生图生成，**不是 JPEG preparation** |
| raw/temp 失联 | `jpegRawPath` 独立持久化，清除 preparation 后仍保留；临时输出改为事务固定路径；恢复清理包含二者 | 固定临时文件重放、准备阶段恢复已覆盖；清理失败见下文 |
| 当前保存重试入口 | 准备失败改为 `onSaveError`，保留同一 `PendingCapture`；`retrySave → publishPrimary → prepare` 不重新捕获 | helper 重试通过；**未见 Binder 实际失败→点击重试用例** |
| XMP 资格 | 裁剪前检查 raw，结果持久化；Binder 的 `liveWillPackage` 检查 fallback；恢复遇到资格未知时重新检查 raw | 源码路径完整；**未见带 XMP 的裁剪→拒绝 Live 测试** |

相关实现：[JpegPreparation](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/JpegPreparation.kt:23)、[恢复准备](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/InterruptedSaveRecovery.kt:82)、[Binder 保存重试](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraBinder.kt:557)。

**仍有两项确定问题：**

1. **[P2] 无需解码的全幅 JPEG 也被裁剪内存门槛拒绝。**  
   [JpegPreparation.kt:24](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/JpegPreparation.kt:24) 在判断是否裁剪之前执行像素上限检查。完整画幅分支随后只是文件复制，并不分配该尺寸 Bitmap。超过上限的高分辨率 JPEG 因此无法完成原片保存，重试也会重复拒绝。应把解码像素门槛限定于实际 `decodeRegion` 分支，保留文件大小和存储准入。

2. **[P2] 正常成功路径仍忽略 raw/temp 删除失败，然后删除 journal。**  
   [CameraBinder.kt:935](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraBinder.kt:935) 未检查 `File.delete()` 返回值，随后删除日志。任一删除失败，raw/temp 仍会失去恢复归属。恢复路径已改为检查删除结果并保留失败记录，正常成功路径应使用同样规则。

**Live Controller 的限定结论**

[LiveRecordingController](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/LiveRecordingController.kt:42) 已真正分离 scheduled executor 与 Recorder executor：

- 自有编码结果直接结束对应 session 并调用 `onFinalized`，不再等待 Recorder Finalize。
- Recorder lease 保留到其 Finalize；同 controller 在 lease 未归还时拒绝新 start，全局 semaphore 限制为两份。
- 晚到 Finalize 先释放 lease，再调用 `onRecorderReady`；Binder 接回 `startRollingRecording`。
- Binder 续录检查 `pendingCapture/captureInProgress/released`，避免 JPEG 尚未返回时启动新段。
- 若 Finalize 先到，自有完成回调随后仍能交付结果；没有看到因此遗漏续录的确定分支。

**测试边界：**target-45 的 Live 用例允许“Motion 成功或明确 JPEG fallback”；它不单独证明正常 Motion 发布、Recorder 延迟归还期间的禁止新起、或 Finalize 后连续续录。未见这些时序的专门断言。

**Changed：none。**本次只读源码和已有报告，未构建、未运行设备或全量验收。
~~~~

## Reader exact final 01a09734-ab0e-7822-9775-e75a01d853af

~~~~text
**最终只读复核完成：本轮不能判为全部通过。确认 1 项可修复代码缺陷、1 组当前状态文档错误；已列测试结果可以接受，实际 CameraX Live 正向链路仍未闭环。**

1. **[P1] 放弃标记写入失败后，界面仍结束待保存状态。**  
   [DiscardCaptureSources.kt:12](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/DiscardCaptureSources.kt:12) 首先写入 `discarded=true`。若此处因存储满或 I/O 错误失败，[CameraBinder.kt:642](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraBinder.kt:642) 捕获错误后仍清空 `pendingCapture`、释放持有资源；[MainActivity.kt:231](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/MainActivity.kt:231) 随后无条件调用 `abandonSaveFailure()`。旧日志仍为 `discarded=false`，后续恢复可能发布用户以为已放弃的照片。  
   应区分“放弃意图尚未持久化”和“意图已持久化、清理待重试”；前者必须保留可操作的失败状态，不能提示“已放弃”。现有 `discardCleanupFailureRetainsTombstoneAndRecoveryNeverPublishesIt` 只覆盖标记成功后的删除失败，需补首次标记写入失败及 Activity 状态回归。

2. **[P2] 汇总和权威入口仍把历史结论表述为当前状态。**  
   [completion-2026-09-12.md:5](/E:/Code/Agent/photo-coach/docs/traceability/completion-2026-09-12.md:5) 全文没有本轮收口补充：第 33～40 行仍把 **266 JVM、Instrumented NotRun** 列为最终证据，第 47 行起仍列旧 APK 哈希，第 57 行称生产锚点为空，第 62 行称没有审核任务运行。  
   [controller-workflow-2026-09-12.md:14](/E:/Code/Agent/photo-coach/docs/traceability/controller-workflow-2026-09-12.md:14) 顶部阶段状态及第 62 行仍停留于 B3/C3；末尾恢复说明不足以构成当前收口记录。  
   [requirement.md:326](/E:/Code/Agent/photo-coach/docs/requirement.md:326)、[architecture.md:165](/E:/Code/Agent/photo-coach/docs/architecture.md:165) 仍用“时间锚点阻塞”解释当前 Live。应保留历史、明确标注历史区间，并将当前结果统一为：方案 C 已接线，合成正向通过，实际 CameraX 完成超时回退 JPEG，根因 **Unknown**。

本轮关键修复的复核结论：

- **JPEG 准备与恢复**：整帧复制已绕过 Bitmap 裁剪内存门禁；裁剪保持原分辨率、确定性临时文件及 raw 重试源；XMP 资格在裁剪前检查并持久化。Binder 同 captureId 重试、恢复配额、pending 提交与缓存包丢失后的同 URI 恢复均有对应证据。成功保存后清理失败保留日志的问题已修正。
- **Live 资源与身份**：公开 `VIDEO_CAPTURE` effect、三 GL owner、不可变帧副本、独立 Recorder executor、退休录制配额、同 ImageProxy JPEG 时间戳及 generation/source 校验已接线。此前指出的输出重协商、GL 释放归属和编码启动失败通知问题已修正；除上述放弃保存问题，未确认新增可修复代码缺陷。
- **实际 Live 仍未通过**：57 轮原始诊断明确记录完成超时，随后 JPEG 回退。`inputs=1、outputs=1、recorderFrames=29、encoderFrames=17` 只能证明实际双路曾收到帧，不能证明完整 Live 成片；也不能据此归因硬件或环境。

证据与范围核对：

| 项目 | 独立核对结果 |
|---|---|
| 逐项矩阵 | 228 行、228 唯一 ID；38 项 FR Scope 与权威矩阵一致 |
| 方法索引 | 341 条 JVM、145 条 AVD 记录，与 93 份原始报告逐项核对，结果错配 **0**；AVD 记录含重复轮次 |
| JVM | Coach **128**、Android **213**，失败/错误/跳过均 0 |
| AVD | 55 轮 **99/99**；57 轮 **33/33**，不能相加 |
| 其他原始记录 | 六研究配置各 1/1、NONE 恢复 1/1、首次断网 2/2、跨进程恢复两阶段各 1/1 |
| 索引导航 | 574 个方法索引锚点引用均存在 |

当前逐项矩阵对合成研究、AVD、目标机及 Scope 的区分基本合格，但不能据此宣布“所有非真机验收完成”。除新增放弃写盘失败回归外，文档已明确保留的实际系统设置返回/运行期撤权、MainActivity 多轮编辑成片等完整链路仍未执行；实际 Live 正向失败也仍需处理。跨进程恢复证据不等于在写入瞬间强杀成功，恢复发布时间仍为 Unknown。

**Changed：none。** 本 Reader 仅做源码、规范、XML/日志与索引只读检查，未写入、构建、操作设备或启动任务。规范与执行规则沿用本轮已读取的产品/架构入口、相关需求及验收附件、FR/冲突矩阵和 `photo-coach-coding` references。目标小米 14 Pro/HyperOS、颜色与热量、时基/封面容差及正式研究仍为 NotRun/Unknown；P0 保持 GateLocked，不构成 Go。
~~~~


## Reader exact final 01a09741-d174-72d0-bb1a-f0b99c5eb675

~~~~text
**已确认存在起算语义混淆：控制器把协商、排队和编码器初始化耗时计入了“8 秒录制片段”。但这不足以解释全部 Live 失败，也不能据此直接移动或放宽 12 秒总期限。** 本结论仅针对读取时的代码和 59 轮 PID10999，不是最终验收。

| 事件 | 日志时间 | 距控制器启动 |
|---|---|---:|
| 控制器启动 | 20:10:49.398 | 0 |
| 安排 8/12 秒任务 | 49.427 | 29 ms |
| Recorder Start 回调 | 54.016 | 4.618 s |
| 自有编码器开始构造 | 54.289 | 4.891 s |
| 8 秒 scheduler 触发 | 57.558 | 8.160 s |
| 自有 codec 启动完成 | 57.654 | 8.256 s |
| 主线程请求停止 | 58.138 | 8.740 s |
| 自有编码器 EOS，保留 2 样本 | 20:11:00.569 | 11.171 s |
| Recorder Finalize error=8 | 01.498 | 12.100 s |
| 自有 MP4 校验成功 | 02.711 | 13.313 s |

证据来自 [live-stages-59.txt](/E:/Code/Agent/photo-coach/androidApp/build/reports/non-device-2026-09-12/live-stages-59.txt)。

**确定的问题与最小修复语义**

- [LiveRecordingController.kt:127](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/LiveRecordingController.kt:127) 在异步 `processor.start()` 提交后立即安排 `+8000` 停止。该事件只表示请求已提交；本次停止任务甚至早于 codec 就绪。应明确区分 `Starting → Recording → Finishing`。
- **8 秒媒体限制**继续按本代视频输入的 sensor/PTS 跨度执行；录制运行计时可在自有 codec、EGL 输入就绪且首帧实际提交后启动一次。这个单调时钟事件只用于资源计时，**不能充当 sensor/media 锚点**。预缓冲、后缓冲和封面仍必须以实际保留并验证的样本判断。
- **最小安全改法保留请求起算的 12 秒绝对总期限**，同时修正上述 8 秒起算。启动过慢耗尽总预算时，明确报告“启动/完成预算不足”并回退 JPEG，不能保证每次都有完整 8 秒录制机会。若要改成“启动另有预算，再录制和完成 12 秒”，那是在改变总耗时合同，应明确登记，不能作为无声的局部修复。
- [MotionSilentEncoder.kt:85](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionSilentEncoder.kt:85) 另从 drain 启动计算 12 秒。建议传递同一 generation 的绝对截止时间，让内部 watchdog 不晚于外层期限；终态接受也检查截止时间，避免仅依赖主线程延迟执行的定时回调。超时只撤销发布资格，原生资源仍由 owner 实际退出后释放。
- 所有就绪通知、定时停止和完成通知必须核对 generation，并保证一次终态；缓冲未就绪时普通快门仍立即可用。

**不能从这段日志推出的结论**

- 本轮首先使 session 失效的是 **Recorder error=8**；12 秒主线程任务到 `02.740` 才运行。不能把本次失效直接记为“12 秒 watchdog 首先触发”。
- 自有编码器后来确实完成 MP4 校验，但仅有 2 个保留样本，日志未给出完整快门窗口覆盖；`success=true` 不等于 Live 成片通过。
- 初始化为何耗时、首个实际提交帧的时刻、两个样本的 sensor 跨度、Recorder error=8 的底层原因，以及分析等待失败与录制停止的因果关系，仍为 **Unknown**。不能归咎模拟器或硬件。

最小回归应覆盖：延迟启动不提前消耗媒体片段计时、始终无就绪仍受总期限限制、首帧后仅启动一次计时、旧代次迟到回调无效，以及截止前后完成竞态。修复后仍需实际 CameraX 正向证据，不能只放宽等待断言。

Changed：none；未写入、构建或操作设备。
~~~~


## Reader exact final 01a09756-63c0-7a62-80e7-e0f778806eb6

~~~~text
本次有限源码复核发现 **2 项期限接线缺陷**。这是提前审查，未运行测试，不构成最终验收。

1. **[P2] 主线程延迟可使过期成功结果仍被接受。**  
   [LiveRecordingController.kt:133](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/LiveRecordingController.kt:133) 在投递主线程前检查 `budget.expired()`，但第 137 行之后只核对代次，没有再次核对期限。  
   可复现时序：编码结果在截止前产生并入队；主线程阻塞至截止后；成功回调排在 watchdog 回调前执行，于是 `session.finish(token)`、`onFinalized(success)` 先发生，随后 watchdog 因代次已结束而失效。这与当前“成功接受也检查期限、主线程延迟扣除预算”的合同不符。  
   **修复位置**：在主线程核对 token 后、接受成功结果前，重新检查同一 budget；过期只交付一次失败，并清理已完成的文件。应使用可控主线程队列测试“截止前产出、截止后消费、watchdog 排在后面”。

2. **[P2] 部分停止路径漏记首次 Finishing 请求时间。**  
   [MotionCameraEffect.kt:126](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionCameraEffect.kt:126) 的 `finish()` 只排队停止；媒体跨度到限会直接调用它。另有 [LiveRecordingController.kt:188](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/LiveRecordingController.kt:188) 的取消路径同样未先登记 Finishing。这些路径要等 [MotionSilentEncoder.kt:67](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionSilentEncoder.kt:67) 实际执行 `finishInput()` 才记录时间。  
   例如 ready 后第 2 秒请求取消，第 5 秒 owner 才执行输入关闭，预算会记为 `ready+17s`，而不是首次请求对应的 `ready+14s`。虽然仍受 `ready+20s` 上限约束，但漏算了停止请求后的排队时间。  
   **修复语义**：所有停止入口在排队前，用该代 budget 幂等登记首次请求；实际 EOS 处保留防御性登记。不能因此让调用线程提前释放 Surface、codec 或 permit。测试需延迟 owner 执行，验证 deadline 不随实际关闭时间后移。

其余有限检查结果：

- `MotionSessionBudget` 当前计算满足 Starting 12 秒、ready 后 Recording 8 秒、Finishing 最多 12 秒且不晚于 ready+20 秒；重复登记不会延长期限。
- ready 来自自有 codec 与 EGL window/texture 就绪，没有被用作 sensor/media 锚。
- 已检查的定时、ready 和完成回调使用 token 门禁；结果有一次性交付保护。未在这些接线中确认新增旧代覆盖新代或 watchdog 提前释放原生资源的问题。
- 现有 3 个预算测试覆盖数值边界；“其他 session”测试只是操作两个独立预算对象，**不能检出上述执行器时序，也不能证明控制器取消与一次性交付接线正确**。

指定名称实际对应 **5 个文件**，读取前后均记录 SHA-256/mtime。四个生产文件未变化；`MotionSessionBudgetTest.kt` 从 `F090A2BE…D824CDA`／UTC `20:35:44.5696653` 变为 `07F2A257…52EDD00`／UTC `20:37:14.7588463`，变化是期望值补 `L`。因此整组为**非冻结快照**；上述生产代码位置在本次读取前后保持一致。

Changed：none；未写入、构建、操作设备或创建任务。
~~~~


## Reader exact final 01a09779-4116-7bb2-9a6b-b4a96e97b6d2

~~~~text
**应先定位承载 `RangeInfo + ACTION_SET_PROGRESS` 的节点，再关联“曝光调节”；不能把内容描述节点及其子树当作滑块本体。** 当前向下递归查找的假设不成立。

依据如下：

- [CreativeResultPanel.kt:199](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/ui/viewfinder/CreativeResultPanel.kt:199) 把 `contentDescription` 加在 Material3 `Slider` modifier 上。
- 固定缓存实际为 **Compose UI 1.11.4、Material3 1.4.0**。已核对用于检查的 `classes.jar` 与对应版本 AAR 内文件哈希一致。
- 固定版本字节码中，Material3 `sliderSemantics` 设置 `SetProgress`、状态描述，并调用 `progressSemantics`。
- UI 1.11.4 的 `SemanticsNode.emitFakeNodes` 会在满足合并条件时生成独立内容描述子节点；其 lambda **只设置 ContentDescription**，虚拟节点没有子节点。因此命中它后继续向下找 RangeInfo，必然找不到。
- Android 无障碍桥从节点的 `unmergedConfig.ProgressBarRangeInfo` 设置平台 RangeInfo，并结合 `SetProgress` 暴露滑块动作；内容描述不保证与这些属性落在同一个平台节点。[官方桥接源码](https://android.googlesource.com/platform/frameworks/support/+/c86a3f34a76126c6f4d97af6771032faa5e97cec/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/platform/AndroidComposeViewAccessibilityDelegateCompat.android.kt)

**建议直接替换测试的选择策略：**

1. 找到可见的精确标签节点后，检查它自身及逐级祖先，选取第一个同时满足：
   - `rangeInfo != null`；
   - `actionList` 含 `ACTION_SET_PROGRESS`；
   - `isEnabled && isVisibleToUser`。
   
   这是按能力验证祖先，**不是硬编码 `parent` 或 `parent.parent`**。

2. 如果祖先链没有候选，再检查新取得的完整窗口树，收集上述可操作节点，并用实际树中的标签归属确认唯一候选。**bounds 只作交叉核对**：虚拟描述节点可能复用父节点边界，不能仅凭相同 bounds 或屏幕顺序确定是哪一个滑块。范围也不能单独定位，因为多个编辑字段可能同范围。

3. 对选中的节点调用 `ACTION_SET_PROGRESS`，参数仍用 `ACTION_ARGUMENT_PROGRESS_VALUE`。保留现有 VM 对应字段及其他六字段不变的断言；每轮操作后重新取得节点，避免复用重组前的快照。不要用容器滚动动作替代滑块设值。

[ActivityLifecycleAcceptanceTest.kt:84](/E:/Code/Agent/photo-coach/androidApp/src/androidTest/java/com/photocoach/app/ActivityLifecycleAcceptanceTest.kt:84) 的 `rangeWithin(named)` 仅向下搜索，是当前明确需要修正的选择逻辑。现象与上述虚拟描述叶节点机制一致；**本次尚未读到 `editor-slider-tree.txt` 实际快照，因此不声称已经证实现场具体父子层级**。如果“自身＋祖先能力检查”仍失败，应保留一次完整候选清单（class、description、RangeInfo、action IDs、bounds、可见性、节点关系）后停止该用例，避免再次整轮猜测。

本次仅只读检查。`javap` 提取到了上述字节码，但以缓存访问 `AccessDenied` 非零退出，未将命令视为验证通过；没有构建或设备执行。
~~~~

