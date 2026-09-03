# P1 创意、保存与 Motion Photo 专题

- 状态：当前架构规范附件
- 权威入口：[../architecture.md](../architecture.md)
- 产品约束：[../requirements/p1-creative.md](../requirements/p1-creative.md)
- 原始位置：重构前架构第 4.5 节
- 迁移方式：Moved；领域模型、资源上限、分阶段保存、回退与 Motion Photo 细则完整保留

本专题属于单独批准的 P1 创意层，不改变 P-1 对照实验、Go 指标或完整 P0 门禁。参数三卡与“一次一件事”的关系仍为 [CP-03](../traceability/decisions-and-conflicts.md)。

---

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
- `MotionPhotoAssembler` 负责纯字节领域逻辑：验证 JPEG EOI 与 MP4、扫描 JPEG APP 段、只替换本 App 可识别的纯 Motion XMP、拒绝无法安全合并的第三方/混合 Motion XMP、普通/扩展 XMP 与 GainMap、写入 Camera/Container XMP、计算 `Item:Length` 并确保视频紧密位于文件末尾；`MotionClipWindow` 计算约 1.5 秒前后、总长约 3 秒的裁剪范围。

图像与保存管线：

1. 每次 `ImageCapture` 先落到 App cache 的 captureId 临时 JPEG；在受限采样上计算清晰度/曝光，不阻塞分析执行器。空间预检覆盖原片、处理峰值与所选兼容副本策略。
2. 默认只把原 JPEG 用 `ContentResolver.insert`、`DATE_TAKEN`、`IS_PENDING=1`、`RELATIVE_PATH=DCIM/拍照教练` 发布，再把编辑配方原子写入 App 私有目录。原片 URI 一经成功便不因配方、风格或编辑失败而撤销、覆盖或删除。
3. 非原图风格默认不生成效果 JPEG。用户明确“另存副本”或主动开启“原片+效果自动保存”后，才按完整质量/省空间档解码并生成独立兼容 SDR JPEG。原片压缩图像字节不经过转码，且不宣称 Ultra HDR 真机通过。
4. 派生图先读取尺寸边界，再按不超过 12,000,000 像素（省空间档使用更低上限）计算 `inSampleSize`。若捕获配方主动开启[自然上镜](beauty.md)，在有界可变源 Bitmap 上重新端侧检测并逐行混合美颜；随后 Canvas + `ColorMatrixColorFilter` 应用共享矩阵，JPEG 编码到新临时文件，再作为独立 MediaStore 项发布。OFF 保持原路径。
5. 轻编辑始终从用户选择的原片 URI 解码，沿用相同像素上限与矩阵构造；保存按钮只调用“另存副本”。安全 EXIF 复制仅允许方向、尺寸、拍摄时间和相机厂商/型号，排除 GPS、缩略图、MakerNote 与未知标签。
6. 保存 journal 在每个发布阶段前后原子落盘；启动恢复会续写 journal 中仍有效的 pending MediaStore 行，只删除无源文件、无法恢复或已完成的遗留项，同时清理过期临时视频，再按 captureId/阶段恢复，不允许重复 MediaStore 项。任何解码/变换/编码/MediaStore 异常都返回部分成功状态和单阶段重试入口。
7. 所有临时文件在成功、失败和取消路径清理；处理中最多持有源/目标两个受限 ARGB Bitmap，目标写完立即释放。OutOfMemoryError 作为受控失败回到原片，不继续重试扩大内存。
8. MediaStore 发布成功即进入非阻塞成功反馈，不显示保存确认页。最近一次 `CreativeResultUi` 仅作为内存中的可选编辑上下文保留，默认隐藏且不参与快门门禁；用户从创意菜单主动打开时才展示选片、编辑和拍后操作。

