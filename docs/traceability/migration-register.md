# 零丢失迁移登记

- 需求基线：[重构前需求正文](../history/2026-08-31-pre-refactor/requirement.md)
- 架构基线：[重构前架构正文](../history/2026-08-31-pre-refactor/architecture.md)
- 状态词汇：`Retained`、`Moved`、`Consolidated`、`ConflictPending`；没有删除状态
- 粒度：每行的“覆盖单元”包含该旧节内每个段落、列表项、表头、表行、流程图、阈值、例外、指标、验收与状态；整段迁移的附件保留原文

## 增量迁移顺序

A. 盘点并保存版本化历史正文；B. 研究依据；C. 产品规格与分阶段验收；D. 架构入口、专题与 ADR；E. FR/UX/组件/证据追踪与冲突去重；F. 链接、编号、范围、状态、diff 与语义超集验证。每阶段仅做路由和结构变更；未决语义进入 ConflictPending。

## 需求文档映射

| 旧位置 | 覆盖单元 | Status | 新位置 |
| --- | --- | --- | --- |
| 前言、状态、版本术语 | 状态、权威声明、P-1/P0/P1/后期术语表 | Retained | [需求入口](../requirement.md) |
| 1.1 | 一句话产品定义 | Retained | [需求入口](../requirement.md#11-一句话) |
| 1.2 | 问题清单全部 5 条 | Retained | [需求入口](../requirement.md#12-要解决的问题) |
| 1.3 | 核心原则表全部 9 行 | Retained | [需求入口](../requirement.md#13-核心产品原则) |
| 1.4 | 已拍板表全部 11 行 | Retained | [需求入口](../requirement.md#14-已拍板) |
| 2.1 | 目标用户、竞品边界 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#21-首个目标用户) |
| 2.2 | 核心任务 4 步 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#22-核心任务) |
| 2.3 | 角色表与非护城河声明 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#23-角色) |
| 2.4 | 非目标用户 5 条 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#24-非首个目标用户) |
| 3.1 | 待验证假设与市场重叠判断 | Retained | [需求入口](../requirement.md#31-待验证假设) |
| 3.2 | P-1 三条件对照实验全部规则 | Retained | [需求入口](../requirement.md#32-p-1-对照实验) |
| 3.3 | 成功指标表全部 11 项及计算方式 | Retained | [需求入口](../requirement.md#33-成功指标) |
| 3.4 | Go/No-Go 全部 5 条 | Retained | [需求入口](../requirement.md#34-go--no-go) |
| 4.1 | P-1/P0/P1/后期范围矩阵全部能力 | Retained | [需求入口](../requirement.md#41-范围矩阵) |
| 4.2 | P-1 包含与明确不含清单 | Retained | [需求入口](../requirement.md#42-p-1-只包含) |
| 4.3 | P0 前置条件、增量与仍不做 | Retained | [需求入口](../requirement.md#43-完整-p0) |
| 4.4 | P1/后期路由 | Retained | [需求入口](../requirement.md#44-p1-与后期) |
| 5.1 | 页面表、入口规则 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#51-页面) |
| 5.2 | 首次进入流程图与说明 4 条 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#52-首次进入) |
| 5.3 | P-1 主流程图、预算与提前快门规则 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#53-p-1-主流程) |
| 5.4 | 竖横屏布局图、黑带/高度/动作卡/无建议规则 | ConflictPending | [交互规格](../requirements/interaction-guidance-and-scenarios.md#54-取景器布局)；[CP-01](decisions-and-conflicts.md) |
| 6.1 | 取景器组件表全部 19 行与验收 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#61-取景器组件) |
| 6.2 | 状态表、时序、防抖、TTS 与替换规则 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#62-交互状态) |
| 6.3 | P-1/P0 意图表和多人回退 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#63-意图操作) |
| 6.4 | 能力发现、自动参数、用户锁与防抖 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#64-初始参数与用户控制) |
| 6.5 | 中文语音/字幕生命周期与失败规则 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#65-中文语音与字幕) |
| 6.6 | 保存反馈和恢复规则 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#66-保存反馈与恢复) |
| 6.7 | P0 代拍 5 步与非独有边界 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#67-p0-代拍简化界面) |
| 6.8 | 无障碍、48dp、TalkBack、字号与手势规则 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#68-易用性与无障碍) |
| 7 | FR 表全部 31 行、阶段、需求和验收摘要 | Consolidated | [FR 唯一权威矩阵](requirements-matrix.md) |
| 8.1 | 候选流程图、持续运行、优先级与预算 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#81-候选优先级和预算) |
| 8.2 | 触发/完成/失效/重复/冲突阈值表 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#82-触发与防抖) |
| 8.3 | 信号到提示表全部条件、口令和限制 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#83-信号到提示) |
| 8.4 | 文案规则 6 条 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#84-文案规则) |
| 8.5 | P0 再讲细全部隐私、失败与客户端过滤规则 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#85-p0-再讲细) |
| 9.1 | UX 表全部 38 行；从错误混合区分为 P-1 与已批准 P1 | Consolidated | [UX 唯一权威验收](../acceptance/acceptance-plan.md)；[CP-02/03/05](decisions-and-conflicts.md) |
| 9.2 | 目标机规则、设备记录字段与 12 项 NotRun 表 | Moved | [验收真机矩阵](../acceptance/acceptance-plan.md#92-目标机真机矩阵) |
| 9.3 | 本地门禁、质量与实验限制 | Moved | [验收质量门槛](../acceptance/acceptance-plan.md#93-质量门槛) |
| 10.1 | 产品/成片约束全部规则 | Retained | [需求入口](../requirement.md#101-产品与成片约束) |
| 10.2 | 权限、端侧、研究事件、云端同意与法律复核 | Retained | [需求入口](../requirement.md#102-权限与数据) |
| 10.3 | 免费、登录、收费、水印与信任阻断 | Retained | [需求入口](../requirement.md#103-可靠性与信任) |
| 11.1 | 三个 P-1 场景表与静态对照卡表 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#111-p-1-三个场景包) |
| 11.2 | P0 场景表、P1 预写表与风格边界 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#112-p0-与-p1-场景) |
| 11.3 | 回归样张四类与额外要求 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#113-回归样张) |
| 11.4 | 冲突优先级、初始参数表与禁止项 | Moved | [交互规格](../requirements/interaction-guidance-and-scenarios.md#114-场景初始参数) |
| 11.5 | 短口令表、核规则和 12–16 数量口径 | ConflictPending | [交互规格](../requirements/interaction-guidance-and-scenarios.md#115-短口令核)；[CP-04](decisions-and-conflicts.md) |
| 12.1 | P1 候选清单与数据门禁 | Moved | [P1 规格](../requirements/p1-creative.md#121-p1-候选) |
| 12.2 | 参数教练、12 风格表、三张连拍、七项编辑、保存与 Live 全部细则 | ConflictPending | [P1 规格](../requirements/p1-creative.md#122-小米-14-pro-p1-创意层本轮已批准)；[CP-03](decisions-and-conflicts.md) |
| 12.3 | 第一版明确不做全部 10 条 | Moved | [P1 规格](../requirements/p1-creative.md#123-第一版明确不做) |
| 12.4 | 范围/市场/实现决策变更规则 | Moved | [P1 规格](../requirements/p1-creative.md#124-决策变更规则) |
| 13 | 合影后期三阶段与第一版安全回退 | Moved | [后期范围](../requirements/deferred-scope.md#13-合影后期保留第一版不做) |
| 14 | 角色形态表、阶段与不做入口规则 | Moved | [后期范围](../requirements/deferred-scope.md#14-角色切换后期保留第一版不做入口) |
| 15.1 | A/B/C 证据分级表与限制 | Moved | [研究依据](../research/evidence.md#151-证据分级) |
| 15.2 | 竞争格局表全部产品、来源、能力和含义 | Moved | [研究依据](../research/evidence.md#152-当前竞争格局) |
| 15.3 | 市场结论全部 5 条 | Moved | [研究依据](../research/evidence.md#153-结论) |
| 15.4 | 竞争取舍表 | Moved | [研究依据](../research/evidence.md#154-竞争取舍) |
| 15.5 | 官方依据表、链接、推论边界与联网复核结论 | Moved | [研究依据](../research/evidence.md#155-本轮提示需求的官方依据与边界) |
| 15.6 | 最大市场风险与停止条件表 | Moved | [研究依据](../research/evidence.md#156-最大市场风险) |
| 16 | 维护职责、同步检查与下一步 | Consolidated | [需求入口维护规则](../requirement.md#13-文档维护) |

## 架构文档映射

| 旧位置 | 覆盖单元 | Status | 新位置 |
| --- | --- | --- | --- |
| 前言/状态 | 权威边界与产品引用 | Retained | [架构入口](../architecture.md) |
| 1 | 原生 Android、小米 14 Pro、平台无关口令与 C# 服务结论 | Retained | [架构入口](../architecture.md#1-结论与系统上下文) |
| 2 | 不跨平台画相机的延迟、每帧和桥接依据 | Consolidated | [ADR 原始依据](../architecture/decisions.md#原始决策依据) |
| 3 | A/B/C 方案对比表与采用结论 | Consolidated | [ADR 原始依据](../architecture/decisions.md#原始决策依据) |
| 4（模块树） | androidApp/coach/ExplainApi 归属、场景/参数/口令职责 | Consolidated | [模块边界](../architecture.md#2-真实模块边界与依赖方向) |
| 4（目标设备） | 目标机记录、其他机型边界、焦段发现与标准 API 限制 | Consolidated | [系统上下文/回退](../architecture.md) |
| 4.1 | 三/四用例、ViewPort、画幅、Extensions、3A、JPEG、倒计时全部规则 | Moved | [相机与感知](../architecture/camera-and-perception.md#41-相机管线) |
| 4.2 | Face/Pose、亮度采样、多人、隐私、遮挡全部规则 | Moved | [相机与感知](../architecture/camera-and-perception.md#42-感知) |
| 4.3 | 输入输出、TTS、事件流/reducer、300/600ms、恢复口令与 JSON 回归 | ConflictPending | [口令专题](../architecture/guidance-and-explain.md#43-口令引擎)；[CP-04](decisions-and-conflicts.md) |
| 4.4 | ExplainApi 同意、压缩帧、固定 JSON、客户端过滤与失败隔离 | Moved | [口令专题](../architecture/guidance-and-explain.md#44-讲解-api) |
| 4.5（领域模型） | CreativeStyle/EditHistory/Burst/Scorer/ParameterCoach/CaptureIdentity/Save/Motion 模型 | ConflictPending | [创意专题](../architecture/creative-and-storage.md#45-小米-14-pro-p1-创意层)；[CP-03](decisions-and-conflicts.md) |
| 4.5（图像/保存） | 8 步图像与保存管线、资源上限、EXIF、journal、回退 | Moved | [创意专题](../architecture/creative-and-storage.md#图像与保存管线) |
| 4.5（连拍/预览） | 顺序三张、失败暂停、确定排序、RenderEffect 降级 | Moved | [创意专题](../architecture/creative-and-storage.md) |
| 4.5（Motion Photo） | 6 步 Live、XMP、SDR、单主文件、恢复清理与 NotRun | Moved | [创意专题](../architecture/creative-and-storage.md#motion-photo-管线) |
| 5 | iOS 留下/新写表与后期边界 | Consolidated | [ADR 与历史依据](../architecture/decisions.md) |
| 6 | minSdk、CameraX、工程设置、依赖、不升级与完整 NotRun 列表 | Consolidated | [技术基线](../architecture.md#5-技术基线)、[验证基线](../architecture.md#8-验证基线与真实状态)、[验收](../acceptance/acceptance-plan.md) |
| 7 | MAUI/Flutter、ISP/RAW、CameraEffect/LUT/私有 API、KMP、纯 C# 不选项 | Consolidated | [ADR-006 与原始不选项](../architecture/decisions.md) |

## 冲突覆盖层

| ID | 旧内容来源 | Status | 保留位置 |
| --- | --- | --- | --- |
| CP-01 | requirement 5.4 与 UX-15 | ConflictPending | [冲突登记](decisions-and-conflicts.md) |
| CP-02 | UX-06 与 UX-31，以及状态规则 | Consolidated | [冲突登记的 2026-09-01 决议](decisions-and-conflicts.md) |
| CP-03 | requirement 1.3/5.4 的一次一件事与 12.2/UX-33 的三张参数卡 | ConflictPending | [冲突登记](decisions-and-conflicts.md) |
| CP-04 | requirement 11.5 的 12–16 与 architecture 4 的 8–12 | ConflictPending | [冲突登记](decisions-and-conflicts.md) |
| CP-05 | 仅小米 14 Pro 承诺与 UX-30 非目标设备可用措辞 | ConflictPending | [冲突登记](decisions-and-conflicts.md) |

## 零丢失判定

1. 任何 Retained 项仍在两个最高权威入口之一。
2. 任何 Moved 项在当前规范附件保留完整原文，而非摘要。
3. Consolidated 只合并完全重复信息或将表格扩展为追踪/ADR 结构；历史快照可逐字复核。
4. ConflictPending 同时保留冲突双方，不产生默认裁决。
5. FR 唯一定义只在追踪矩阵，UX 唯一定义只在分阶段验收；历史快照明确排除权威计数。
6. NotRun/Unknown 不因路径存在、文档迁移或自动化通过而改变。
