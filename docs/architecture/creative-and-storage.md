# P1 创意、保存与 Motion Photo 专题

- 状态：当前架构规范附件
- 权威入口：[../architecture.md](../architecture.md)
- 产品约束：[P1 创意功能需求](../requirements/p1-creative.md)
- 原始位置：重构前架构第 4.5 节
- 迁移方式：Moved；领域模型、资源上限、分阶段保存、回退与 Motion Photo 细则完整保留

本专题属于单独批准的 P1 创意层，不改变 P-1 对照实验、Go 指标或完整 P0 门禁。参数建议遵循 [CP-03 决议](../traceability/decisions-and-conflicts.md)：用户主动打开独立面板，取景器保留单主动作；面板期间的视觉/TTS 与预算行为以 P1 创意需求为准。

---

### 4.5 小米 14 Pro P1 创意层

该层只依赖标准 Android/CameraX/MediaStore API，不读取小米传感器 ID，不调用厂商私有相机或相册接口。非目标设备继续走能力探测和安全回退，但不作颜色、速度或相册行为承诺。

纯 Kotlin 领域模型：

- `CreativeStyle` 固定定义原图、自然人像、柔光人像、清冷人像、清透旅行、森林清新、日落暖金、美食暖色、都市冷调、夜色霓虹、纪实黑白、高反差黑白，并用曝光、对比度、饱和度、色温、色调、褪色与强度生成同一确定性 4×5 颜色矩阵；预览、编辑和导出都消费该矩阵。
- `EditHistory` 保存上述七项调整的不可变 past/current/future 状态，提供 update / undo / redo / reset；对比原图只是 UI 状态，不进入历史；模型不保存 Bitmap，不持有 Android `Context`。
- `BurstSession` 目标状态包含 Idle → Capturing(1..3) → Complete，失败进入 CaptureFailed 或 SaveFailed；保存恢复进入 Saving，只有显式继续拍摄事件才返回 Capturing，启动必须来自用户已开启的三张模式；Capturing 状态拒绝重入。
- `PhotoQualityScorer` 在有上限的灰度采样上计算拉普拉斯清晰度和中间调曝光比例，使用固定权重和稳定的序号平局规则；分数只用于排序，UI 只显示可解释原因，不显示美学分。
- `ParameterCoach` 输入端侧 `Signals` 与 EV 步长/范围、焦段标定、扩展模式、AE/AF 锁、倒计时、捕获偏好、连拍、构图/画幅和保存能力快照；按稳定优先级输出最多三项。控件动作使用类型安全的 `ParameterAction`，物理动作携带原因和动作正文；输出不包含 ISO、快门速度、WB 或开尔文值。
- `CaptureIdentity` 目标合同区分 batchId、单张 captureId 和派生 derivativeId；批次序号关联单张，保存重试身份不变，再次捕获使用新 captureId。主 JPEG 与 Motion Photo 共享互斥主资产身份。阶段键为 captureId + 资产角色/derivativeId + 阶段，不能用批次 ID 单独判断发布完成。既有日志迁移和代码一致性尚未核验，保持 Unknown。
- `SaveCoordinator` 是可序列化的阶段状态机：SpaceCheck → Original/MotionPack → OriginalPublish → Recipe → OptionalDerivative → Complete/PartialFailure。每阶段有幂等 key，只重试失败阶段，原片成功后不回滚。
- `MotionPhotoAssembler` 负责纯字节领域逻辑：验证 JPEG EOI 与 MP4、扫描 JPEG APP 段、只替换本 App 可识别的纯 Motion XMP、拒绝无法安全合并的第三方/混合 Motion XMP、普通/扩展 XMP 与 GainMap、写入 Camera/Container XMP、计算 `Item:Length` 并确保视频紧密位于文件末尾；`MotionClipWindow` 计算约 1.5 秒前后、总长约 3 秒的裁剪范围。

#### 图像与保存管线

