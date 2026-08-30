# 拍照教练：技术架构

- 状态：已按需求拍板，第一版采用方案 A
- 更新日期：2026-08-30
- 产品依据：[requirement.md](requirement.md)

本文只写怎么实现。产品范围、口令内容和验收以需求文档为准。

---

## 1. 结论

取景器这一层用 **Android 原生**，P-1、P0 和 P1 只验收 **小米 14 Pro**。以后如果正式立项其他 Android 或 iOS，再扩充目标机与原生实现；场景口令 JSON 和信号字段保持平台无关。

「再讲细」API 用 **C# / ASP.NET Core**。C# 主场在服务端和口令数据，不在取景器。

---

## 2. 为什么不跨平台画相机

需求要 250–300ms 出提示、叠线、语音指挥被拍者。这依赖每帧分析。

2026 年对照：相机管线、实时视觉这类能力，跨平台抽象经常接不到每帧。React Native 侧有实测：CameraX+ML Kit 走原生约 80–120ms，帧过 JS 桥约 200–400ms，还会卡界面。MAUI CommunityToolkit `CameraView` 底层虽是 CameraX，但不开放 `ImageAnalysis` 每帧回调。

为 iOS 预留的不是「一套 UI 两边跑」，而是同一份口令表。口令本就是查表，JSON 两边读，比把 CameraX 包进 MAUI/Flutter 再迁 iOS 更稳。

---

## 3. 方案对比

| 做法 | Android 取景器 | 以后 iOS | 对 C# 开发者 | 结论 |
| --- | --- | --- | --- | --- |
| A. Kotlin 原生 + JSON 口令 | CameraX / ML Kit / Extensions 官方示例最多 | Swift + AVFoundation + Vision，读同一份 JSON | 要学 Kotlin（和 C# 最像）；API 仍用 C# | **第一版采用** |
| B. MAUI/C# 调 CameraX 绑定 | 绑定存在，文档和坑在 Java/Kotlin | 另写 iOS Handler | 少换语言，相机调试更难 | 备选 |
| C. 第一版就上 KMP | 相机仍要接 CameraX | 共享口令引擎更完整 | 学习面过大 | iOS 立项后再评估 |

不采用 Flutter / React Native 画整个取景器。

---

## 4. 第一版结构

```
androidApp（Kotlin + Jetpack Compose）
  取景器（点按对焦、AE/AF 锁、能力驱动 EV/快捷焦段/双指缩放/扩展模式、闪光、倒计时、画幅、拍摄偏好、音量键快门）、叠线、字幕、持久化设置、TTS、开相机说明、权限、MediaStore
  P1 创意层（专业参数建议、12 风格共享矩阵、七项非破坏编辑、captureId 分阶段保存、无声 Android Motion Photo、明确三张连拍与本地确定性选优）

coach/（无 Android UI）
  scenes.json     需求第 11 节参数库（P0 人像光位+旅行+建筑/街拍；P1 默认关闭）
  匹配规则        信号 → 最多 3 条口令（shooter / subject / proxy）；更具体的包优先
  场景起始参数    认出场景后套用模式/镜头/对焦点/EV 方向/关闪光（需求 11.4）
  短口令核        约 8–12 条跨场景指挥句 + 每场景 1–2 条特有句（需求 11.5）

ExplainApi（C# / ASP.NET Core 10，可后做）
  压缩帧 + 已有信号 → 视觉大模型
  失败不影响取景器
```

目标设备约束：

- P-1、P0 和 P1 的唯一验收机型是小米 14 Pro；每次真机结果必须记录地区版本、Android/HyperOS 版本、Build fingerprint 和 App 版本。
- 其他 Android 机型不进入兼容矩阵、不作功能或成片质量承诺，也不为它们新增厂商分支；保留标准 CameraX 能力探测和安全回退，不设置无必要的安装白名单。
- 快捷焦段不能硬编码为 2x。启动时枚举 CameraX 可选择的后摄 `CameraInfo`，用 Camera2 `LENS_INFO_AVAILABLE_FOCAL_LENGTHS` 与 CameraX `intrinsicZoomRatio` 去掉无元数据和重复候选；按钮倍率来自相对视角能力，不读取厂商传感器 ID。候选为空、失效或绑定失败时只保留默认后摄。最终仍以小米 14 Pro 真机预览、成片视角和 EXIF 标定标签；未经真机验收不得把按钮宣称为已验证光学焦段。
- 小米系统相机公开提供的徕卡风格、可变光圈、夜景或人像能力，不等于第三方 CameraX 一定可控；只使用标准 API 实际暴露且能与实时 `ImageAnalysis` 共存的能力，不接未文档化的厂商私有接口。

