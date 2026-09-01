# 版本范围、状态与文件边界

新增能力、页面、权限、网络、场景、目标设备、平台，或做范围合规检查时读本文件。本文件提供判断流程，不复制版本范围、FR 或 UX 定义。

## 判断流程

1. 先读 `docs/requirement.md`，再读 `docs/architecture.md`。
2. 读 `docs/traceability/requirements-matrix.md`，以对应 FR 的 `Phase` 与 `Scope` 判断 `InScope`、`GateLocked`、`ApprovedSeparate` 或 `Deferred`；不要从代码路径或测试名推断范围。
3. 按任务读取产品细则：交互/指导/场景读 `docs/requirements/interaction-guidance-and-scenarios.md`，P1 创意读 `docs/requirements/p1-creative.md`，合影/角色切换/后期读 `docs/requirements/deferred-scope.md`。
4. 读取相应架构专题；涉及平台取舍、技术选择或历史技术决定时读 `docs/architecture/decisions.md`。
5. 命中 `CP-*`、两个规范给出不同口径或无法唯一解释时，读 `docs/traceability/decisions-and-conflicts.md`。保持 `ConflictPending` 并请求用户决定，不能用实现、测试或“更严格”解释代替拍板。

用户直接要求一个仍受门禁或后期约束的能力时，报告对应 `Scope` 和缺失前提；只有用户做出明确范围决策后才扩大实现。

## 三个独立维度

| 维度 | 只回答 | 不得推断 |
| --- | --- | --- |
| `Scope` | 当前规范是否允许在本阶段实施 | 代码存在不等于获准 |
| `Delivery` | 仓库中实际存在、已检查到什么，哪些仍 Unknown | 路径或占位不等于完整交付 |
| `Verification` | 哪类自动化、仪器、目标机或产品验收有当前证据 | JVM/.NET Pass 不等于仪器、真机或产品 Pass |

同一任务必须分别给出三个维度。`ApprovedSeparate` 能力不得计入 P-1 Go 证据；`GateLocked` 能力的服务或规则占位不得写成已批准交付；`Deferred` 不得渲染第一版入口。

## 文件归属与权威角色

| 内容 | 位置与角色 |
| --- | --- |
| 产品总纲、范围、Go/No-Go、跨版本约束与隐私 | `docs/requirement.md`，最高产品入口 |
| 用户、页面、取景器交互、指导、文案、场景与短口令 | `docs/requirements/interaction-guidance-and-scenarios.md`，当前产品附件 |
| P1 创意与后期范围 | `docs/requirements/p1-creative.md`、`docs/requirements/deferred-scope.md`，当前产品附件 |
| UX、真机矩阵与质量门槛 | `docs/acceptance/acceptance-plan.md`，当前验收附件 |
| FR、Phase、组件、证据、Scope/Delivery/Verification | `docs/traceability/requirements-matrix.md`，当前追踪附件 |
| 未决冲突 | `docs/traceability/decisions-and-conflicts.md`，当前冲突控制附件 |
| 系统边界、依赖方向、质量属性、技术基线与回退 | `docs/architecture.md`，最高架构入口 |
| 相机/感知、口令/Explain、创意/存储与 ADR | `docs/architecture/*.md`，当前架构专题 |
| 研究依据 | `docs/research/`，只提供依据，不决定范围、交付或验证 |
| 历史快照 | `docs/history/`，只供追溯，禁止作为当前规范 |
| CameraX、Compose、权限、TTS、MediaStore、研究事件与 Android 图像处理 | `androidApp/` |
| 平台无关信号、场景 JSON、候选与两步会话 | `coach/` |
| “再讲细”契约、供应商适配与失败隔离 | `ExplainApi/` |

`coach/` 禁止依赖 Android UI 或 `Context`；ExplainApi 禁止承载取景器状态机；P1 创意层不得把 Bitmap 或 Android 类型放进平台无关模型。技术归属不改变产品 Phase。

## 验收口径

需要 UX、质量或目标机结论时必须读 `docs/acceptance/acceptance-plan.md`；需要某个 FR 的当前证据时必须同时读需求追踪矩阵。没有带设备与构建身份的当前小米 14 Pro 记录时，对应设备项目保持 `NotRun`。自动化结果只按实际命令和覆盖层报告。
