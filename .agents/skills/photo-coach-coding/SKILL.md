---
name: photo-coach-coding
description: "在 photo-coach / 拍照教练仓库内实现、修复、重构、测试或只读检查 Android CameraX 取景器、ML Kit 感知、Kotlin 口令引擎、P1 创意层或 ASP.NET Core ExplainApi 时使用。也适用于用户只说‘按需求实现’‘取景器黑屏’‘补口令回归’等仓库内任务。不用于纯产品文案、通用技术解释、Skill 包创建/审核、兄弟仓库、跨平台取景器选型或第一版 iOS 实施。"
---

# 拍照教练编码

在不越过产品版本门禁的前提下，把仓库需求落实为可验证的 Android、Kotlin 与 C# 代码。通用语言或框架教程不是本 Skill 的内容。

## 先判定范围

1. 从用户验收、目标文件和 `docs/requirement.md` 判断任务属于 P-1、完整 P0、已单独批准的 P1 创意层，还是后期能力。新增功能、页面、权限、网络、场景、设备或只读合规检查时，先读 [references/version-scope.md](references/version-scope.md)。
2. 用户没有指定版本时，以需求范围矩阵和已批准状态为准；不要把 P0/P1/后期能力静默塞进 P-1。若请求与前置门槛冲突且会实质改变产品，说明冲突并请求决策。
3. 以 `gradle/libs.versions.toml`、Gradle 文件和 `.csproj` 为实际依赖基线。修复或实现任务不得顺手升级依赖、迁移存储或替换相机 UI；版本升级必须是任务本身的一部分。

## 权威顺序

1. `docs/requirement.md`：产品行为、版本范围、验收和隐私。
2. `docs/architecture.md`：技术选型、模块边界和回退策略。
3. [references/sources.md](references/sources.md) 中的官方文档：API 语义。
4. 本 Skill 的按层 references：仓库落地约束。
5. 现有代码：当前实现事实；与前四项冲突时只能视为待确认偏差，不能反过来改写需求。

## 按层读取

- `androidApp/` 相机、Compose、权限、设置、保存、TTS、`captureId` 或 Motion Photo：读 [references/android-camerax.md](references/android-camerax.md)。
- `androidApp/.../analysis/` 的 Face、Pose、帧统计和隐私：再读 [references/mlkit-signals.md](references/mlkit-signals.md)。
- `coach/`、`scenes.json`、两步会话和口令测试：读 [references/coach-engine.md](references/coach-engine.md)。
- `androidApp/.../creative/`、三张连拍、风格、编辑、选优、分阶段保存或无声 Live：读 [references/p1-creative.md](references/p1-creative.md)。
- `ExplainApi/` 或客户端“再讲细”：读 [references/explain-api.md](references/explain-api.md)。
- 完成前只读取触碰层对应的 [references/self-check.md](references/self-check.md) 项。
- 只有在核对 API、升级依赖或更新本 Skill 研究依据时才读 [references/sources.md](references/sources.md)。

## 工作方式

1. 检查目标文件、相关测试和用户已有改动；只读任务不改文件。
2. 读取需求与架构中和任务直接相关的章节，再加载上面的最小 reference 集合。
3. 编码请求在范围清楚时直接实现；只在产品冲突、缺少实质选择或需要扩大授权时停下询问。
4. 保持文件归属：Android 相机/UI/端侧感知在 `androidApp/`；平台无关的场景、信号模型、两步会话和口令选择在 `coach/`；可选云端讲解契约与供应商适配在 `ExplainApi/`。
5. 运行触碰层的最小充分测试。命令通过只证明自动化覆盖；没有小米 14 Pro 证据的项目必须明确写 `NotRun`，不能推断为真机通过。

## 跨层硬门禁

- P-1 主路径是小米 14 Pro 后摄单人、两个意图、三个场景包、一个 shooter 必做动作加一个 subject 必做动作；候选最多三条不等于同屏或必做三步。“再优化一下”最多一条且不算必做步骤。
- 快门只因相机不可用或正在捕获而暂时禁用；指导、分数、网络和未达阈值不得锁快门。P-1 意图、指导、捕获、保存必须飞行模式可用。
- CameraX 默认同时绑定 `Preview + ImageAnalysis + ImageCapture`；分析用 `STRATEGY_KEEP_ONLY_LATEST`，并且只关闭 `ImageProxy`。唯一例外是用户显式开启且已批准的 P1 无声 Live：能力允许时绑定 `Preview + ImageAnalysis + ImageCapture + VideoCapture`，强制普通 Photo、关闭 Extensions、不启用音频；四用例或后续阶段失败时可见回退普通 JPEG。Extensions 不能保住 `ImageAnalysis` 时回退标准 Photo，不能牺牲实时指导。
- 快捷焦段来自当前后摄能力与小米 14 Pro 真机标定；不得硬编码 2x、把数码裁切叫光学镜头、读取厂商传感器 ID，或宣称未验证焦段已通过。
- Face/Pose 只产生端侧构图与口令信号；禁止身份识别、embedding、人脸库、外貌评分和姿势分。两张脸以上不得进入单人姿势匹配。
- P-1 只申请 `CAMERA`。不新增登录、麦克风、定位、读取整本相册、后台上传或持续重试。
- P1 创意层不得改变 P-1 对照实验管线和 Go 指标。每次捕获用不可复用的 `captureId` 关联原片、配方、副本与连拍序号；默认发布原片并私有保存配方，效果 JPEG 只在明确另存或用户主动开启自动双保存时生成。保存按阶段幂等恢复、只重试失败阶段；无声 Motion Photo 是单一主文件，组装失败必须回退普通 JPEG，不能重复发布原片。

## 完成汇报

给出：

- `Changed`：修改文件与目的；只读检查写 `none`。
- `Scope`：P-1 / P0 / P1，以及明确未扩大的范围。
- `Rules`：实际读取的需求章节和 references。
- `Validated`：运行命令与结果。
- `Residual risk`：真机、HyperOS、Extensions、颜色、热量或其他 `NotRun` 项；不要把 Unknown/NotRun 写成通过。

只在修改 description 或评估自动触发时读取 [references/trigger-eval.md](references/trigger-eval.md)。
