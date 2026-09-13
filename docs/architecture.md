# 拍照教练：技术架构

- 状态：已按需求拍板，第一版采用方案 A
- 更新日期：2026-09-12
- 产品依据：[产品需求总纲](requirement.md)
- 技术决定：[architecture/decisions.md](architecture/decisions.md)
- 追踪矩阵：[功能需求定义与追踪矩阵](traceability/requirements-matrix.md)

本文是系统上下文、模块边界、依赖方向、质量属性、核心运行链路、技术基线与回退策略的最高权威入口。产品范围、口令内容和验收以需求入口为准；专题不能静默改变产品 Scope 或把 Unknown/NotRun 写成通过。

## 1. 结论与系统上下文

取景器采用 Android 原生 Kotlin + Jetpack Compose + CameraX/ML Kit。P-1、完整 P0 和单独批准的 P1 只验收小米 14 Pro；其他 Android 与 iOS 没有当前兼容承诺。场景 JSON、信号和口令模型保持平台无关；iOS 只有正式立项后才新写 Swift/AVFoundation/Vision 取景器。

可选“再讲细”服务采用 C# / ASP.NET Core 10。它属于 GateLocked 的 P0，默认关闭、按需单帧、可替换供应商；P-1 飞行模式主路径不依赖该服务。

### 1.1 目标设备约束

- P-1、P0 和 P1 的唯一验收机型是小米 14 Pro；每次真机结果必须记录地区版本、Android/HyperOS 版本、Build fingerprint 和 App 版本。
- 其他 Android 机型不进入兼容矩阵、不作功能或成片质量承诺，也不为它们新增厂商分支；保留标准 CameraX 能力探测和安全回退，不设置无必要的安装白名单。
- 快捷焦段不能硬编码为 2x。启动时枚举 CameraX 可选择的后摄 `CameraInfo`，用 Camera2 `LENS_INFO_AVAILABLE_FOCAL_LENGTHS` 与 CameraX `intrinsicZoomRatio` 去掉无元数据和重复候选；按钮倍率来自相对视角能力，不读取厂商传感器 ID。候选为空、失效或绑定失败时只保留默认后摄。最终仍以小米 14 Pro 真机预览、成片视角和 EXIF 标定标签；未经真机验收不得把按钮宣称为已验证光学焦段。
- 小米系统相机公开提供的徕卡风格、可变光圈、夜景或人像能力，不等于第三方 CameraX 一定可控；只使用标准 API 实际暴露且能与实时 `ImageAnalysis` 共存的能力，不接未文档化的厂商私有接口。

系统边界：

| 外部参与者/系统 | 与系统的关系 | 当前边界 |
| --- | --- | --- |
| shooter / subject | 选择意图、接收动作、手动快门 | P-1 只支持一名 subject；proxy 仅 P0 简化页 |
| 小米 14 Pro 相机栈 | CameraX/Camera2/ML Kit/Extensions 提供标准能力 | 只使用实际报告能力；不调用小米私有接口 |
| Android MediaStore / 系统相册 | 发布本 App 新拍的照片并执行受控拍后操作 | 不读取整本相册；HyperOS 行为仍需真机 |
| 系统 TTS | 中文当前口令输出 | 优先离线音色；失败不能阻断拍照 |
| ExplainApi / 外部视觉供应商 | P0 用户单独同意后的可选讲解 | 不连续上传、不落库、失败隔离 |
| 研究数据导出 | 计算 P-1 指标 | 只含非图像最小事件，不含帧/坐标/身份 |

## 2. 真实模块边界与依赖方向

| 模块 | 归属 | 不得承担 |
| --- | --- | --- |
| [androidApp/](../androidApp) | CameraX、Compose、权限、TTS、MediaStore、端侧感知、设置、研究事件、P1 Android 图像处理与 Motion Photo | 不把 Bitmap/Context 倒灌平台无关模型；不把供应商网络变成主路径 |
| [coach/](../coach) | 场景 JSON、平台无关 Signals、规则匹配、候选选择、两步 GuidanceSession | 不依赖 Android UI、Context、CameraX 或 MediaStore |
| [ExplainApi/](../ExplainApi) | P0 讲解契约、供应商适配和失败隔离 | 不承载取景器状态机，不决定客户端两步预算 |
| [docs/](.) | 产品、架构、验收、研究、追踪与验证状态 | 不替代代码依赖事实，不把研究资料当通过证据 |

