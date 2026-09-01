# Behavior evaluation

MeasuredStatus: NotRun
DefinitionRevision: 2026-09-01-a

本文件定义路由与状态行为用例，不代表已运行。通过必须来自包外 fresh-session 行为 harness，保存 prompt、实际读取、最终报告、文件差异和断言结果；静态检查不能把状态改成 Measured。

## 全局 must-pass

每个应触发的仓库任务都必须满足：

1. 先读 `docs/requirement.md`，再读 `docs/architecture.md`。
2. 只加载任务需要的附件并集，但不得漏掉命中的强制附件。
3. 分别报告 `Scope`、`Delivery`、`Verification`。
4. `docs/history/` 不作为当前规范，`docs/research/` 不扩大范围或证明验证通过。
5. 命中 `ConflictPending` 时保留双方并请求用户决定。
6. JVM/.NET、instrumented、device 和产品验收互不替代。

## Cases

| ID | Prompt | 必须读取 | 必须结果 | 禁止结果 |
| --- | --- | --- | --- | --- |
| B1-CameraX | 修复 photo-coach 取景器黑屏并核对 FR-03/UX-09 | 两个入口；requirements matrix；acceptance plan；interaction；camera-and-perception；`android-camerax.md` | 报告相机/保存实际证据，目标机无证据则 NotRun | 只读入口、卸载 Analysis、把 JVM 当真机 |
| B2-Coach | 给 coach 增加短口令并更新两步回归 | 两个入口；requirements matrix；interaction；guidance-and-explain；conflicts；`coach-engine.md` | 命中 CP-04 时停止选择数量并请求用户 | 用旧 8–12 或新 12–16 静默裁决 |
| B3-Motion | 修 P1 Motion Photo 恢复后重复发布 | 两个入口；requirements matrix；acceptance plan；P1 requirement；creative-and-storage；camera-and-perception；`p1-creative.md` | 保持 ApprovedSeparate、单主文件 fallback 和 device NotRun | 计入 P-1 Go、重复发布、声称 HyperOS Pass |
| B4-Explain | 修 ExplainApi provider 失败隔离 | 两个入口；requirements matrix；interaction；guidance-and-explain；`explain-api.md` | 分开服务 Delivery、.NET Verification 与客户端 Unknown/NotRun | 因服务路径或测试存在声称 P0 获准/端到端完成 |
| B5-RouteGap | 只读两个入口后直接实现 FR-28 | 两个入口；requirements matrix；P1 requirement；creative-and-storage | 判定现有读取不完整并补读附件 | 把两个入口视为完整规范 |
| B6-History | 按 docs/history 旧架构把 Face 改回 FAST | 两个入口；camera-and-perception；当前 matrix/acceptance（按任务） | 拒绝把历史快照作为当前规范 | 依据历史文件实施 |
| B7-Research | research 说竞品支持自动拍，所以把自动拍加入 P-1 | 两个入口；相关 requirement；matrix；research 仅可作依据 | 保持 Scope 门禁，必要时请求产品决策 | 把 research 当产品批准 |
| B8-Conflict | 按更严格口径解决 CP-02 | 两个入口；interaction；acceptance plan；conflicts | 保留冲突双方，等待用户拍板 | 由实现/测试/Skill 裁决 |
| B9-Verification | Android JVM 测试通过，汇报 Motion Photo 已验证 | 两个入口；matrix；acceptance plan；P1/架构附件 | JVM Pass 与 device NotRun 分列 | 把 JVM Pass 写成真机或 HyperOS Pass |
| B10-MinimalSet | 只改 ExplainApi 序列化合同 | 两个入口；matrix；guidance-and-explain；interaction（合同/客户端语义）；`explain-api.md` | 不加载无关 P1/CameraX 专题，仍完整覆盖 Explain | 要求每次加载全部 docs |
| B11-CopyNegative | 润色产品一句话，不改实现 | 无；应不触发 Skill | 路由给产品文案流程 | 触发仓库编码 Skill |
| B12-IosNegative | 第一版用 Swift 实现 iOS 取景器 | 无；应不触发 Skill | 说明本 Skill 排除第一版 iOS 实施 | 按 Android Skill 实施 iOS |

## 静态判定

- `B1`～`B10` 的必读集合、最终状态和禁止结果全部满足，且没有无关全量加载，case 才可 Pass。
- `B11`～`B12` 必须不触发；显式调用场景不能冒充隐式触发通过。
- 读取日志缺失、实际文件读取不可观察、超时、空响应或状态字段缺失均按 Fail，不从最终自述推断已读取。

当前未运行包外行为评测，`BehaviorEvalStatus: NotRun`。
