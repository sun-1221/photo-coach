# 收尾自检

只检查触碰层。命令通过不能替代范围、产品冲突、隐私、仪器或真机口径；失败即对应层未通过。

## 规范路由

- [ ] 所有仓库实现、修复、重构、测试或只读合规检查已先读 `docs/requirement.md`，再读 `docs/architecture.md`。
- [ ] 已按任务信号加载最小但完整附件集合；没有只读两个入口就声称规范已完整读取。
- [ ] 涉及 FR、Phase、实现状态、组件或证据时已读需求追踪矩阵；涉及 UX、质量或真机时已读验收计划。
- [ ] 用户/页面/取景器交互/指导/文案/场景/短口令、P1、后期、相机感知、口令/Explain、创意存储、技术决定和冲突分别走 SKILL.md 的对应路由。
- [ ] `docs/history/` 未被用作当前规范；`docs/research/` 未被用来扩大范围、证明交付或声称验证通过。
- [ ] 命中的 `ConflictPending` 已保留双方并请求用户决定，没有由实现、测试、旧 reference 或“更严格解释”静默裁决。

以下任一行为都视为路由失败：只读入口遗漏相关附件；把历史快照当当前规范；把 research 当产品批准；遗漏 `ConflictPending`；把 JVM/.NET Pass 写成 instrumented 或小米 14 Pro Pass。

## 范围与状态

- [ ] `Scope`、`Delivery`、`Verification` 已分别判断；路径存在不等于完整交付，自动化通过不等于真机通过。
- [ ] Phase 使用需求追踪矩阵当前值；P0 GateLocked、P1 ApprovedSeparate、Deferred 没有提前或混入 P-1。
- [ ] 新页面、权限、网络、场景、设备或平台没有越过用户授权和当前规范。
- [ ] 只读任务未修改文件；编码任务只修改用户范围内文件并保留已有改动。

## 分层回归

- [ ] CameraX/Compose/权限/保存/TTS 已按 `android-camerax.md` 和当前相机/交互附件检查。
- [ ] Face/Pose/帧/隐私已按 `mlkit-signals.md` 检查；没有沿用旧 FAST 配置、身份推断或错误帧关闭方式。
- [ ] `coach/` 已按 `coach-engine.md` 检查模块边界、意图、动作预算、实时替换和回归样张。
- [ ] ExplainApi 已按 `explain-api.md` 检查 GateLocked 范围、同意、失败隔离和客户端证据缺口。
- [ ] P1 已按 `p1-creative.md` 检查 P-1 隔离、原片、`captureId`、分阶段恢复、资源与 Motion Photo fallback。

## 默认自动化

当前环境是 Windows PowerShell：

- 口令引擎：`.\gradlew.bat :coach:test`
- Android JVM 单测：`.\gradlew.bat :androidApp:testDebugUnitTest`
- 讲解 API：`dotnet test ExplainApi.sln`

在 Unix shell 把 `.\gradlew.bat` 换成 `./gradlew`。不要并行启动会争同一 Gradle 输出或同一 `bin/obj` 的重复构建。只运行触碰层的最小充分集合，并记录未运行层。

## 结果口径

- [ ] 每条命令、测试层和结果单独记录，不用“全部通过”掩盖未运行项。
- [ ] 需求追踪矩阵与验收计划中没有当前证据的 instrumented、设备和产品验收项仍为 `NotRun`/`Unknown`。
- [ ] 没有小米 14 Pro 记录时，焦段/EXIF、Zoom/EV、Extensions、3A、画幅、按键、连续保存、遮挡误报、风格/编辑颜色、连拍热量、HyperOS 分阶段保存/拍后操作、Live 四用例/编码/裁剪/Motion Photo 播放等均未被写成通过。
- [ ] 真机通过项包含地区版本、Android/HyperOS、Build fingerprint 和 App 版本；缺任一身份信息不得泛化覆盖。
- [ ] 完成汇报包含 `Changed`、`Scope`、`Delivery`、`Verification`、`Rules`、`Validated`、`NotRun` 与 `Residual risk`。