1. 每次 `ImageCapture` 先落到按 captureId 管理的持久私有暂存 JPEG；承诺保存恢复的源不能仅放可回收 cache。暂存与日志排除云备份/设备迁移，优先使用 `noBackupFilesDir` 或有明确排除规则的持久私有位置，并验证目标系统行为。目录有配额与空间预检；在受限采样上计算清晰度/曝光，不阻塞分析执行器。空间预检覆盖原片、处理峰值与所选兼容副本策略。
2. 默认只把原 JPEG 用 `ContentResolver.insert`、`DATE_TAKEN`、`IS_PENDING=1`、`RELATIVE_PATH=DCIM/拍照教练` 发布，再把编辑配方原子写入 App 私有目录。原片 URI 一经成功便不因配方、风格或编辑失败而撤销、覆盖或删除。
3. 非原图风格默认不生成效果 JPEG。用户明确“另存副本”或主动开启“原片+效果自动保存”后，才按完整质量/省空间档解码并生成独立兼容 SDR JPEG。原片压缩图像字节不经过转码，且不宣称 Ultra HDR 真机通过。
4. 派生图先读取尺寸边界，再按不超过 12,000,000 像素（省空间档使用更低上限）计算 `inSampleSize`。若捕获配方主动开启[自然上镜](beauty.md)，在有界可变源 Bitmap 上重新端侧检测并逐行混合美颜；随后 Canvas + `ColorMatrixColorFilter` 应用共享矩阵，JPEG 编码到新临时文件，再作为独立 MediaStore 项发布。OFF 保持原路径。
5. 轻编辑始终从用户选择的原片 URI 解码，沿用相同像素上限与矩阵构造；保存按钮只调用“另存副本”。安全 EXIF 复制仅允许方向、尺寸、拍摄时间和相机厂商/型号，排除 GPS、缩略图、MakerNote 与未知标签。
6. 保存 journal 在每个发布阶段前后原子落盘；启动恢复会续写 journal 中仍有效的 pending MediaStore 行，只删除无源文件、无法恢复或已完成的遗留项，同时清理过期临时视频，再按单张 captureId、资产身份及阶段恢复，不允许重复 MediaStore 项。任何解码/变换/编码/MediaStore 异常都返回部分成功状态和单阶段重试入口。
7. 已无恢复用途的临时文件在成功、失败和取消路径清理；可恢复失败保留必要源文件及日志直到成功或用户明确放弃；处理中最多持有源/目标两个受限 ARGB Bitmap，目标写完立即释放。OutOfMemoryError 作为受控失败回到原片，不继续重试扩大内存。
8. MediaStore 发布成功即进入非阻塞成功反馈，不显示保存确认页。最近一次 `CreativeResultUi` 仅作为内存中的可选编辑上下文保留，默认隐藏且不参与快门门禁；用户从创意菜单主动打开时才展示选片、编辑和拍后操作。

自然上镜使用 `beautyPreset + beautyEngineVersion` 捕获快照，随 `CapturedPhoto`、配方和 journal 传递；重试/另存不读取当前美颜开关。配方 schema 2、journal schema 3，旧记录缺字段为 OFF；不记录脸部坐标。额外 CPU 工作数组只按宽高各 1/4 的单通道图分配，RGB 逐通道处理，不新增全尺寸 Bitmap 或像素数组。ML Kit 内部内存与实际堆峰值仍需目标机测量。

连拍由 Activity 顺序调用现有单张 `ImageCapture` 三次，不并行提交 capture 请求。ViewModel 中的 `BurstSession` 在正在执行的批次期间拒绝重复启动批次，收集每张原片 URI 和评分；保存失败进入可重试/可结束的暂停状态，不无限伪装成捕获中。用户结束批次后按真实相机与空间状态开放新拍；三张完成后按总分、清晰度、曝光、序号稳定排序。单张失败即暂停余下请求，已发布照片不删除；保存重试只调用保存流程，成功后仍保持暂停；只有用户明确“继续拍剩余 N 张”才启动未捕获序号。恢复进程不自动捕获；结束本批保留已发布原片。此处为修订后的目标合同，既有实现和日志兼容性需另行验证。

