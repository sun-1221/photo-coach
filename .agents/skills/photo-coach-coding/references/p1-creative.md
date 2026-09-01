# P1 创意层执行检查

改参数建议、风格、三张连拍、选优、轻编辑、`captureId`、分阶段保存、兼容副本、拍后操作或无声 Motion Photo 时读本文件。它只提供代码映射和回归门禁，不复制 P1 产品定义。

## 强制规范集合

- 先读 `docs/requirement.md` 和 `docs/architecture.md`。
- 总是读 `docs/requirements/p1-creative.md` 与 `docs/architecture/creative-and-storage.md`。
- 涉及相机会话、四用例、Extensions、感知或 ViewPort 时再读 `docs/architecture/camera-and-perception.md`。
- 涉及页面、建议卡、编辑/保存反馈或文案时读 `docs/requirements/interaction-guidance-and-scenarios.md`。
- 读需求追踪矩阵确认相关 FR 的 `ApprovedSeparate`、组件和证据；读验收计划确认 P1 UX 与目标机矩阵。
- 参数卡、非目标设备、低置信度或其他 `CP-*` 口径必须读取冲突登记，不能由实现或测试裁决。

## 代码映射

- 领域模型与纯逻辑：风格矩阵、编辑历史、连拍状态、选优、参数建议、捕获身份、保存阶段和 Motion Photo 字节组装保持可单测、确定且不持有无关 Android 状态。
- Android 管线：捕获、受限解码、MediaStore、journal、临时文件、预览效果、四用例录制和 fallback 按创意/存储专题实现，不占用实时分析执行器。
- 跨层边界：P1 不改变 P-1 相机对照管线、Go 指标、权限或主路径；`coach/` 不接收 Bitmap/Context，P1 结果不倒灌为 P-1 证据。

## 不得回归

- 原片优先且不可覆盖/删除；效果只按当前规范的明确用户动作生成独立兼容副本。
- 每次捕获使用不可复用 `captureId`；保存分阶段、幂等、只重试失败阶段，已成功项不回滚，恢复不重复发布。
- 连拍只由明确开启触发并防重入；失败保留已成功照片，推荐不等于删除或美学评分。
- 解码、处理、编码、EXIF、空间和临时资源遵守当前专题上限与清理规则；OOM 与部分失败必须可见、可恢复，不能扩大内存重试。
- Motion Photo 使用当前规范定义的无声四用例、单一主文件、XMP 安全边界和普通 JPEG fallback；不申请音频、不显示假 Live、不重复发布。

## 验证

- 运行触碰到的 `androidApp/src/test/.../creative/*Test.kt`、相机/保存/Motion JVM 测试；只有任务需要且环境具备时运行 instrumented 测试。
- JVM 不能证明真实颜色、广色域、连拍间隔/热量、HyperOS MediaStore、收藏/回收站、四用例绑定、编码、裁剪或系统相册播放。逐项以验收计划与矩阵为准；没有当前小米 14 Pro 证据即 `NotRun`。