### 4.1 相机管线

- CameraX 普通主路径：`Preview` + `ImageAnalysis` + `ImageCapture`
- 显式 Live 且能力允许时：`Preview` + `ImageAnalysis` + `ImageCapture` + 无音轨 `VideoCapture<Recorder>`；Live 一律使用标准 Photo 选择器，不启用 Extensions。四用例或编码器绑定失败立即回普通三用例并向 UI 返回明确降级原因。
- `PreviewView` 使用 `FILL_CENTER` 覆盖长屏取景区；竖屏操作区叠在预览底部，避免 4:3 / 16:9 surface 居中产生额外黑带。三用例放入带 `PreviewView.viewPort` 的 `UseCaseGroup`，使预览可见裁切、ML Kit view-referenced 坐标和捕获裁切使用同一视口。
- 分析策略：`STRATEGY_KEEP_ONLY_LATEST`，分析完必须 `ImageProxy.close()`
- `Preview`、`ImageAnalysis`、`ImageCapture` 使用同一 4:3 或 16:9 `AspectRatioStrategy`；分析分辨率只作为性能偏好，不能用 UI 裁切伪造画幅。
- 成片：`ExtensionsManager` 查询后，扩展可用且 `isImageAnalysisSupported` 为真，才开 `NIGHT` / `BOKEH` / `HDR` 并同时绑分析；扩展不可用，或扩展可用但分析绑不上，则标准 Preview + Analysis + Capture，口令照给。不要为成片卸掉分析（需求：保口令、弃扩展成片）
- 不要用 `ExtensionSessionConfig` 当默认绑定（该会话不支持 ImageAnalysis）
- 第一版不开 `FACE_RETOUCH`，不接自研滤镜 / LUT / `CameraEffect` 调色层
- 第 12.2 节 P1 创意风格不进入 CameraX use case：原片仍由上述捕获管线生成，取景预览仅在支持时对 `PreviewView` 使用共享颜色矩阵的显示效果，导出在捕获后的受限内存处理器中另存兼容 SDR 副本；任何效果失败都回退原片，不能卸载或替换 `ImageAnalysis`
- 能力状态在每次镜头/画幅/模式重绑后重建：`ZoomState` 提供连续缩放范围，`ExposureState` 提供 EV 索引范围与步长，Extensions 双查询提供可选模式。旧设置越界时夹紧，旧模式不可用时回到“自动/普通”，不沿用陈旧能力。
- 长按预览使用 `FocusMeteringAction` 的 AF+AE 且关闭自动取消；成功后显示锁定并提供显式 `cancelFocusAndMetering` 解除。任何 use case 重绑都会把 UI 锁定状态恢复为未锁，避免状态假死。
- “对焦优先”映射 `CAPTURE_MODE_MAXIMIZE_QUALITY`，“拍摄优先”映射 `CAPTURE_MODE_MINIMIZE_LATENCY`；两者都不改变三用例同绑和手动快门优先。
- 倒计时只延迟一次用户发起的捕获，支持关闭/3 秒/10 秒；再次按屏幕或音量键快门取消，不进入自动连拍。
- 这是第三方走厂商计算摄影的正路，对应需求「成片优先走系统计算摄影，口令分析不能停」

### 4.2 感知

- ML Kit Face：可多人，第一版只用于「两张脸以上不要套单人剪影」
- ML Kit Pose：单人骨骼
- Face 与 Pose 独立生成可靠性：Face 未命中但 Pose 上半身可靠且未检测到多张脸时，允许保守单人姿势提示；两者都不可用时进入可恢复的“请露出脸，或靠近一点”状态，不得落入“可以拍了”。
- 另算：水平倾角、脸占比、脸框横向是否居中、特写脸框是否明显落在下半部、脸/画面亮度、粗场景
- 不做人脸比对，不存底库，不识别「这是谁」
- 镜头检查只在连续多帧同时满足极暗、低方差时置位；单帧不触发，恢复也需连续清晰帧。该信号只能说明“可能遮挡或弄脏”，不能区分镜头盖、手指、暗室或污渍，文案必须保守。

