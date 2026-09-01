# 市场、交互与官方依据

- 状态：当前规范附件
- 权威入口：[../requirement.md](../requirement.md)
- 原始位置：重构前 `docs/requirement.md` 第 15 节
- 迁移方式：Moved；原有证据、结论、边界、日期和链接完整保留

本附件承载研究依据，不改变产品范围，也不把外部资料当作真机或用户实验通过证据。

---

## 15. 市场与交互依据（复核至 2026-08-25）

### 15.1 证据分级

| 等级 | 含义 | 可以推出什么 | 不能推出什么 |
| --- | --- | --- | --- |
| A | 系统 / OEM 已发布能力，或有明显规模的成熟商店产品 | 用户已有可比较的功能与相机可靠性基线 | 该能力一定高频，或本产品一定无法胜出 |
| B | 应用商店可下载，但公开评分、下载量或评论样本仍小 | 同类功能已经在售，不能再宣称“市场没有” | 已验证产品市场匹配、留存或宣传效果 |
| C | 官网、Web 产品、落地页或 Beta 招募 | 产品定位与交互方案已出现 | 功能稳定、真实规模、付费意愿或拍照效果 |

商店数字会受地区、平台和抓取时间影响，只作为当日快照；官网自述只证明厂商公开声称提供该能力，不视为独立质量评测。

### 15.2 当前竞争格局

