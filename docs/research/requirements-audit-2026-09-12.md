# 需求审核矩阵与联网优化建议

审核日期：2026-09-12。对象：当前 photo-coach 需求及其架构、验收附件。

后续状态：本文件保留首次25组问题及当时建议，不能单独充当每条需求审核完成凭据。用户随后要求逐项审核并优化，已建立[逐项矩阵](requirements-line-audit-2026-09-12.md)并按[修订记录](../traceability/requirements-optimization-2026-09-12.md)更新权威文档。下文“尚未批准/未改”等描述只反映首次报告时点；当前落实情况以修订记录为准。

本文件是研究与审核产物，**不是第二份 FR/UX 定义**。建议尚未批准，不修改 Scope、Delivery、Verification，不解锁 P0，不把已批准 P1 混入 P-1 实验。原始需求以[总纲](../requirement.md)、[FR 矩阵](../traceability/requirements-matrix.md)和[验收规范](../acceptance/acceptance-plan.md)为准。

## 1. 结论

需求需要优化，重点是能力语义、异常边界和可验收性。后摄单人、主路径离线、最多两步、快门优先、非破坏保存及三组对照实验的方向可保留；联网结果没有提供扩大功能范围的理由。

本次建立 25 个审核项，覆盖 FR-01～FR-38，并单列不属于单个 FR 的实验与文档治理问题。审核发现分为：

- **需修订**：现有文字会产生错误技术承诺，或不足以唯一验收。
- **待冻结**：原规范已经承认 Unknown，本次提出可执行的冻结方案，不当作新发现。
- **待验证**：合同基本明确，缺实现、设备或用户证据。
- **保留**：未发现需要改变的原则，不表示产品验收通过。
- **ConflictPending**：沿用 CP-01；本次不裁决。

优先级 A：正式实验或对应功能验收前处理；B：对应功能开放前处理；C：后续阶段门禁。这里的优先级不是产品 P0/P1。

## 2. 审核矩阵

证据栏的 S 编号对应第 4 节可点击的一手来源；“文档”表示内部一致性检查。责任均为建议角色，具体负责人尚未指定。