依赖方向：

```text
androidApp  ───────► coach
    │
    └─ P0 用户明确同意后，经固定契约 ───────► ExplainApi ───────► 可替换供应商

coach ─X─► Android / Compose / CameraX / MediaStore
ExplainApi ─X─► 取景器状态机
```

## 3. 核心运行链路

### 3.1 P-1 取景与指导

```text
CameraX Preview + ImageAnalysis + ImageCapture
       │ latest-only；关闭 ImageProxy
       ▼
ML Kit Face/Pose + 水平/亮度/脸占比/遮挡等端侧信号
       ▼
coach：意图硬过滤 → 场景匹配 → 最多三类候选
       ▼
GuidanceSession：稳定门槛 → 一次一个动作 → 最多两个必做动作
       ├─ Compose 动作卡/字幕
       ├─ 当前稳定中文 TTS
       └─ 任意阶段手动快门
       ▼
ImageCapture → MediaStore → 可见成功或可恢复失败
```

回调只将最新帧投入容量为一的事件流；GuidanceSession、100ms tick、UI 与 TTS 回调在主线程 reducer 串行。Android 约每 300ms 提交最新信号，600ms/连续三次稳定后更新动作槽；数值是产品可测参数，真机仍需校准。

### 3.2 已批准 P1 创意与 Live

普通创意处理发生在捕获后，不替换 CameraX 原片管线：原片优先发布，配方私有保存，效果副本仅在明确另存或主动双保存时生成。batchId 关联单拍/连拍请求，独立 captureId 关联单张原片、配方和派生资产，derivativeId 区分另存副本；SaveCoordinator 分阶段、幂等、只重试失败阶段。

Live 仅在显式开启且四用例能力允许时增加无音轨 VideoCapture，强制标准 Photo、SDR JPEG、关闭 Extensions。Motion Photo 打包或发布任何失败都回到未改写的普通 JPEG，最终至多发布一个主文件。

## 4. 质量属性

独立批准的 P1 自然上镜见[美颜架构](architecture/beauty.md)：只在启用时添加 CameraEffect.PREVIEW；原始分析/捕获不加工，Face 数值快照、GPU 表面处理和离线副本不进入 coach 模块。此有限例外由需求第 4.6 节批准。

| 属性 | 架构约束 | 证据边界 |
| --- | --- | --- |
| 实时性 | latest-only 分析；约 300ms 信号输入；600ms/三帧防抖；主线程 reducer 避免旧帧竞态 | JVM 能测状态机；250–300ms/p95 与热量需目标机 |
| 可用性 | 预览优先；除相机不可用/捕获中外快门可用；无无限加载/纯黑屏 | UI/设备链路 NotRun 不可被单测替代 |
| 离线 | P-1 意图、指导、捕获、保存不依赖 ExplainApi | 飞行模式设备证据 NotRun |
| 隐私 | 端侧信号、只申请 CAMERA、不识别身份、不读整本相册 | 上架前安装包与数据安全披露仍需审计 |
| 可靠性 | 标准能力探测、可见降级、MediaStore pending、journal 幂等恢复、原片不回滚 | HyperOS 存储压力/进程恢复 NotRun |
| 确定性 | 场景规则、P1 颜色矩阵、选优排序、captureId/资产/阶段键可单测 | 真实颜色、广色域和推荐有效性 NotRun |
| 可移植边界 | coach 数据/模型平台无关，平台相机壳原生 | 不等于已立项 iOS 或其他 Android |
| 可追踪性 | FR/UX/组件/自动化/设备状态分列 | 路径存在不等于 Delivery 完成 |

## 5. 技术基线

实际版本以 [gradle/libs.versions.toml](../gradle/libs.versions.toml)、Gradle 文件和 [.csproj](../ExplainApi/ExplainApi.csproj) 为准；本次文档重构不升级依赖、不改变构建。