预览使用 Android 12+ 的 `View.setRenderEffect(ColorMatrixColorFilter)`；低版本或渲染效果设置失败时显示原图预览和降级说明，拍摄/分析仍可用。系统屏幕色彩管理、预览 Surface 与 JPEG 编码可能造成细微差异，产品不承诺像素完全一致。

#### Motion Photo 管线

- 2026-09-13 用户选择保留四用例并接受双编码器资源成本。每段双路录制共享唯一代次，分别结束；重绑/退出先注销代次，旧回调只清理旧资源。`VIDEO_CAPTURE` 专用公开 Effect 把输入有界分发给 Recorder 与自有 MediaCodec。Recorder 仅承担公开会话/格式协商并产生有界、最终丢弃的临时输出；最终 Live 视频只来自自有编码器，不增加公开资产。
- 自有支路以实际消费的 SurfaceTexture sensor 时间、同相机/代次的 CaptureResult 精确关联、提交 PTS、实际非空非配置编码样本、实际 mux 首关键帧建立账本。缓存充分性与封面映射不使用 Recorder Start/Stats；首输入不等于首个保留样本。未知关联、乱序、重绑、截断或不足前后窗口均回退 JPEG，不 clamp 成功。UNKNOWN 跨系统时间源不等于同相机 sensor 域永远不可比较，但必须有逐帧同域证据；质量新鲜度门禁独立。
- 两路 EGL/编码输出采用有界资源与独立执行所有者，禁止在主线程、分析线程或同一 Effect 线程串行无界等待两路 swap。纹理/帧池、在途队列、双编码器、双 mux 临时文件及裁剪/封装峰值共同计入上限。SurfaceOutput 先停止写入和销毁自有 EGL window 再 close，不释放 CameraX 拥有的 Surface；输入等待 provideSurface 完成回调后释放。快门后停止期限仍以快门时刻为准，不在 JPEG 回调后额外等待完整 1.5 秒。

1. Live 开关开启后先用标准后摄的 `SessionConfig` 查询四用例及本次实际 Effect 配置；预检与绑定配置一致，候选按 HD、SD 排序，查询不支持则不绑定，查询异常时仍以实际绑定结果为准。CameraX 固定 1.6.1，使用公开 `VIDEO_CAPTURE`-only CameraEffect/SurfaceProcessor，不能调用 RestrictTo/private/reflection。两路均无音轨，文件、时长和大小有硬上限，不请求 `RECORD_AUDIO`；Live 与美颜互斥不变。
2. 录制生命周期维护快门前缓存；缓存充分时快门后约 1.5 秒停止，并用平台媒体变换裁出总长约 3 秒的 MP4。`ImageCapture` 同时得到原始 JPEG 封面；缓存不足不等待，按本次普通 JPEG 回退。封面时刻须映射到裁剪后的媒体时间轴；录制 Start、重绑和缓存轮换的代次不混用，时长及封面容差待专项冻结。指导和手动快门继续工作。
3. `MotionPhotoAssembler` 先扫描 JPEG APP 段。没有 XMP 时新增官方 v1 XMP；旧包与本 App 生成的纯 Motion 包一致时删除旧段并只写一份新段；发现第三方/混合 Motion XMP、非 Motion/扩展 XMP 或 GainMap 时抛出可恢复错误，由保存状态机发布未改写的普通 JPEG。成功路径对齐小米 14 Pro 原生 `MVIMG` 样本：写入 `GCamera:MotionPhoto=1`、`GCamera:MotionPhotoVersion=1`、封面展示时间，在两个 `rdf:li` 中分别嵌套 Primary/MotionPhoto `Container:Item`，MotionPhoto 项包含真实 `Item:Length` 与 `Item:Padding=0`；再追加 MP4，确保没有尾随字节。
4. 只发布一个以 `MVIMG_` 开头且符合 `^([^\\s/\\\\][^/\\\\]*MP)\\.(JPG|jpg|JPEG|jpeg)$` 的 MediaStore JPEG；容器主 JPEG 即 SDR 原片，不再另存重复静态原片。Live 选择强制普通 Photo 和 `OUTPUT_FORMAT_JPEG`，Extensions 与 Ultra HDR 不参与四用例绑定。
5. 录制、裁剪、XMP、空间或发布失败均进入普通 JPEG fallback 阶段；状态机保证最终至多一个主文件。滤镜只生成可选静态兼容 SDR 副本，主 Motion Photo 保持原始封面颜色。
6. 进程恢复根据 journal 和 MediaStore URI 幂等完成或回退；清理所有临时 MP4/JPEG、超限录制和 pending 行。是否被 HyperOS 相册识别、播放、保留广色域或 Ultra HDR 均为真机 `NotRun`。

