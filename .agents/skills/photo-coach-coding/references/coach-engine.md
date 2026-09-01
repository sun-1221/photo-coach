# 口令引擎执行检查

改 `coach/`、`scenes.json`、候选选择、两步状态机或口令回归时读本文件。场景、短口令、阈值和 UX 以当前规范附件为准。

## 强制规范集合

- 先读 `docs/requirement.md` 和 `docs/architecture.md`。
- 用户、场景、提示、两步流程、触发/防抖、文案或短口令：读 `docs/requirements/interaction-guidance-and-scenarios.md`。
- 线程模型、节奏、恢复口令或 TTS 消费：读 `docs/architecture/guidance-and-explain.md`。
- 对应 FR、Phase、组件与证据：读 `docs/traceability/requirements-matrix.md`；UX/质量门槛读验收计划。
- 数量、低置信度、页面呈现或其他未决口径命中 `CP-*` 时读冲突登记。尤其不能静默解决短口令数量 `CP-04`。

## 模块与行为边界

- `coach/` 是无 Android UI 的 Kotlin/JVM 模块。它可以定义可序列化信号、场景、候选和指导会话，但不能依赖 Activity、Compose、CameraX、Bitmap 或 `Context`。
- 用户意图是硬过滤条件；场景/Phase 开关、候选上限、听众、两步预算、可选建议、Ready、跳过、随时快门和多人安全回退均以当前需求附件为准，不从旧测试数据扩大范围。
- 候选持续消费最新信号并经过当前规范的稳定/替换门槛；不得积压旧帧、保留失效口令、增加必做步骤或无限等待高置信度。
- 口令只能来自当前启用场景和当前权威短口令集合；禁止术语、外貌/姿势评分、身份推断和未批准模板库。

## 场景与回归

- 修改 `scenes.json`、阈值、优先级、去抖或会话状态后，覆盖当前规范要求的场景正反例、必现/禁现提示、意图锁、两步预算、跳过、随时快门、可选建议、多人/无脸/姿态缺失、恢复口令、相反方向和 TTS 节流。
- 优先扩展行为测试与 fixture，不写只匹配标题、固定 JSON 排版或实现私有方法的脆弱测试。
- 运行 `.\gradlew.bat :coach:test`。JVM Pass 只证明对应自动化覆盖；真机节奏、语音、样张和用户验收仍按矩阵与验收计划报告 `NotRun`。