| 项目 | 当前实际值 |
| --- | --- |
| Android | minSdk 26、compileSdk/targetSdk 36、Java/JVM 17 |
| AGP / Kotlin | 8.11.1 / 2.1.20 |
| CameraX | 1.6.1；core、camera2、lifecycle、view、video、extensions、mlkit-vision 同版本 |
| ML Kit | Face 16.1.7；Pose 18.0.0-beta5 |
| Compose | BOM 2026.06.01 |
| Kotlinx | Serialization 1.8.1；Coroutines 1.10.2 |
| ExplainApi | .NET 10 / net10.0 Minimal API |
| ExplainApi.Tests | net10.0；Microsoft.AspNetCore.Mvc.Testing 10.0.0；xUnit 2.9.3 |

工程约束：

- CameraX Extensions 必须同时通过可用性与 ImageAnalysis 共存能力检查；不使用 ExtensionSessionConfig 作为默认链路。
- 原片写入 DCIM/拍照教练，使用 RELATIVE_PATH 与 IS_PENDING，无水印。ImageCapture 明确使用普通 JPEG；Live 不请求 Ultra HDR。
- P-1 只申请 CAMERA；无声 Motion Photo 不启用音频，也不申请麦克风、定位或读整本相册。
- 相机用户设置保存在同一私有 `SharedPreferences` 中：语音、字幕、网格、水平仪、倒计时、画幅、拍摄偏好和模式偏好均用安全枚举恢复；非法旧值回默认。一键重置只覆盖相机设置键，不清除端侧分析同意、不触碰照片。
- P1 创意偏好（当前风格、是否启用三张连拍、保存策略、副本质量与 Live 开关）使用同一私有设置文件的独立键；重置相机设置不删除照片。编辑配方按 captureId 写 App 私有文件，编辑历史仍只保存在当前结果页，不写入原片元数据。
- 音量键映射快门；代拍页屏幕常亮。第一版不上登录、EF、ABP 或手机端大模型。
- 不做无关依赖升级。文档中的“1.6+”历史决策由当前实际 1.6.1 落地，不构成自动升级授权。

## 6. 回退策略

| 失败点 | 必须回退 | 禁止 |
| --- | --- | --- |
| 快捷焦段元数据缺失、重复、失效或绑定失败 | 只保留默认后摄并继续标准拍照/指导 | 根据机型名、传感器 ID 或 2x 猜测 |
| Extensions 不可用或不能与 ImageAnalysis 共存 | 标准 Preview + Analysis + Capture | 为扩展卸载分析、黑屏 |
| 镜头/画幅/模式重绑 | 重建 Zoom/EV/模式能力；旧值夹紧；AE/AF UI 恢复未锁 | 沿用陈旧能力或虚报锁定 |
| 风格预览/解码/变换/编码失败 | 原图预览/原片；可见说明；资源清理 | 覆盖原片、无限扩大内存重试 |
| 分阶段保存部分失败 | 保留已成功项，只重试失败阶段 | 回滚已发布原片、重复 MediaStore 项 |
| Live 预检/HD/SD 绑定/编码/裁剪/XMP/空间/发布失败 | 普通三用例与未改写普通 JPEG；清理临时项 | 假 Live、重复静态原片、混合/未知 XMP 盲改 |
| TTS、网络或 ExplainApi 失败 | 字幕/内置指导/离线主路径继续，按用户设置给可见状态 | 擅自开字幕、后台持续重试、阻断快门 |
| 权限、相机占用或保存错误 | 明确原因、重试/设置入口和可恢复结果 | 纯黑屏、无限加载、静默丢图 |

## 7. 专题与 ADR 索引

| 文档 | 内容 |
| --- | --- |
| [相机与感知专题](architecture/camera-and-perception.md) | 三/四用例、ViewPort、Extensions、3A、亮度、Pose、遮挡与隐私 |
| [口令与讲解 API 专题](architecture/guidance-and-explain.md) | latest-only 事件、reducer、稳定门槛、TTS、恢复口令与 ExplainApi |
| [P1 创意、保存与 Motion Photo](architecture/creative-and-storage.md) | 领域模型、颜色矩阵、连拍、资源上限、SaveCoordinator、MediaStore 与 Motion Photo |
| [自然上镜 v1](architecture/beauty.md) | 独立批准的 PREVIEW-only GPU、端侧关键点快照、按需 CPU 导出和失败/热回退 |
| [架构决策记录](architecture/decisions.md) | 原生方案、共享边界、C# 服务、iOS 方向、明确不选项和历史比较 |
| [产品需求总纲](requirement.md) | Scope、Go/No-Go、跨版本硬约束和隐私 |
| [功能需求定义与追踪矩阵](traceability/requirements-matrix.md) | Requirement → Phase → Acceptance → Component → Evidence → Status |
| [需求决策与冲突记录](traceability/decisions-and-conflicts.md) | 未经用户拍板的产品/架构矛盾 |