2026-09-12：上述为修订后目标合同；持久暂存、备份排除、崩溃窗口核验和缓存不足 Live 回退的代码符合性 Unknown，验证 NotRun。Android cache可回收与Auto Backup依据见[逐项审核矩阵](../research/requirements-line-audit-2026-09-12.md)。临时循环视频仍可放有界cache，不能把清理它的策略用于尚待恢复的照片源。

### 2026-09-13 Live 启动与录制预算

59/60轮实际日志证实旧实现从Recorder请求时开始8秒停止计时，导致相机协商和自有codec启动消耗录制预算：59轮codec开始时8秒任务已到期，仅保留2样本，之后先出现Recorder error8，自有MP4稍后才验证完成。这是已确认的起算混淆，不是由硬件能力未知就停止排查。

实现将Starting与Recording/Finishing分开：Starting从提交自有编码启动开始，12秒内必须收到自有codec已start且GL window/texture就绪的ready。ready不构成传感器/媒体时间锚；Sensor→PTS仍只使用实际SurfaceTexture输入、匹配的CaptureResult和保留编码样本。Recording从该ready的elapsed时间起算，8秒发出停止输入。64轮日志进一步证实EOS、codec/mux释放后的实际检查仍会触发旧drain+12秒计时，因此65轮明确分离Finishing：首次停止请求/输入关闭开始后最多12秒，且绝不晚于ready+20秒；重复停止不能延期。控制器与自有编码器共用MotionSessionBudget的绝对截止时间，成功接受也检查期限。主线程调度延迟从剩余预算扣除。Starting≤12秒、Recording≤8秒、Finishing≤12秒，逻辑端到端上限为32秒；正常按快门后的停止仍依赖实际曝光sensor+1.5秒已保留覆盖，可更早结束。媒体跨度仍≤8秒，文件仍≤24MiB，前缓冲不足时普通JPEG立即继续，不等待启动。

Recorder支路仅作CameraX协商输出，移除其早于自有ready起算的8秒duration limit，由控制器统一停止两路；其24MiB文件上限仍有效。取消、重绑或任一失败撤销代次，迟到回调不能重新发布；未达到ready/完成预算明确JPEG回退。期限限制逻辑决策和任务准入，不声称能强制终止阻塞的厂商native调用；原生资源与全局permit继续由原owner持有直到返回，不能在watchdog线程释放。以上为授权方案C内的工程资源预算，不是已冻结的目标机质量/封面容差。

JPEG 时间戳在同一 ImageProxy 的 try/finally 内读取并先投递，随后才执行磁盘准备；投递或准备异常都关闭该 proxy。停止预算在 finish/close/控制器取消入队之前登记，成功结果在主线程实际消费时再次检查绝对截止时间。JPEG 准备晚于视频完成时，最多暂存一份完成的视频，按 captureId 和 generation 汇合；错代、取消、失败或普通 JPEG 回退删除该完成文件。此阶段不并发写同一 journal，进程中断仍可恢复 JPEG，但不承诺恢复尚未登记的临时视频。

CaptureResult 的 sensor 观测不再等待主线程 AE/AF 状态更新：回调验证绑定代次并通过 volatile 当前处理器到达线程安全账本；不同 source 的旧回调忽略。输入停止只阻止继续提交视频帧，保留一个有界匹配账本，直到 mux 验证 sealing；完成后迟到结果不能改变已封存验证。新代次替换匹配账本，不访问回调线程上的可变 effects map。
