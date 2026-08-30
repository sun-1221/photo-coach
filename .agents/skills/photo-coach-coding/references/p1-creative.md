# P1 创意层

改 `androidApp/src/main/java/com/photocoach/app/creative/`、参数建议、风格预览/导出、三张连拍、选优、轻编辑、分阶段保存或无声 Live 时读本文件。该层已单独批准，但不得进入 P-1 对照实验的 Go 证据。

## 领域模型

- `CreativeStyle` 固定原图、自然人像、柔光人像、清冷人像、清透旅行、森林清新、日落暖金、美食暖色、都市冷调、夜色霓虹、纪实黑白、高反差黑白，并用曝光、对比度、饱和度、色温、色调、褪色与强度生成同一套确定性 4×5 颜色矩阵。预览、导出和编辑必须消费同一参数源。
- `EditHistory` 保存上述七项调整的不可变 past/current/future 状态，支持 update / undo / redo / reset；“对比原图”只是 UI 状态，不进入历史，也不持有 Bitmap 或 Android `Context`。
- `BurstSession` 只允许 Idle → Capturing(1..3) → Complete/Failed。只有用户拍摄前明确开启才能开始；Capturing 拒绝重入。
- `PhotoQualityScorer` 在有上限的灰度采样上按固定权重计算清晰度和曝光合理性，并用拍摄序号稳定破平。分数只排序，UI 只给“更清晰/曝光更稳”等理由。
- `ParameterCoach` 按“无法正常成片 > 曝光/对焦 > 稳定性 > 机位/构图 > 输出偏好”输出最多三项。只建议当前能力可执行的 EV、对焦/测光、AE/AF 锁、普通/扩展模式、已验收焦段、靠稳/倒计时、捕获偏好、连拍、构图/画幅和保存策略；控件动作使用类型安全的 `ParameterAction`，物理动作同时给原因和动作正文。禁止 ISO、快门速度、光圈、白平衡或开尔文值。
- `CaptureIdentity` 生成不可复用 `captureId`，统一构造原片、Motion Photo、效果副本、连拍序号和配方名；显示名不依赖同毫秒唯一性。
- `SaveCoordinator` 是可序列化的阶段状态机：SpaceCheck → Original/MotionPack → OriginalPublish → Recipe → OptionalDerivative → Complete/PartialFailure。每阶段有 `captureId + 阶段` 幂等 key，只重试失败阶段，主文件成功后不回滚。
- `MotionPhotoAssembler` 负责验证 JPEG/MP4、注入 Camera/Container XMP、计算视频真实字节长度并保证视频紧密位于文件末尾；`MotionClipWindow` 计算快门前后各约 1.5 秒、总长约 3 秒的裁剪窗口。

## 原片、配方、派生副本与恢复

1. 每次 `ImageCapture` 先写 App cache 的 `captureId` 临时 JPEG；评分、处理、配方和 MediaStore I/O 不得占用实时分析执行器。保存前按原片、处理峰值、临时视频和可选副本预检空间。
2. 默认“原片优先”：先把未覆盖的原始 JPEG 发布到 MediaStore，再把所选风格与七项编辑参数原子保存到 App 私有目录。默认不生成效果 JPEG；只有用户明确“另存副本”或主动开启“原片+效果自动保存”时才生成。
3. 发布原片时写 `DATE_TAKEN`，Android 10+ 使用 `RELATIVE_PATH` 与 `IS_PENDING`。journal 在每个不可逆阶段前后落盘；发生部分成功时保留已成功项、只重试失败阶段。恢复按 `captureId + 阶段` 幂等判断，清理遗留 pending 行和临时文件，绝不重复发布。
4. 派生 JPEG 提供“完整质量”和“省空间兼容”两档，均明确为兼容 SDR，不能修改或重编码原片。安全 EXIF 只复制方向、尺寸、拍摄时间和相机厂商/型号；排除 GPS、缩略图、MakerNote 与未知标签。
5. 解码先读边界，完整质量按最多 12,000,000 像素计算采样，省空间档使用更低上限；处理中最多同时持有源、目标两个受限 ARGB Bitmap。写完立即释放，成功、失败和取消都清理临时文件。
6. `OutOfMemoryError` 是受控失败：回到原片并显示可恢复错误，不用更大内存重试。
7. Android 12 / API 31+ 可用 `View.setRenderEffect` + `ColorMatrixColorFilter` 做近似预览；低版本或设置失败显示原图预览。预览与 JPEG 因色彩管理可能略有差异，不能承诺像素一致。
8. 禁止 `CameraEffect`、LUT、小米私有 API、FACE_RETOUCH、磨皮/瘦脸或生成替换；效果不能卸载 `ImageAnalysis`。

## 三张连拍

- Activity 顺序调用现有单张捕获三次，不并行提交。
- 整批期间屏幕与音量键快门都不能创建第二批。
- 三张原片全部保留。完成后按总分、清晰度、曝光、序号稳定排序；用户仍可选任意一张。
- 单张失败立即暂停余下请求并保留已发布照片。只有用户明确重试成功才继续本批；否则终止，不能伪装为完整三张。
- 不自动删除、不显示美学分、不在后台连续拍；连拍、风格和编辑数据不得计入 P-1 Go。

## 拍后操作

- 拍后只对本 App 本次得到的 URI 执行打开、系统分享、收藏和移到系统回收站；不扫描整本相册，不新增定位权限。平台不支持收藏/回收站请求时给出可见说明，不伪装成功。

## 无声 Live（Android Motion Photo）

- Live 是默认关闭的显式开关，输出 Android Motion Photo Format 1.0，不命名或冒充 Apple Live Photo。绝不申请 `RECORD_AUDIO`、启用音轨或录制声音。
- 能力允许时绑定 `Preview + ImageAnalysis + ImageCapture + VideoCapture`，继续实时指导和手动快门。Live 强制普通 Photo，禁止 CameraX Extensions；四用例、编码器、空间或运行时不支持时显示原因并回退普通 JPEG，不能保留假 Live 状态。
- 开启后维护有严格时长、大小和清理上限的循环临时无声 MP4；快门后保留前后各约 1.5 秒，并用原始 JPEG 作为高分辨率封面。
- 主文件必须注入 `Camera:MotionPhoto=1`、`Camera:MotionPhotoVersion=1`、`Camera:MotionPhotoPresentationTimestampUs`，以及有且仅有 `Primary` / `MotionPhoto` 两项的 Container Directory；`MotionPhoto` 项长度等于真实 MP4 字节数，视频紧密追加在文件末尾，显示名符合 `…MP.JPG`。
- Motion Photo 主 JPEG 本身就是原片，只发布一个主文件。录制、裁剪、XMP、空间或发布任一阶段失败时，清理不完整容器和 pending 行并可靠发布普通 JPEG；journal 保证进程重启至多发布一个主文件。
- 风格和编辑只生成可选静态兼容 SDR 副本，主 Motion Photo 保持原始封面颜色。所有成功、失败、取消和恢复路径都清理超时/超量临时视频。

## 验证

- 领域逻辑、矩阵、编辑历史、状态机、journal 幂等、XMP 组装和资源上限：`androidApp/src/test/.../creative/*Test.kt`
- 解码真机边界：`androidApp/src/androidTest/.../creative/BoundedImageDecoderInstrumentedTest.kt`
- JVM 测试不能证明小米 14 Pro 真机颜色、预览/导出差异、连拍间隔与热量、HyperOS 分阶段保存/收藏/回收站、四用例绑定、视频裁剪或系统相册播放；没有对应记录时这些全部写 `NotRun`。
