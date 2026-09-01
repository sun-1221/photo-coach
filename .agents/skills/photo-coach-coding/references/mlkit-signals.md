# 感知信号执行检查

改 `androidApp/.../analysis/` 的人脸、姿态、帧统计、镜头检查、坐标或隐私行为时读本文件。信号定义、阈值和文案以当前 docs 为准，本文件不复制第二份产品规则。

## 强制规范集合

- 先读两个入口，再读 `docs/architecture/camera-and-perception.md`。
- 涉及指导触发、恢复口令、场景或文案时读 `docs/requirements/interaction-guidance-and-scenarios.md`；涉及节奏器再读 `docs/architecture/guidance-and-explain.md`。
- 涉及 FR、组件或证据时读 `docs/traceability/requirements-matrix.md`；涉及 UX/真机阈值时读 `docs/acceptance/acceptance-plan.md`。
- 低置信度或多人行为命中 `CP-02` 等未决口径时读冲突登记，不得选一条覆盖另一条。

## 检测器与帧生命周期

- 检测器配置必须来自当前相机与感知专题。当前规范使用精确 Face 模式、最小脸尺寸偏好、睁眼分类与帧间 tracking，不同时开启 contour；业务层忽略微笑概率。不得沿用旧 reference 的 FAST 配置。
- Pose 使用流式单人骨骼路径；Face 与 Pose 各自报告可靠性，不把一个检测器对象或身份字段传入 `coach/`。
- 优先保持单一 latest-only 分析链和统一 ViewPort 坐标。自写异步分析在全部检测结束后关闭 `ImageProxy`，异常路径也关闭；不要关闭 `Media.Image`。
- 同步亮度/方差采样必须有上限，不阻塞 ML Kit；脸部明暗、旋转映射、遮挡、姿势和多人回退严格按当前架构专题实现，不用位置、笑容或单帧 2D 几何发明新含义。

## 隐私与输出边界

- 只输出当前规范定义的有限端侧信号；Unknown 保持 Unknown，不把检测结果写成身份、真实人数承诺、审美分、姿势分或确定的镜头污渍诊断。
- 禁止 embedding、比对、底库、身份字段、年龄/性别/外貌分类，以及帧、人脸框、关键点或身份日志。
- 多人、无脸和 Face/Pose 不可靠的行为必须按当前需求、验收与冲突登记处理；测试不能替用户裁决 `ConflictPending`。

## 验证

- 运行触碰到的 Android JVM 信号/策略测试，并核对需求追踪矩阵中的对应 FR 和设备证据。
- 真实帧旋转、脸框亮度、Pose、多人、遮挡误报、延迟与热量没有当前目标机记录时全部保持 `NotRun`。
