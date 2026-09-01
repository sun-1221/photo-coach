---
name: photo-coach-coding
description: "在 photo-coach / 拍照教练仓库内实现、修复、重构、测试或只读检查 Android CameraX 取景器、ML Kit 感知、Kotlin 口令引擎、P1 创意层或 ASP.NET Core ExplainApi 时使用。也适用于用户只说‘按需求实现’‘取景器黑屏’‘补口令回归’等仓库内任务。不用于纯产品文案、通用技术解释、Skill 包创建/审核、兄弟仓库、跨平台取景器选型或第一版 iOS 实施。"
---

# 拍照教练编码

在不越过产品版本门禁的前提下，把当前仓库规范落实为可验证的 Android、Kotlin 与 C# 代码。本文只规定如何找到并执行权威规范，不是 FR、UX、产品范围或交付状态的第二份权威。

## 先判定范围

1. 所有仓库实现、修复、重构、测试和只读合规检查，先完整读取 `docs/requirement.md`，再完整读取 `docs/architecture.md`。只读入口文件不构成完整规范读取。
2. 根据任务触及的行为、组件和证据，加载“按任务路由”中的最小但完整附件集合；任务跨层时取各行并集，不要求每次加载所有文档。
3. 涉及具体 FR、Phase、实现状态、组件或证据时读取 `docs/traceability/requirements-matrix.md`，分别判断 `Scope`、`Delivery`、`Verification`。路径存在、自动化通过和真机通过是三个不同事实。
4. 新增能力、页面、权限、网络、场景、设备、平台或范围合规检查时，再读 [references/version-scope.md](references/version-scope.md)。P0 的 `GateLocked`、P1 的 `ApprovedSeparate` 和后期 `Deferred` 不得互相提前。
5. 若任务命中 `ConflictPending`，读取 `docs/traceability/decisions-and-conflicts.md`，保留冲突双方并请求用户拍板；实现、测试和本 Skill 都不能静默选择一方。
6. 以 `gradle/libs.versions.toml`、Gradle 文件和 `.csproj` 为实际依赖基线。非升级任务不得顺手升级依赖、迁移存储或替换相机 UI。

## 权威结构

1. `docs/requirement.md` 与 `docs/architecture.md` 是最高权威入口。完整当前规范由入口及入口列出的、与任务相关的规范附件共同组成。
2. `docs/requirements/` 承载当前产品细则；`docs/acceptance/acceptance-plan.md` 是当前 UX 与真机/质量门槛来源；`docs/traceability/requirements-matrix.md` 是当前 FR、Phase、组件与证据状态来源。
3. `docs/architecture/` 承载当前架构专题；`docs/architecture/decisions.md` 承载当前技术选择和历史技术决定。专题不能扩大产品范围。
4. `docs/traceability/decisions-and-conflicts.md` 控制未决冲突。在用户拍板前，任何局部文本、代码或测试都不能覆盖 `ConflictPending`。
5. [references/sources.md](references/sources.md) 中的官方资料只用于 API 语义；本 Skill 的其他 references 只提供仓库执行方法和回归检查，不拥有产品事实。
6. 现有代码只说明当前实现事实。代码路径、测试名、旧注释或历史实现与当前规范冲突时，记录偏差，不反向改写规范。
7. `docs/research/` 只提供研究依据，不能扩大 `Scope`、证明 `Delivery` 或把任何验证状态改成 Pass。`docs/history/` 默认禁止作为当前产品、架构、验收或状态来源。

## 按任务路由

| 任务信号 | 入口之后必须读取 |
| --- | --- |
| 具体 FR、Phase、实现状态、组件或证据 | `docs/traceability/requirements-matrix.md` |
| UX、验收、真机矩阵或质量门槛 | `docs/acceptance/acceptance-plan.md` |
| 用户、页面、取景器交互、指导引擎、文案、场景或短口令 | `docs/requirements/interaction-guidance-and-scenarios.md` |
| P1 参数建议、风格、连拍、编辑、分阶段保存或 Motion Photo | `docs/requirements/p1-creative.md` + `docs/architecture/creative-and-storage.md`；涉及相机会话/感知时再加 `docs/architecture/camera-and-perception.md` |
| 合影、被拍者主用户、角色切换或其他后期能力 | `docs/requirements/deferred-scope.md` |
| CameraX、取景器、相机能力、ML Kit、帧生命周期或端侧感知 | `docs/architecture/camera-and-perception.md` |
| `coach/`、口令节奏、TTS 或 `ExplainApi/` | `docs/architecture/guidance-and-explain.md` |
| MediaStore、`captureId`、P1 保存恢复或 Motion Photo | `docs/architecture/creative-and-storage.md` |
| 技术选型、平台边界、ADR 或历史技术决定 | `docs/architecture/decisions.md` |
| 任一未决口径或 `CP-*` | `docs/traceability/decisions-and-conflicts.md` |