## 8. 验证基线与真实状态

本地自动化门禁依次为 `.\gradlew.bat :coach:test` 与 `.\gradlew.bat :androidApp:testDebugUnitTest`；ExplainApi 契约另运行 `dotnet test .\ExplainApi.sln`。命令通过只代表相应 JVM/.NET 覆盖，不替代仪器、真机、P-1 对照实验或 P0/P1 产品验收。

历史自动化结果（2026-08-31）：coach JVM Pass；Android JVM Pass；ExplainApi .NET Pass（3/3），当时 Android instrumented 与目标机证据为 NotRun。2026-09-03 自然上镜实施新增模拟器仪器验证，详见[美颜验证报告](traceability/beauty-validation-2026-09-03.md)；不据此改写历史或目标机 NotRun。完整文档基线见[验证报告](traceability/validation-report.md)。

2026-08-31 基线轮次没有目标机连接；2026-09-03 已有[局部真机证据及失败](traceability/combined-device-validation-2026-09-03.md)。当前完整合同尚无覆盖全部项目的通过证据；历史局部 Pass/Fail 保留，不由本轮文档修订覆盖。快捷焦段/EXIF、真实 Zoom/EV、Extensions 三用例、AE/AF 3A、画幅成片、音量键倒计时、连续 100 张保存、遮挡/污渍误报、十二种风格和七项编辑真实颜色、连拍间隔/热量/推荐有效性、HyperOS MediaStore 分阶段保存/收藏/回收站、广色域/Ultra HDR，以及 Live 四用例绑定、编码、裁剪和 Motion Photo 播放全部为 NotRun。唯一详细状态以[目标机真机矩阵](acceptance/acceptance-plan.md#92-目标机真机矩阵)和[功能需求定义与追踪矩阵](traceability/requirements-matrix.md)为准。

## 9. 需求修订与剩余接口

2026-09-05 按用户同意的方案：CP-03 采用主动打开的独立参数面板；CP-04 由行为覆盖推导口令数量；CP-05 定位非目标设备检查为非阻断健壮性检查。历史双方与决议见[需求决策与冲突记录](traceability/decisions-and-conflicts.md)。

CP-01 已于 2026-09-12 统一为默认字号下约整个屏幕高度的五分之一，源码已核对全屏20%目标，目标机布局验收NotRun。状态、面板、持久恢复及旧日志处理的明确代码缺口已修复并独立复验；未知历史归属继续不猜。感知有效期/阈值、实验配置、真实存储恢复、颜色与热校准仍待验证，详见[总控验收记录](traceability/completion-2026-09-12.md)。

## 10. 2026-09-12 逐项需求审核修订

对应[逐项矩阵](research/requirements-line-audit-2026-09-12.md)与[修订登记](traceability/requirements-optimization-2026-09-12.md)：分开 AF/AE 请求与确认，画质/速度优先名称与 API 对齐；新鲜帧/会话边界、持久暂存与备份排除、Live 缓存不足回退以及系统热撤回边界同步专题。需求审核阶段未改业务代码；后续三轮工程修复已独立复验，持久暂存与旧日志处理已接线，2026-09-13已补真实跨进程同身份恢复（不声称写入瞬间强杀）。CameraX仍固定1.6.1；用户批准的方案C已通过公开VIDEO_CAPTURE effect接入自有编码账本及同JPEG曝光timestamp，合成双MP4正向通过。实际CameraX仍有完成超时及JPEG回退，正在阶段诊断，完整Live正向未验证，不能沿用旧的空编码锚点结论。CP-01已解决，实验公式和感知/媒体/热校准仍待冻结或验证，不从研究示例取默认值。