### 4.3 口令引擎

- 输入：上述信号
- 输出：最多 3 条人话，带听众字段
- 语音播报当前稳定的 `shooter` 或 `subject` 口令；拍摄者动作卡始终可见，中文语音和被拍者字幕作为两个独立、持久化的输出开关
- Android 11+ 清单声明 `TTS_SERVICE` 可见性；初始化优先选已安装的简体中文离线音色，OEM 引擎不提供音色枚举时回退 `setLanguage(zh-CN)`，仍不新增麦克风或网络依赖
- CameraX / ML Kit 回调只把最新分析帧投入容量为一的事件流；`GuidanceSession`、100ms tick、UI 状态与 TTS 回调统一在主线程 reducer 中串行处理，避免真机多线程竞态和旧帧积压
- Android 每约 300ms 将最新信号送入口令引擎；节奏器按 600ms/连续三帧防抖更新当前动作槽，动作完成、失效或被稳定的新候选替换后不得继续保留旧口令
- 姿势只匹配短口令核 + 当前场景特有句，不维护男/女姿势大库
- JSON 进仓库，用单测锁回归样张（需求 11.3）和核口令（需求 11.5）

### 4.4 讲解 API

- 默认关；用户点「再讲细」且已单独同意后才上传当前帧（短边 512–768）
- 请求带上端侧已算信号和已提示的 3 条
- 返回固定 JSON：最多 3 条指令 + 一句原因，每条带听众；缺听众或术语（光圈/ISO）客户端丢掉
- 供应商可替换
- 主路径不依赖本 API，飞行模式取景器仍可用

### 4.5 小米 14 Pro P1 创意层

该层只依赖标准 Android/CameraX/MediaStore API，不读取小米传感器 ID，不调用厂商私有相机或相册接口。非目标设备继续走能力探测和安全回退，但不作颜色、速度或相册行为承诺。

纯 Kotlin 领域模型：

- `CreativeStyle` 固定定义原图、自然人像、柔光人像、清冷人像、清透旅行、森林清新、日落暖金、美食暖色、都市冷调、夜色霓虹、纪实黑白、高反差黑白，并用曝光、对比度、饱和度、色温、色调、褪色与强度生成同一确定性 4×5 颜色矩阵；预览、编辑和导出都消费该矩阵。
- `EditHistory` 保存上述七项调整的不可变 past/current/future 状态，提供 update / undo / redo / reset；对比原图只是 UI 状态，不进入历史；模型不保存 Bitmap，不持有 Android `Context`。
- `BurstSession` 只允许 Idle → Capturing(1..3) → Complete/Failed，启动必须来自用户已开启的三张模式；Capturing 状态拒绝重入。
- `PhotoQualityScorer` 在有上限的灰度采样上计算拉普拉斯清晰度和中间调曝光比例，使用固定权重和稳定的序号平局规则；分数只用于排序，UI 只显示可解释原因，不显示美学分。
- `ParameterCoach` 输入端侧 `Signals` 与 EV 步长/范围、焦段标定、扩展模式、AE/AF 锁、倒计时、捕获偏好、连拍、构图/画幅和保存能力快照；按稳定优先级输出最多三项。控件动作使用类型安全的 `ParameterAction`，物理动作携带原因和动作正文；输出不包含 ISO、快门速度、WB 或开尔文值。
- `CaptureIdentity` 生成不可复用 `captureId`，统一构造原片、Motion Photo、效果副本、连拍序号和配方名；显示名不依赖同毫秒唯一性。
- `SaveCoordinator` 是可序列化的阶段状态机：SpaceCheck → Original/MotionPack → OriginalPublish → Recipe → OptionalDerivative → Complete/PartialFailure。每阶段有幂等 key，只重试失败阶段，原片成功后不回滚。
- `MotionPhotoAssembler` 负责纯字节领域逻辑：验证 JPEG EOI 与 MP4、注入 Camera/Container XMP、计算 `Item:Length`、确保视频紧密位于文件末尾；`MotionClipWindow` 计算约 1.5 秒前后、总长约 3 秒的裁剪范围。