| 证据 | 产品与来源 | 已核对的公开能力 | 对本产品的含义 |
| --- | --- | --- | --- |
| A | [Pixel Camera Coach](https://support.google.com/pixelcamera/answer/17367411?hl=en) | 分析场景，让用户选择想拍的画面，再按屏幕步骤完成；需要网络、仅后摄和受支持模式 | “确认意图 → 分步指导”已进入系统相机 |
| A | [HONOR 灵感帮拍](https://www.honor.com/cn/tech/honor-ai-phone/) | 官方称可实时识别场景、推荐姿势并用样片预览指导 | OEM 已把实时场景和姿势指导放进原生相机 |
| A | [Samsung Shot suggestions](https://www.samsung.com/us/support/answer/ANS10010343/) / [iPhone Camera tools](https://support.apple.com/guide/iphone/set-up-your-shot-iph3dc593597/26/ios/26) | 构图对齐、场景优化、网格、水平、曝光和镜头控制 | 基础构图与相机控制是系统基线 |
| A | [Open Camera](https://play.google.com/store/apps/details?hl=en-US&id=net.sourceforge.opencamera) | Google Play 当日显示 1 亿+ 下载、约 28.7 万条评价，并提供大量控制和适配选项 | Android 相机首先要证明兼容、取景和保存可靠 |
| B | [浅影 AI 相机](https://www.qianying-ai.com/) / [GudoCam](https://gudocam.com/en/) | 实时构图、主体位置、姿势、语音、曝光/变焦建议；GudoCam 还提供端侧分析与按需讲解 | 端侧实时、语音、参数、解释和真实照片均已有重叠 |
| B | [PosePilot](https://apps.apple.com/gh/app/posepilot-ai-photo-coach-bf/id6793015171) | 商店页称最多三条建议、就绪分、稳定后自动拍和拍后解释 | 少建议、稳定判定和拍后原因也不是空白 |
| B | [Cue](https://play.google.com/store/apps/details?id=com.orange.cue) / [SnapPose](https://apps.apple.com/us/app/snappose-ai-photo-pose-ideas/id6760754230) / [Photogenik](https://apps.apple.com/us/app/photogenik-pose-photo-coach/id6779983641) | Android 实时教练、场景/图片/地标姿势推荐、轮廓相机和姿势叠图 | Android、场景推荐和轮廓只能证明同类供给存在 |
| C | [PoseOverlay](https://poseoverlay.com/) | 官网称有姿势库、身体匹配、语音、自动拍，以及陌生人简化界面、大按钮和三张连拍 | 代拍闭环不能写成功能护城河 |
| C | [Pix](https://pix-lens.com/) / [SnapStance](https://snapstance.com/) / [FrameMe](https://www.frameme.app/) | 前两者直接面向伴侣/男友帮拍；三者提供拍摄意图、方向指令、姿势构图或距离/光线提示 | “不会拍照的伴侣”也是已有定位 |

### 15.3 结论

- 没有已确认的功能护城河。
- Android、离线、语音、分步、姿势轮廓、代拍、自动拍和真实照片都不能单独写成壁垒。
- shooter / subject / proxy 是实现结构，不是用户可感知的差异化。
- 同类供给拥挤，但不少样本仍是小体量商店应用、Beta 或落地页；现有证据也不能证明需求足够强。
- “最多两步就拍”是主动选择的体验约束，不宣称独有。只有第 3.3 节盲评、完成时间和自然复用过线，才能升级为正式定位。

### 15.4 竞争取舍

| 市场常见做法 | 本产品决定 |
| --- | --- |
| 实时显示多项建议 | 一次一条，默认最多两个必做动作 |
| 就绪分、匹配分和对齐百分比 | 不显示分数，不让用户追分 |
| 稳定后自动拍 | P-1 不做；P0 也默认关闭 |
| 数百姿势和模板商城 | 先验证第 11.5 节短口令核 |
| 拍后评分、课程和挑战 | P0 不做 |
| 云端连续视觉分析 | 主路径端侧；P0 仅按需单帧讲解 |
| 订阅或免费姿势额度 | 基础教练、口令、快门和保存免费 |

### 15.5 本轮提示需求的官方依据与边界

| 来源 | 已核对事实 | 对本需求的结论 |
| --- | --- | --- |
| [W3C WCAG 2.2：Timing Adjustable](https://www.w3.org/WAI/WCAG22/Understanding/timing-adjustable.html) | 非必要的阅读时限应允许关闭、调整或延长；消失后没有其他获取途径的临时消息会形成时间限制 | 被拍者动作不是必须限时完成的事件，因此不能只显示 2.5 秒就永久消失；字幕应由动作生命周期控制。该网页无障碍标准不是 Android 原生应用的强制合规声明，只作为“给用户足够阅读和反应时间”的设计依据 |
| [Android TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech) | `QUEUE_FLUSH` 会丢弃旧播放队列并由新条目替换，`stop()` 会中断当前朗读并清空待播内容 | 实时画面确认旧口令失效时，应停止旧句并播稳定后的新句，不允许把过期指令排队播完 |
| [Android 11 TTS 可见性](https://developer.android.com/about/versions/11/behavior-changes-11) / [小米 TargetSdk 30 适配指南](https://dev.mi.com/xiaomihyperos/documentation/detail?pId=1738) | Android 与小米均要求目标 Android 11+、需要与 TTS 引擎交互的 App 在清单声明 `android.intent.action.TTS_SERVICE` 查询 | P-1 清单补齐该查询，修复目标机上因软件包可见性导致的 TTS 引擎绑定风险；这不是新权限，也不引入口令识别或麦克风 |
| [Android TTS Voice](https://developer.android.com/reference/android/speech/tts/Voice) | TTS 引擎可暴露多个音色，并可标记某个音色是否需要网络 | 优先选择已安装的简体中文离线音色；OEM 引擎未枚举音色时回退其简体中文语言设置，真实外放仍须在小米 14 Pro 验证 |
| [Apple 相机网格与水平仪](https://support.apple.com/guide/iphone/set-up-your-shot-iph3dc593597/ios) / [Pixel Framing hints](https://support.google.com/pixelcamera/answer/14106982) | 两者都把网格、水平或倾斜校正作为拍前取景辅助，而不是拍后评分 | 保留独立网格/水平仪，并把“放平”改成拍摄者能沿网格执行的具体动作 |
| [Nikon 构图指南](https://www.nikonusa.com/learn-and-explore/c/tips-and-techniques/5-easy-composition-guidelines) | 官方指南把三分交点、水平线校正、主体朝向空间作为常见构图起点，同时明确这些是可打破的指导而非硬规则 | 只在“人带景且人物仍居中”时建议左右三分线；中心人像不一律判错，也不加入当前信号无法判断的朝向空间 |
| [CameraX ImageAnalysis](https://developer.android.com/media/camera/camerax/analyze) / [ML Kit Pose Detection](https://developers.google.com/ml-kit/vision/pose-detection/android) | CameraX 可只保留最新分析帧；ML Kit 的 `STREAM_MODE` 用于相关视频帧并利用跟踪降低延迟、平滑检测 | 口令输入应持续取最新画面，而不是第一次命中后冻结；帧丢弃和流模式支持“最新状态优先”，但 300ms 采样、600ms/三帧稳定门槛是本产品的可测防抖参数，须由真机误报率继续校准 |
| [CameraX Zoom](https://developer.android.com/media/camera/camerax/configuration#zoom) | 变焦比必须位于相机报告的最小/最大范围内；越界会失败 | 双指缩放必须尊重小米 14 Pro 实际报告的范围；快捷焦段只显示经真机标定的镜头，模拟器继续用于验证“不支持时不虚报” |

联网复核结论：四种提示组合、字幕按动作生命周期常驻、口令按稳定后的最新画面替换是合理需求；“永远不变地常驻”不合理，动作已完成、失效或被新建议替代时必须清除。外部资料支持设计方向，不证明 600ms/三帧是最优值，该数值仍以第 9 节真机与用户测试为准。

### 15.6 最大市场风险

| 风险 | 验证或停止条件 |
| --- | --- |
| 动态指导不比静态卡有效 | 按第 3.3 节动态增量门槛；不过线停止扩实时分析 |
| 新鲜感不能转成复用 | D7 自然复用 ≥ 30%；不过线不靠玩法补指标 |
| 第三方成片输给系统相机 | 同机同景比较；先修拍照链路或改伴随方案 |
| 两步仍让人分心 | ≥80% 样本在两个动作内拍完；反向提示和重复 TTS 为阻断 |
| 语音让被拍者尴尬 | 打扰比例 >20% 时停止默认外放 |
| 功能迅速被 OEM 覆盖 | 不承诺功能独有，只用完成效率、偏好和复用证明价值 |

---
