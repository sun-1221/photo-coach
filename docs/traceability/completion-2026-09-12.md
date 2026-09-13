# 需求审核、实施与总控验收记录（持续至2026-09-13）

日期：2026-09-12启动，2026-09-13继续。总控：`01a0949d-eddb-7240-b1fc-ecb9b1a0d3b1`。本地目录：`E:/Code/Agent/photo-coach`。

## 当前状态（2026-09-13，78j 后仍非最终验收）

**换新对话请先读：[当前断点-非真机Writer-2026-09-13.md](当前断点-非真机Writer-2026-09-13.md)。** 未提交工作区才是当前源码；B3 APK/266 JVM 不是当前。

方案C已批准并接入CameraX四用例、公开VIDEO_CAPTURE effect、三GL owner和独立双编码器；同JPEG曝光timestamp与实际保留编码样本建立时间线。合成Surface输入五种变换双MP4正向通过。实际CameraX曾完成超时并明确回退JPEG，不能称时间锚点仍未实现。76/77轮已改：关联后缀可作前置缓冲、剩余媒体跨度须仍能容纳1.5s后覆盖、空闲8秒停录不切断在途覆盖、快门立即 holdForCoverage。1.5s窗口与8s媒体上限未放宽。

78j 专用 AVD（`emulator-5580` / adb `5038`）上，实际 Activity 两轮七滑块编辑/撤销/重做/对比/重置/另存已通过。实际 Live SPEED 自有20样本视频验证成功，后覆盖约0.967s不足1.5s，正确回退普通JPEG，正向成片仍未闭环。宿主报告用中文文件名：[第78j轮-编辑两轮-通过.txt](../../androidApp/build/reports/non-device-2026-09-12/第78j轮-编辑两轮-通过.txt)、[第78j轮-实际Live-SPEED-失败.txt](../../androidApp/build/reports/non-device-2026-09-12/第78j轮-实际Live-SPEED-失败.txt)。61/67/78j 已补进[用例索引](non-device-test-index-2026-09-12.md#activitylifecycleacceptancetest)；55/57 历史英文报告名不改。详细见[逐项执行记录](non-device-acceptance-2026-09-12.md)。

已有工程证据：Coach128、Android219（72轮离线验证）；55轮AVD99/99、57轮影响范围33/33；六研究变体、NONE恢复、首次断网、跨进程恢复均有实际通过报告。两组AVD有重叠，不相加。Debug/Release/lint、实际APK权限/备份通过；具体APK与SHA以[当前逐项执行记录](non-device-acceptance-2026-09-12.md)的最新完成轮次为准，下面B3产物全部为历史，不能用于当前源码。

独立Reader turn 01a09734-ab0e-7822-9775-e75a01d853af的最终报告已[逐字归档](non-device-reader-research-2026-09-12.md)：接受已有运行记录，但发现放弃保存意图首次写盘失败的P1、汇总状态陈旧、实际权限/编辑链路缺口及实际Live未闭环。放弃标记与权限设置返回已在58–67轮修复并通过；78j关闭实际编辑两轮缺口。总控未宣布全部非真机验收完成；不再使用更早2f轮或B3 APK/266 JVM替代当前源码。

67轮实际权限设置返回与拍照通过；70轮9项GL/JPEG组件回归通过。73轮七滑块字段断言曾通过、撤销定位失败；该编辑缺口以78j实际Activity用例为准，不得回写为73/76通过。

目标机/HyperOS、真实颜色/热量、封面时间容差和正式研究Go仍NotRun/Unknown。数据负责人、资料保存/删除规则需用户输入。P0 GateLocked、独立P1 ApprovedSeparate不变。无新任务/工作树/依赖升级/提交推送。

## 历史：2026-09-12 B1/B2/B3与C1/C2/C3收口

以下全文保留当时的266项、Instrumented NotRun、旧APK、空锚点与无运行任务等结论，仅适用于该历史时点，不是当前状态。
**需求逐项审核、联网核查和正式文档原位修订已完成；已发现的8项可修复代码缺口经三轮修复及独立复验关闭。整体产品任务仍受真实Live时间锚点、目标设备和正式研究输入阻塞，不能宣布全部产品验收完成或Go。**

## 逐项覆盖与当前规范

- [逐项需求审核矩阵](../research/requirements-line-audit-2026-09-12.md)：228个唯一审核ID，包括38 FR、49 UX、11指标、46未编号约束、22场景、14短口令、15信号组、12姿势、9技巧和12风格。每项都有审核结论；门禁、待冻结和待校准也逐项登记，不算通过。
- [来源覆盖索引](../research/requirements-source-coverage-2026-09-12.md)：7个规范文件、817个原文单元的需求审核阶段快照。CP-01和后续状态更新后，不声称旧行号或SHA仍对应当前文件。
- 联网检查实际使用官方资料，20项来源索引与用途分别见[首轮研究](../research/requirements-audit-2026-09-12.md)和[逐项矩阵](../research/requirements-line-audit-2026-09-12.md)。权限复核另查固定依赖与官方Manifest/ML Kit/WorkManager资料，见[权限审核](permission-audit-2026-09-12.md)。研究不是设备验证。
- 正式需求已直接更新[产品总纲](../requirement.md)、相应需求/架构附件、[UX验收规范](../acceptance/acceptance-plan.md)及[FR追踪矩阵](requirements-matrix.md)，不是仅追加建议报告。当前Delivery和Verification已回写FR表；历史证据按日期保留。
- CP-01采用用户决定：竖屏默认字号操作区约占完整屏幕高度的五分之一（20%）。源码已核对；系统栏测量、大字号和横屏仍需目标机验证。

## 审核与修复闭环

| 阶段 | 实际结论 | 证据 |
|---|---|---|
| A 逐项源码审查 | 228个ID及12个优化组已审核，找到明确实现缺口 | [审核原文](code-audit-2026-09-12.md) |
| B1 → C1 | 第一轮修复后仍发现7项，未接受首次Fixed申报 | [首轮复验](code-reaudit-2026-09-12-round1.md) |
| 权限专项 | 实际APK含5项额外系统权限，纠正仅根据源Manifest的CAMERA-only判断 | [固定依赖复核](permission-audit-2026-09-12.md) |
| B2 → C2 | 7项及权限共8项修复后，仍剩重试回调漏传身份这1项P1 | [第二轮复验及补充](code-reaudit-2026-09-12-round2.md) |
| B3 → C3 | 重试启动时固定身份、Binder核对、统一失败刷新；定点复验通过，未发现直接回归 | [最终独立复验](code-reaudit-2026-09-12-round3.md) |

结合C2和C3，关闭的8项为：原片后新拍与旧保存隔离、研究快速再拍准备、研究指导起算、STATIC实时警告隔离、锁定超时通知、必要姿势证据失效、导出/恢复统一配额、实际APK权限最小化。O01～O12的其余已检查工程路径未发现新增回归；O09完整Live仍明确Blocked，不能纳入关闭数。

代码变更和每项生产接线、回归与边界见[实施报告](implementation-2026-09-12.md)。自动化可观察行为、源码符合性、设备效果分别报告，不使用“8项关闭”推导全部需求通过。

## 实际验证

| 层 | 最终证据与限制 |
|---|---|
| Coach JVM | 82项通过；模块第三轮未改，第三轮task为UP-TO-DATE，保留有效XML，不称第三轮重跑 |
| Android JVM | 第三轮实际执行184项通过，包含2项生产重试回调分发回归 |
| JVM总数 | 266项，0失败、0错误、0跳过；总控及独立审核均读取XML核对 |
| .NET | 第一轮实际3/3通过；后两轮未改服务、未重跑，不代表P0客户端已获准 |
| 构建 | 最终Debug、Release unsigned、Debug AndroidTest APK成功；Release曾一次增量打包异常，未改源码重试成功，原因仍Unknown |
| lint | lintDebug 0错误、9警告；Release lintVital通过 |
| 包权限 | Debug/Release合并清单门禁及实际APK检查通过；系统权限仅CAMERA，自有signature保护保留，指定6个后台入口移除、必要初始化保留 |
| Instrumented | 真实MediaStore、VM、Compose、Handler、恢复和A/B重试交错用例编译通过；本轮执行NotRun，未验证真实CameraX拍摄 |
| 目标机/产品 | 当前没有连接设备；首次离线推理、覆盖安装、HyperOS、布局、真实成片、长期可靠性及产品实验均未完成 |

总控独立核对最终三个APK的SHA-256、两个主APK的实际权限、测试XML及diff检查。最终源码未再修改，收口阶段仅更新文档，无需重复构建。

收口检查：228个唯一审核ID、38个FR、49个UX均保留；38项Phase/Scope与HEAD基线一致。涉及的27份Markdown中，1296个有效本地链接无缺失；逐字归档的历史正文以源码块保留，其原始链接不充当当前导航。C2两份及C3一份final的正文完整性核对3/3。`git diff --check`通过，最终`adb devices -l`仍为空。

| 最终产物 | SHA-256 |
|---|---|
| [Debug APK](../../androidApp/build/outputs/apk/debug/androidApp-debug.apk) | `A49090FF9A974DFB73509402DCE4513AA21B34BC880C7B41063CD3983FAD56CA` |
| [Release unsigned APK](../../androidApp/build/outputs/apk/release/androidApp-release-unsigned.apk) | `C7043A275E5F986ED4353A754A67140073F30759F18C8D72803B160CBB189952` |
| [AndroidTest APK](../../androidApp/build/outputs/apk/androidTest/debug/androidApp-debug-androidTest.apk) | `EEFD3835AC5BDBFA07BD283A425DBA8AB3226CC4E5D2F053CFCC5532D8D958E2` |

## 无法在本次环境关闭的项目

| 阻塞 | 当前事实 | 继续所需条件 |
|---|---|---|
| O09 / FR-31完整Live | CameraX 1.6.1 Recorder输出时间归零，公开接口没有已核验的首编码帧sensor/media锚点。生产锚点为空，正常Live也明确降JPEG；没有伪造成功 | 提供可核验的编码起点方案，冻结时长/封面误差并做目标机播放验收；涉及替换固定媒体架构/依赖的方案需另行决策 |
| 目标机与运行证据 | 无连接的小米14 Pro；权限裁剪后的首次Face/Pose、旧WorkManager/DataTransport覆盖安装、真实恢复/按键/TTS/颜色/热量均NotRun | 连接目标设备，登记系统及构建信息，按[验收规范](../acceptance/acceptance-plan.md)执行已有用例并保留真实结果 |
| 正式实验与产品校准 | 研究责任、资料访问/保留/删除、盲评汇总、平局/缺评、失败时间比较、D7窗口、可靠性样本/停止规则以及感知/颜色/媒体/热阈值尚未冻结或实测 | 按产品总纲3.3.1/3.3.2冻结方案与责任，再收集真实数据。20人×3场景×3条件为180轮，不能用示例或JVM数据代替 |
| 完整P0 | P-1 Go未成立，仍GateLocked；FR-16三张语义等保留到对应阶段 | 先完成既定P-1门槛，再决定P0专属范围和验收 |

上述前3项是当前整体未完成的具体阻塞。现在没有运行中的Writer或审核任务；总控不声明后台自动推进。收到设备、媒体方案或研究输入后，可从[总控执行记录](controller-workflow-2026-09-12.md)保存的实际任务身份继续。

## 范围、规则与变更边界

38个FR的Phase/Scope与本次工作前基线一致：P-1 InScope、P0 GateLocked、独立P1 ApprovedSeparate、后期Deferred。未升级依赖、改Skill、创建新任务/代理/worktree、提交推送、安装设备或采集正式研究资料。HEAD保持`39719dac5b499ff9bba2eb02331df35dcf05d5ae`。

Rules：使用thread-workflow-orchestrator的Native协调循环，以及photo-coach-coding；已读取产品/架构入口、FR/UX/冲突登记、交互指导/P1/自然上镜、相机感知/指导/创意保存/美颜架构及相关执行references。各轮具体读取和验证集合见实施报告。总控仅在Writer停止后回写当前状态，既有设备历史和需求审核快照没有覆盖。