图像与保存管线：

1. 每次 `ImageCapture` 先落到 App cache 的 captureId 临时 JPEG；在受限采样上计算清晰度/曝光，不阻塞分析执行器。空间预检覆盖原片、处理峰值与所选兼容副本策略。
2. 默认只把原 JPEG 用 `ContentResolver.insert`、`DATE_TAKEN`、`IS_PENDING=1`、`RELATIVE_PATH=DCIM/拍照教练` 发布，再把编辑配方原子写入 App 私有目录。原片 URI 一经成功便不因配方、风格或编辑失败而撤销、覆盖或删除。
3. 非原图风格默认不生成效果 JPEG。用户明确“另存副本”或主动开启“原片+效果自动保存”后，才按完整质量/省空间档解码并生成独立兼容 SDR JPEG。原片压缩图像字节不经过转码，且不宣称 Ultra HDR 真机通过。
4. 派生图先读取尺寸边界，再按不超过 12,000,000 像素（省空间档使用更低上限）计算 `inSampleSize`。Canvas + `ColorMatrixColorFilter` 应用共享矩阵，JPEG 编码到新临时文件，再作为独立 MediaStore 项发布。
5. 轻编辑始终从用户选择的原片 URI 解码，沿用相同像素上限与矩阵构造；保存按钮只调用“另存副本”。安全 EXIF 复制仅允许方向、尺寸、拍摄时间和相机厂商/型号，排除 GPS、缩略图、MakerNote 与未知标签。
6. 保存 journal 在每个发布阶段前后原子落盘；启动恢复会续写 journal 中仍有效的 pending MediaStore 行，只删除无源文件、无法恢复或已完成的遗留项，同时清理过期临时视频，再按 captureId/阶段恢复，不允许重复 MediaStore 项。任何解码/变换/编码/MediaStore 异常都返回部分成功状态和单阶段重试入口。
7. 所有临时文件在成功、失败和取消路径清理；处理中最多持有源/目标两个受限 ARGB Bitmap，目标写完立即释放。OutOfMemoryError 作为受控失败回到原片，不继续重试扩大内存。
8. MediaStore 发布成功即进入非阻塞成功反馈，不显示保存确认页。最近一次 `CreativeResultUi` 仅作为内存中的可选编辑上下文保留，默认隐藏且不参与快门门禁；用户从创意菜单主动打开时才展示选片、编辑和拍后操作。

连拍由 Activity 顺序调用现有单张 `ImageCapture` 三次，不并行提交 capture 请求。ViewModel 中的 `BurstSession` 在整批期间保持快门禁用，收集每张原片 URI 和评分；三张完成后按总分、清晰度、曝光、序号稳定排序。单张失败即暂停余下请求，已发布照片不删除；只有用户明确重试成功才继续本批，否则终止。

预览使用 Android 12+ 的 `View.setRenderEffect(ColorMatrixColorFilter)`；低版本或渲染效果设置失败时显示原图预览和降级说明，拍摄/分析仍可用。系统屏幕色彩管理、预览 Surface 与 JPEG 编码可能造成细微差异，产品不承诺像素完全一致。

Motion Photo 管线：