自然上镜使用 `beautyPreset + beautyEngineVersion` 捕获快照，随 `CapturedPhoto`、配方和 journal 传递；重试/另存不读取当前美颜开关。配方 schema 2、journal schema 3，旧记录缺字段为 OFF；不记录脸部坐标。额外 CPU 工作数组只按宽高各 1/4 的单通道图分配，RGB 逐通道处理，不新增全尺寸 Bitmap 或像素数组。ML Kit 内部内存与实际堆峰值仍需目标机测量。

连拍由 Activity 顺序调用现有单张 `ImageCapture` 三次，不并行提交 capture 请求。ViewModel 中的 `BurstSession` 在整批期间保持快门禁用，收集每张原片 URI 和评分；三张完成后按总分、清晰度、曝光、序号稳定排序。单张失败即暂停余下请求，已发布照片不删除；只有用户明确重试成功才继续本批，否则终止。

预览使用 Android 12+ 的 `View.setRenderEffect(ColorMatrixColorFilter)`；低版本或渲染效果设置失败时显示原图预览和降级说明，拍摄/分析仍可用。系统屏幕色彩管理、预览 Surface 与 JPEG 编码可能造成细微差异，产品不承诺像素完全一致。

Motion Photo 管线：

- 每段录制由 `LiveRecordingSession` 的唯一代次拥有。重绑/退出先注销代次，旧 Start/Finalize 只可清理自己已结束的临时文件，不能清空、降级或打包新录制。缓存时间从编码器 Start 事件开始计算；快门后停止期限以快门时刻为准，不在 JPEG 保存回调后再额外等待完整 1.5 秒。Finalize 即使没有 cause 也必须按 error code 处理失败，不能误报无错。

1. Live 开关开启后先用标准后摄的 `SessionConfig` 查询四用例组合；候选按 HD、SD 排序，查询不支持则不绑定，查询异常时仍以实际绑定结果为准。成功后使用 `camera-video` 同版本的 `Recorder` 建立无音轨录制。录制文件、时长和大小均有硬上限；不开启音频，不请求 `RECORD_AUDIO`。
2. 录制生命周期维护快门前缓存；快门后约 1.5 秒停止，并用平台媒体变换裁出总长约 3 秒的 MP4。`ImageCapture` 同时得到原始 JPEG 封面；指导和手动快门继续工作。
3. `MotionPhotoAssembler` 先扫描 JPEG APP 段。没有 XMP 时新增官方 v1 XMP；旧包与本 App 生成的纯 Motion 包一致时删除旧段并只写一份新段；发现第三方/混合 Motion XMP、非 Motion/扩展 XMP 或 GainMap 时抛出可恢复错误，由保存状态机发布未改写的普通 JPEG。成功路径对齐小米 14 Pro 原生 `MVIMG` 样本：写入 `GCamera:MotionPhoto=1`、`GCamera:MotionPhotoVersion=1`、封面展示时间，在两个 `rdf:li` 中分别嵌套 Primary/MotionPhoto `Container:Item`，MotionPhoto 项包含真实 `Item:Length` 与 `Item:Padding=0`；再追加 MP4，确保没有尾随字节。
4. 只发布一个以 `MVIMG_` 开头且符合 `^([^\\s/\\\\][^/\\\\]*MP)\\.(JPG|jpg|JPEG|jpeg)$` 的 MediaStore JPEG；容器主 JPEG 即 SDR 原片，不再另存重复静态原片。Live 选择强制普通 Photo 和 `OUTPUT_FORMAT_JPEG`，Extensions 与 Ultra HDR 不参与四用例绑定。
5. 录制、裁剪、XMP、空间或发布失败均进入普通 JPEG fallback 阶段；状态机保证最终至多一个主文件。滤镜只生成可选静态兼容 SDR 副本，主 Motion Photo 保持原始封面颜色。
6. 进程恢复根据 journal 和 MediaStore URI 幂等完成或回退；清理所有临时 MP4/JPEG、超限录制和 pending 行。是否被 HyperOS 相册识别、播放、保留广色域或 Ultra HDR 均为真机 `NotRun`。