| 审核 ID | 需求/范围 | 检查结果与证据 | 优化或保留建议 | 关闭条件 / 建议责任 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| R-01 | 总纲 1、3；产品假设 | 保留。Pixel 已提供意图选择和分步指导，且要求联网；只能证明功能重叠，不能证明本产品留存或差异化成立。S01 | 继续验证“两步、离线、双方偏好”的组合价值；不以新增姿势/滤镜回应竞争 | 三条件原始数据、盲评与 D7 记录齐备；产品/研究 | A |
| R-02 | 总纲 3.2/3.3.1；FR-14；InScope | 待冻结（既有）。20 名首次用户×3 场景×3 条件，完整时是 180 轮、其中动态 60 轮；同人多轮和双方评分并非独立样本。文档、S10 | 冻结配对单元、双方汇总、平局/缺评、顺序平衡、失访、失败及主动退出规则；“高 10 个百分点”必须有明确比较基准 | 带异常的示例数据可以复算；冻结版本签署后才采正式 Go 数据；研究/产品 | A |
| R-03 | 总纲 3.3 保存 ≥99.5%；FR-12/14 | 待冻结（既有）。100 张专项不能证明长期可靠性；缺置信要求、样本量和停止规则。S10 | 分开报告首试成功与恢复；可考虑固定样本的单侧置信下界方案，示例见第 3 节，不把示例直接变门槛 | 冻结目标总体、负载分层、样本量、允许失败数和统计方法；研究/质量 | A |
| R-04 | FR-01/02/13；UX-01/02/11；InScope | 保留并补验收。仅相机权限、端侧模型、自己的 MediaStore 资产与目标设备方向相容。模型有内置/下载区别。S05/S08 | 增加“新安装、首次启动前断网、没有可用中文离线音色”的组合；区分视觉主路径通过与语音降级 | 真实 APK 权限/模型核对和离线全流程；Android/质量 | A |
| R-05 | FR-03/18；UX-23；InScope | 保留。Extensions 支持和 Analysis 支持必须分别核查，后者不支持时绑定会失败。S07 | 保留三用例与普通 Photo 回退；不得把系统相机同名 HDR/夜景当成第三方同等成片质量 | 记录实际镜头、系统、扩展、组合与绑定结果；目标机同景样片；Android | A |
| R-06 | FR-09/19；UX-18；InScope | **需修订**。相机专题用 AF+AE 测光点及 disableAutoCancel 推导“对焦和曝光已锁定”。测光区域不等于 AE 锁；新的 setLockingMode 标为 1.7.0-alpha03，仓库固定 1.6.1。S02 | 分开定义 AF/AE 的支持、请求中、确认、失败和解除；未证实 AE 锁不得显示双锁成功。评估当前版本标准 Camera2 路径或调整文案，不直接升级 alpha | 锁定后改变明暗并检查 CaptureResult/曝光响应；解除与重绑复测；Android/产品 | A |
| R-07 | FR-20；UX-22/24；InScope | **需修订**。“对焦优先”实际映射 MAXIMIZE_QUALITY；官方语义是画质与延迟取舍，不是等待合焦的快门门禁。S03 | 建议改名“画质优先 / 速度优先”；说明不承诺合焦后才拍，不改变随时快门 | 同步控件、默认值说明、FR/UX 与架构用语；产品/Android | A |
| R-08 | FR-05/07/32；UX-31/40；InScope 与 ApprovedSeparate 分开 | **需澄清并验证**。Face 未检出不等于画面里没有脸；官方 Pose 要求脸在画面中，只有单人能力，不能承诺遮脸后仍可靠。S04 | UX-31 分开“Face 检测漏检但新鲜 Pose 有效”和“实际遮脸后 Pose 也失效”；后者应恢复提示，不能沿用旧骨骼 | 启动即遮脸、跟踪中遮脸、部分遮挡、恢复与多人样张；感知/质量 | A |
| R-09 | FR-05/07/22；UX-06/25/38；InScope | 待冻结并细化。脸占比 5%/12% 的分母和几何度量未写清；ML Kit minFaceSize 是脸宽/图宽，不等于面积占比。人脸像素数、模糊和角度影响结果。S05/S06 | 每个信号写清分析裁切、宽比/面积比、输入分辨率、有效期、阈值和不可观测处理；100×100 是官方一般建议，不是硬保证 | 校准表含正例、反例、边界、Unknown 和有效期；感知/研究 | A |
| R-10 | FR-06/07/08；UX-04/16/17；InScope | 保留并补边界。一次一条、两步预算、统一结束判定合理；600ms 或三帧在可变采样率下不是同一时间保证。S11、文档 | “三次”限定为不同捕获时间的新鲜分析帧；冻结大间隔/热降频/旧结果处理及系统错误、多人、最短展示的优先关系 | 用旧帧、乱序帧、长间隔、热降频和 TTS 迟到事件验证不误 Ready；引擎/质量 | A |
| R-11 | UX-15/32；交互 5.4/6.8 | **ConflictPending（既有 CP-01）**。屏幕五分之一与取景器四分之一分母不一；48dp 触达、大字号要求已存在。S12、文档 | 保留双方；待决定可测分母、设计目标和最大容差。补大字号/横屏/TalkBack 时的布局优先级 | 用户裁决 CP-01；截图测量说明含系统栏、预览和操作区边界；产品/设计 | A |
| R-12 | FR-11/12/25/28/36；UX-05/10/27/34 | **需澄清**。全局“只在捕获中禁快门”与连拍整批禁用、保存失败等待重试并存。文档 | 区分防批次重入、当前捕获和待保存；明确失败后结束本批、下一次单拍、队列满及磁盘不足的反馈，不让保存状态无限冒充捕获 | 失败后重试/结束/新拍均有唯一状态转移；产品/Android | A |
| R-13 | FR-12/27/28/29/36；UX-10/29/34/44 | **需修订架构合同**。要求保留可恢复源直到成功或放弃，却把源放 cache；低存储时 Android 可回收 cache。S09 | 可恢复照片使用有配额、保留策略的持久私有暂存；可丢的预览/循环视频才放缓存；源丢失须明确不可恢复，不假重试成功 | 低空间、缓存被清理、杀进程、重启和磁盘满用例；存储/质量 | A |
| R-14 | FR-28/29/36；UX-34/44；ApprovedSeparate | 保留并待验证。ID、主资产互斥、阶段恢复合同已细化；旧日志映射仍 Unknown。S08、文档 | 以已知 URI+源文件+日志共同核对；补发布成功但日志写失败、验证失败但公开行已存在等中断窗口 | 每阶段前后中断，验证一个主资产、源保留、无零字节/重复；存储 | A |
| R-15 | FR-31；UX-36/37；ApprovedSeparate | 需补边界。官方规范定义视频时间轴上的封面时间戳；“前后各约 1.5 秒”尚未规定刚开启就拍、录像重启、关键帧裁剪及允许误差。S13 | 快门不等待缓存；缓存不足采用明确短片策略或普通 JPEG；冻结 JPEG/视频时钟映射、时长和封面偏差容差 | 开启即拍、缓存轮换时拍、连续拍、重绑、Finalize 错误及原生相册播放；媒体/质量 | B |
| R-16 | FR-37；UX-45；ApprovedSeparate | **需修订**。“Critical 及以上仍保证预览/快门/保存”超出 App 能控制的边界；Emergency 已可能关闭组件，Shutdown 要立即关机。S14 | 正常可用相机时保快门；系统撤回相机/热关机时明确错误、持久化恢复记录并停止新捕获，恢复后不自动补拍；补降温恢复滞回 | 模拟热回调与相机撤回，验证安全恢复；不需把真机加热到关机；Android/质量 | A |
| R-17 | FR-24/26/27/29/35；UX-26/28/29/43 | 保留并待验证。确定性配方、SDR 副本、原片保留明确；同矩阵不能证明真实色彩一致。文档 | 冻结色彩空间、降采样说明、代表性光照与人工评价规则；原片与派生图分别验证 | 受控色卡/人物样张、方向/尺寸/安全 EXIF 和失败恢复；图像/质量 | B |
| R-18 | FR-38；UX-46～49；ApprovedSeparate | 待冻结并验证。几何蒙版不等于皮肤/遮挡分割；现有专项覆盖合理，但观感、时延、内存的通过判据未完全量化。文档 | 为 OFF/自然/柔和冻结盲评判据和相对性能预算；保留实际处理状态、关键点失效归零与原片优先 | 眼镜/胡须/手挡脸/运动样张与长期负载证据；图像/研究 | B |
| R-19 | FR-23/32/33/34；UX-33/39/41/42 | 待验证。独立参数面板、可执行控件与人工灵感分离已明确；推荐/姿势目标机有效性未知。文档 | 保留现范围；把“可观察”逐条落实为证据与退出，重点排除无法识别的椅子、地标语义和真实深度 | 每条口令覆盖触发/退出/失效/未知；面板恢复不增加预算；引擎/质量 | B |
| R-20 | FR-04/09/10/18/21；UX-03/08/13/17/23/24 | 保留并清理残留歧义。11.4 已禁止场景自动 EV，但 6.4 仍出现“自动改变…EV”。文档 | 同步说明 EV 仅用户主动应用；定义单次照片与连续相机会话，分别说明意图、EV、焦段锁定何时重置 | 连拍/单拍/重绑/重启的状态表无歧义；产品/Android | A |
| R-21 | FR-15/16/17；GateLocked | 已知验收缺口，未批准开发。FR-16 的“拍三张”可能被误读成 P1 一次快门三张，交互只描述大快门。文档 | P0 开发前补专属 UX，明确三次手动单拍或另行批准批次；云端补取消、撤回、迟到回包和供应商保留策略验收 | P-1 Go 后完成 P0 合同，不以已有服务代码替代；产品/服务 | C |
| R-22 | FR-30；UX-30/35；ApprovedSeparate | 保留。非目标机非阻断已由 CP-05 解决；自己的媒体访问不等于所有系统版本都同一行为。S08 | 写清目标 OS/相册版本；minSdk 是安装技术基线，不承诺 Android 8/9 免存储权限同等保存，不为非目标机新增权限 | 目标 HyperOS 的打开/分享/收藏/回收站真实结果；Android | B |
| R-23 | 总纲 10.2；FR-01/02/14/17 | 待冻结（部分既有）。研究侧照片/录像与 App 非图像日志是不同数据流；当前导出负责人、期限未定。文档 | 形成字段、目的、获取方式、访问者、保存位置、保留/删除期限表，纳入照片/录像及匿名关联；上线前仍按原要求专项法律复核 | 研究知情材料、数据表及删除流程可执行；研究/隐私负责人 | A |
| R-24 | 所有 FR 的 Verification；架构 8、验收 9.2 | **需整理证据表述**。入口仍概括“无目标机连接证据”，FR 矩阵却引用 9 月 3 日部分目标机结果；新合同不能继承旧 Pass。文档 | 明确“该历史轮次未连接”和“完整当前合同未验收”；建立日期、构建、设备、用例子集与结果链接，不把部分通过升级为全通过 | 逐行可追溯，无历史 Fail 被 NotRun 擦除；质量/文档 | A |
| R-25 | 总纲 4、P1 12.1/12.2、后期附件 | 保留。InScope、GateLocked、ApprovedSeparate、Deferred 已区分；P1 候选并非全部获批。文档 | 继续隔离 P1 开关和 P-1 研究配置，后期能力不渲染入口；不将本次建议变成新功能排期 | 范围与研究配置核对；产品 | A |