1. Live 开关开启且四用例可绑定时，使用 `camera-video` 同版本的 `Recorder` 建立无音轨录制。录制文件、时长和大小均有硬上限；不开启音频，不请求 `RECORD_AUDIO`。
2. 录制生命周期维护快门前缓存；快门后约 1.5 秒停止，并用平台媒体变换裁出总长约 3 秒的 MP4。`ImageCapture` 同时得到原始 JPEG 封面；指导和手动快门继续工作。
3. `MotionPhotoAssembler` 向 JPEG APP1 注入官方 v1 XMP：`Camera:MotionPhoto=1`、`Camera:MotionPhotoVersion=1`、封面展示时间，以及 Primary/MotionPhoto 两项 Container Directory。之后原样追加 MP4，确保 `Item:Length` 等于真实视频字节数且没有尾随字节。
4. 只发布一个符合 `^([^\\s/\\\\][^/\\\\]*MP)\\.(JPG|jpg|JPEG|jpeg)$` 的 MediaStore JPEG；容器主 JPEG 即原片，不再另存重复静态原片。Live 选择强制普通 Photo，Extensions 不参与四用例绑定。
5. 录制、裁剪、XMP、空间或发布失败均进入普通 JPEG fallback 阶段；状态机保证最终至多一个主文件。滤镜只生成可选静态兼容 SDR 副本，主 Motion Photo 保持原始封面颜色。
6. 进程恢复根据 journal 和 MediaStore URI 幂等完成或回退；清理所有临时 MP4/JPEG、超限录制和 pending 行。是否被 HyperOS 相册识别、播放、保留广色域或 Ultra HDR 均为真机 `NotRun`。

---

## 5. 以后接 iOS

不必翻 Android 取景器。

| 留下不动 | iOS 新写 |
| --- | --- |
| `scenes.json` 和字段约定 | SwiftUI 取景器 |
| 信号 → 口令匹配（可抄薄模块或再抽 KMP） | AVFoundation 预览/拍照 |
| 讲解 API | Vision：脸、人体、水平 |
| 产品规则（三通道、自动快门默认关） | AVSpeechSynthesizer；系统相册 |

iOS 成片走系统 Photo 管线，对应 Android 的 CameraX Extensions。合影后期两边都先用多人脸。角色切换只换口令主通道，不换相机壳。

---

## 6. 版本与工程

- Android minSdk 26；CameraX 1.6+（避免 2026-11-01 起部分设备撤掉 ≤1.5 扩展）
- Extensions 按机型探测，且必须同时通过 `isExtensionAvailable` 与 `isImageAnalysisSupported` 才启用
- Kotlin + Compose + CameraX + `camera-extensions` + 与 CameraX 同版本的 `camera-video` + ML Kit；不做无关依赖升级
- 成片写入 `DCIM/拍照教练`（`RELATIVE_PATH` + `IS_PENDING`），无水印
- 第一版只申请 `CAMERA`；无声 Motion Photo 不启用音频，也不申请麦克风、定位、读整本相册
- 相机用户设置保存在同一私有 `SharedPreferences` 中：语音、字幕、网格、水平仪、倒计时、画幅、拍摄偏好和模式偏好均用安全枚举恢复；非法旧值回默认。一键重置只覆盖相机设置键，不清除端侧分析同意、不触碰照片。
- P1 创意偏好（当前风格、是否启用三张连拍、保存策略、副本质量与 Live 开关）可使用同一私有设置文件的独立键；重置相机设置不删除照片。编辑配方按 captureId 写 App 私有文件，编辑历史仍只保存在当前结果页，不写入原片元数据。
- 音量键映射为快门；代拍页屏幕常亮
- 讲解服务：.NET 10 Minimal API
- 第一版不上登录、EF、ABP、手机端大模型
- 第一版只验收小米 14 Pro 竖屏和横屏；其他机型后期单独立项

当前自动化环境未连接小米 14 Pro。CameraX/Camera2 能力枚举、回退策略和设置恢复可由 JVM 单测覆盖，但快捷焦段/EXIF、真实 Zoom/EV 范围、Extensions 三用例组合、AE/AF 3A 行为、画幅成片、音量键倒计时、连续 100 张保存、遮挡/污渍误报率、十二种风格与七项编辑真实颜色、三张连拍间隔/热量/推荐有效性、HyperOS MediaStore 分阶段保存/收藏/回收站、广色域/Ultra HDR，以及 Live 四用例绑定、编码、裁剪和 Motion Photo 播放均为 **NotRun**，不得据此宣称 HyperOS 真机通过。

---

## 7. 明确不选

- 用 MAUI / Flutter CameraView 当主取景器
- 自研 ISP / RAW 成片
- 通过 `CameraEffect`、LUT、小米私有 API 或覆盖原片实现 P1 风格
- 第一版就上 KMP 整包 UI
- 为了「纯 C#」把 CameraX 绑定当唯一实现