再按代码层读取执行 reference：

- `androidApp/` 的 CameraX、Compose、权限、设置、保存、TTS、`captureId` 或 Motion Photo：读 [references/android-camerax.md](references/android-camerax.md)。
- `androidApp/.../analysis/` 的 Face、Pose、帧统计和隐私：再读 [references/mlkit-signals.md](references/mlkit-signals.md)。
- `coach/`、`scenes.json`、两步会话和口令测试：读 [references/coach-engine.md](references/coach-engine.md)。
- `androidApp/.../creative/`、参数建议、连拍、风格、编辑、选优、分阶段保存或无声 Live：读 [references/p1-creative.md](references/p1-creative.md)。
- `ExplainApi/` 或客户端“再讲细”：读 [references/explain-api.md](references/explain-api.md)。
- 完成前只检查触碰层对应的 [references/self-check.md](references/self-check.md) 项。
- 只有核对 API 语义、升级依赖或维护官方来源时才读 [references/sources.md](references/sources.md)。

## 工作方式

1. 检查目标文件、相关测试和用户已有改动；只读任务不得改文件。
2. 先读两个入口，再按任务信号加载最小完整附件集合和对应执行 reference；在 `Rules` 中记录实际读取集合。
3. 编码前写清 `Scope`、已知 `Delivery` 与所需 `Verification`。代码存在不能推断交付完整，自动化通过不能推断仪器或真机通过。
4. 范围清楚时直接实现；只有 `ConflictPending`、缺少实质产品选择或需要扩大授权时停下请求决策。
5. 保持模块边界：Android 相机/UI/端侧感知在 `androidApp/`；平台无关信号、场景、候选和两步会话在 `coach/`；可选讲解契约与供应商适配在 `ExplainApi/`。
6. 运行触碰层的最小充分测试。没有当前小米 14 Pro 证据的项目必须明确为 `NotRun`，不得由 JVM/.NET Pass 推断为真机 Pass。

## 不得弱化的执行门禁

- CameraX 普通主路径保留 `Preview + ImageAnalysis + ImageCapture`；只有当前规范明确允许、用户显式开启且能力支持的无声 Motion Photo 才增加无音轨 `VideoCapture`。`ImageAnalysis` 保持 latest-only，正确关闭 `ImageProxy`；Extensions 或 Live 失败必须保留实时指导并回退普通 JPEG。
- 快门不得被指导、分数、网络或阈值锁住；端侧主路径、权限最小化、无身份识别/人脸库/外貌评分和多人安全回退不得退化。
- 口令引擎仍保持用户意图硬过滤、一次一个主要动作、最多两个必做动作、可选附加建议与随时快门；具体文案、数量和冲突以当前需求附件与冲突登记为准。
- P1 创意层仍与 P-1 证据隔离；原片优先、不可复用 `captureId`、非破坏副本、分阶段幂等恢复和 Motion Photo 单主文件回退不得退化。
- ExplainApi 仍是 P0 可选、按需、单独同意且失败隔离的补充路径；服务代码存在不等于 P0 已获准或客户端链路已交付。
- 上述是回归门禁摘要，不替代对应 docs 原文。摘要与当前规范不一致时以入口及相关附件为准，并把 Skill 漂移作为待修复问题。

## 完成汇报

给出：

- `Changed`：修改文件与目的；只读检查写 `none`。
- `Scope`：`InScope` / `GateLocked` / `ApprovedSeparate` / `Deferred`，以及明确未扩大的范围。
- `Delivery`：只报告实际检查到的实现事实与未知项，不把路径存在写成完整交付。
- `Verification`：分列 JVM/.NET、instrumented、目标机和产品验收；不能互相替代。
- `Rules`：实际读取的两个入口、规范附件和执行 references。
- `Validated`：实际运行的命令、用例和结果。
- `NotRun`：未运行、无设备或无法证明的检查。
- `Residual risk`：`ConflictPending`、目标机、HyperOS、Extensions、颜色、热量、存储或其他剩余边界。

只在修改 description 或评估自动触发时读取 [references/trigger-eval.md](references/trigger-eval.md)；验证任务路由与状态口径时读取 [references/behavior-eval.md](references/behavior-eval.md)。