## 3. 可讨论的修订稿

以下均是建议文本。原需求和 ConflictPending 不因本文件自动改变。

### 3.1 锁定与捕获偏好（R-06、R-07）

建议需求文字：“长按发起当前设备支持的对焦/曝光锁定。界面分别展示请求与确认结果；只有两项均有确认依据时显示‘对焦和曝光已锁定’。不支持的项目明确说明。解除和相机重绑后重新读取状态。”

当前固定 CameraX 1.6.1，不应把官网当前新增的 1.7 alpha 接口当作可直接使用的已交付能力。是否采用标准 Camera2 补足须另做版本和真机核查。[官方 Builder API](https://developer.android.com/reference/androidx/camera/core/FocusMeteringAction.Builder)

建议把“对焦优先 / 拍摄优先”改为“画质优先 / 速度优先”，沿用现有捕获模式映射，避免让用户以为前者保证合焦后才允许拍摄。[官方 ImageCapture API](https://developer.android.com/reference/androidx/camera/core/ImageCapture)

### 3.2 保存恢复（R-13）

建议需求文字：“凡向用户承诺可重试的已捕获照片，在发布成功或用户明确放弃前保存在受管理的持久私有暂存区。暂存有容量上限与空间预检；暂存失败或源文件丢失时明确显示无法恢复，不以保存重试再次调用相机。卸载、清除应用数据不属于可恢复保证。”

原因是 cache 可被系统在低存储时删除，不能独自承担持久恢复承诺。这是架构缺口判断，本次未审核实际代码是否已有额外保护。[Android 应用专属存储](https://developer.android.com/training/data-storage/app-specific)

### 3.3 热状态（R-16）

建议需求文字：“App 自身降载不得以指导或创意功能阻断普通快门。系统仍提供相机时优先保留普通拍摄；系统热保护撤回相机、进入 Emergency/Shutdown 或终止进程时，停止新捕获，尽可能保留可恢复记录并展示原因。重新可用后由用户主动继续。”

这保留快门优先原则，同时承认系统关闭硬件的边界。[PowerManager 热状态定义](https://developer.android.com/reference/android/os/PowerManager)

### 3.4 实验与可靠性（R-02、R-03）

建议先冻结“至少 20 人”为探索性产品实验下限，是否用于统计推断另写清楚。三种条件可使用六种顺序平衡分配，并按场景记录；同一参与者、同一被拍者或同一配对的重复数据须关联，不能把 180 轮当作 180 个独立用户。汇总时保留双方分歧，并同时报告样本数和不确定性。

动态增量建议选择一套明确方案：独立做动态与静态的配对盲评，并预先定义平局计法及偏好阈值；或比较二者各自对系统相机的胜出率差。两者不是同一指标，不能采样后择优解释。原“偏好或时间至少一项通过”的产品门槛如保留，两项都报告，统计推断时预先处理双路径选择。

可靠性数字示例（推导，不是已批准门槛）：假设试验独立同分布、固定样本且全部成功，成功率单侧 95% 精确下界为 `0.05^(1/n)`。要求该下界达到 0.995，需 `n ≥ ceil(log(0.05)/log(0.995)) = 598`。100/100 的相应下界约 97.05%。有失败时必须重新按精确二项式计算；连拍、热量、磁盘等相关性会影响独立假设，598 次相同操作不能直接代表所有运行条件。[NIST 精确二项式置信区间](https://itl.nist.gov/div898/software/dataplot/refman2/auxillar/exacbino.htm)

### 3.5 CP-01（R-11）

需要用户最终决定的内容：以全屏、扣系统栏的可用区域还是可见取景器为分母；设计目标和最大容差分别是多少；大字号下如何例外。建议采用可测的统一分母，并给出默认字号截图测量示例。本次不代填 20%/25%，保持原有双方与 ConflictPending。

## 4. 联网证据索引

访问日期均为 2026-09-12。API 页是滚动文档，具体方法须核对 Added in 与仓库固定版本；网页事实不等于小米 14 Pro 实测结论。来源摘要为转述。

| ID | 一手来源 | 本次核对事实与边界 |
| --- | --- | --- |
| S01 | [Google Pixel Camera Coach](https://support.google.com/pixelcamera/answer/17367411?hl=en) | 需要网络、后摄、意图选择与分步指导；不证明留存或效果优于本产品 |
| S02 | [FocusMeteringAction.Builder](https://developer.android.com/reference/androidx/camera/core/FocusMeteringAction.Builder) | 测光区域与锁定独立；disableAutoCancel 只取消自动解除；setLockingMode 新增于 1.7.0-alpha03 |
| S03 | [ImageCapture](https://developer.android.com/reference/androidx/camera/core/ImageCapture) | MAXIMIZE_QUALITY / MINIMIZE_LATENCY 对应画质与延迟取舍 |
| S04 | [ML Kit Pose detection](https://developers.google.com/ml-kit/vision/pose-detection) | Beta、脸须在画面内、允许部分身体；不能把 Pose 的有无当作多人完整检测 |
| S05 | [ML Kit Face Android](https://developers.google.com/ml-kit/vision/face-detection/android) | 模型内置/下载两种方式；一般建议输入至少 480×360、脸至少 100×100；不是检测成功保证 |
| S06 | [Face detection concepts](https://developers.google.com/ml-kit/vision/face-detection/face-detection-concepts) | 最小脸尺寸是宽度比例，分类受人脸方向影响；不替代业务阈值校准 |
| S07 | [ExtensionsManager](https://developer.android.com/reference/androidx/camera/extensions/ExtensionsManager) | isImageAnalysisSupported 为 false 时带分析绑定会失败 |
| S08 | [MediaStore 与共享媒体](https://developer.android.com/training/data-storage/shared/media) | Android 10+ 访问/修改本 App 拥有的媒体无需存储权限；所有权和系统版本有边界 |
| S09 | [应用专属存储](https://developer.android.com/training/data-storage/app-specific) | 低存储时 cache 文件可被系统删除 |
| S10 | [NIST Exact Binomial](https://itl.nist.gov/div898/software/dataplot/refman2/auxillar/exacbino.htm) | 比例点估计与单/双侧置信界不同；统计公式不替产品决定置信门槛 |
| S11 | [CameraX ImageAnalysis](https://developer.android.com/media/camera/camerax/analyze) | latest-only 丢弃旧帧并要求关闭 ImageProxy；不证明产品防抖参数最优 |
| S12 | [Compose 无障碍 API 默认行为](https://developer.android.com/develop/ui/compose/accessibility/api-defaults) | 最小触控目标及触达扩展；不提供本产品底栏占比结论 |
| S13 | [Motion Photo format 1.0](https://developer.android.com/media/platform/motion-photo-format) | 目录、视频及封面时间戳规范；格式合规与 HyperOS 播放需分别验证 |
| S14 | [PowerManager](https://developer.android.com/reference/android/os/PowerManager) | Critical、Emergency、Shutdown 含义不同，后两者不允许承诺硬件始终可用 |

市场部分只复核与核心流程直接相关的 Pixel 官方资料，并未重做全部竞品清单。隐私部分是需求完整性检查，未执行专项法律审查，不给出法律合规结论。

## 5. 覆盖、执行与证据边界

FR 覆盖对照（仅表明已审阅对应合同）：

| FR | 审核项 |
| --- | --- |
| FR-01/02/13 | R-04、R-23 |
| FR-03/18 | R-05、R-20 |
| FR-04/09/10/21 | R-20；FR-09 另见 R-06 |
| FR-05/07/22 | R-08～R-10 |
| FR-06/08 | R-10 |
| FR-11/12 | R-03、R-12～R-14 |
| FR-14 | R-02、R-03、R-23 |
| FR-15/16/17 | R-21、R-23 |
| FR-19/20 | R-06、R-07 |
| FR-23/32/33/34 | R-19；FR-32 另见 R-08 |
| FR-24/26/27/29/35 | R-17；保存另见 R-13、R-14 |
| FR-25/28/36 | R-12～R-14 |
| FR-30/31/37/38 | R-22、R-15、R-16、R-18 |

- Changed：只新增本研究文件；业务代码、权威 FR/UX 与冲突决议未改。
- Scope：P-1 InScope；P0 GateLocked；已批准 P1 ApprovedSeparate；后期 Deferred。
- Delivery：本次是需求/架构合同审核，未做业务代码完整性审计，代码符合性 Unknown。
- Verification：本轮 JVM/.NET、instrumented、目标机与产品验收均 NotRun；不继承历史 Pass。外部资料只验证公开 API 语义。
- Rules：已读取 requirement.md、architecture.md；requirements 下交互、P1 创意、自然上镜、后期附件；acceptance-plan.md；requirements-matrix.md、decisions-and-conflicts.md；architecture 下 camera-and-perception、guidance-and-explain、creative-and-storage、beauty、decisions；research/evidence.md；gradle/libs.versions.toml；photo-coach-coding SKILL.md 及 references/version-scope.md、sources.md。未以 history 作为当前依据。
- Validated：文档 FR 覆盖、研究文件本地相对链接与 Git 差异范围检查；联网搜索并打开上述官方资料；可靠性示例数值复算。
- NotRun：实现符合性测试、APK 审计、模型/音色离线实测、锁定 CaptureResult、真实保存/恢复、HyperOS Live 播放、热/颜色/美颜及用户对照实验；全部竞品更新与专项法律审查。
- Residual risk：CP-01 仍待决；研究参数未冻结；持久源、AE 锁定、感知条件与系统热关闭边界需落实到权威规范后重新验收。

建议下一轮先处理 R-06/07/13/16 的确定性语义问题，再冻结 R-02/03/09/11；每项经决策后同步需求附件、FR/UX、架构和证据状态。既有需求变更须以新增覆盖重新验证，不能用本报告直接关闭验收。
