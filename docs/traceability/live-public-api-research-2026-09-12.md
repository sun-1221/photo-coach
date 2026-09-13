# CameraX 1.6.1 Live 公开接线研究原文

两个已完成Reader终报原样归档，历史链接不作为当前导航。2026-09-13 用户已选择保留四用例＋双编码器的 C 方案，见当前架构决策；下面的待决/Blocked 是当时研究结论，保留原文，不代表当前仍等待批准。

## 01a0961b-33ba-7181-8183-80c109aa3ff1

~~~~text
**结论：在固定 CameraX 1.6.1、仅使用公开 API、保留现有 Recorder 架构这三个条件同时成立时，未找到可证明的 sensor→成片 PTS 桥接。O09/FR-31 仍不能关闭。** 但阻塞已具体化：可采用公开帧入口配合自有编码器解决，需要批准媒体架构调整，不必升级 CameraX。

**固定版本核查结果**

我直接解析了缓存 1.6.1 AAR 的接口及 `RestrictTo` 注解，并核对反汇编所用 class 与 AAR 内 class 字节一致。

| 路径 | 实际结论 |
|---|---|
| `Recorder` / `RecordingStats` | 没有公开首个入片编码帧的 sensor 时间或原始编码 PTS。`Recorder` 为 final；写视频时将 PTS 减去 `mFirstRecordingVideoDataTimeUs`，文件中的零点不能反推原 sensor 零点。 |
| 自定义 `VideoOutput` | 接口及单参数 `onSurfaceRequested` 公开，但 `getMediaSpec`、携带 `Timebase` 的重载、流状态等为 `RestrictTo(LIBRARY)`。**默认 MediaSpec 为 null，而 1.6.1 的绑定路径调用 `getMediaSpecOrThrow` 并抛出 `MediaSpec can't be null`。** 因此仅实现公开方法无法构成当前版本可用的替代输出；委托这些受限方法也不满足本任务边界。 |
| Recorder 自定义 muxer | `Recorder.Builder.setMuxerFactory` 为 `RestrictTo(LIBRARY)`，不能作为公开扩展方案。 |
| `CameraEffect` / `SurfaceProcessor` | 公开构造与输入、输出 Surface 回调确实存在，可以处理帧；但它们只证明输入/提交帧，不证明 Recorder 最后采用哪一帧作为成片起点。加一个透明 Effect 后把“首个提交帧”当首个入片帧仍不成立。 |
| Camera2 回调 | `setSessionCaptureCallback` 是公开实验接口，可取得 capture result，但不能证明某帧已被编码并写入 Recorder 文件。 |

公开文档也区分了 [VideoOutput 的扩展入口](https://developer.android.com/reference/androidx/camera/video/VideoOutput) 与 [SurfaceProcessor 的 Surface 生命周期](https://developer.android.com/reference/androidx/camera/core/SurfaceProcessor)。上表的固定版本限制来自本地 AAR，未把新版官网能力当作 1.6.1 能力。

另外，不能统一假设所有 Surface 时间都等于 sensor 时间：Android 对编码器输出 Surface 与 SurfaceTexture 的默认时间基准有不同规定；SurfaceTexture 默认保留 sensor 时间，但中间处理仍须核对。[OutputConfiguration 时间基准](https://developer.android.com/reference/android/hardware/camera2/params/OutputConfiguration#TIMESTAMP_BASE_DEFAULT)

**可评审的实现选项**

| 选项 | 具体实现与代价 |
|---|---|
| **A：保留 CameraX 三用例，Preview Effect 分流至自有编码器，建议优先评审** | 使用公开 `CameraEffect(PREVIEW, …, SurfaceProcessor, …)`；输入 SurfaceTexture 同时渲染到正常预览和自有 MediaCodec Surface。应用记录每个输入帧时间、提交 PTS、实际输出 PTS与成功写入样本。保留独立 `ImageCapture` 原 JPEG、分析和保存恢复。增加 EGL、编码器、muxer、资源及故障隔离责任；需移除“Live 必须增加 Recorder 四用例”的架构限定。 |
| B：从现有 ImageAnalysis 分流 YUV 至自有编码器 | 使用 `ImageInfo.timestamp`，在分析入口有界复制视频所需帧，再交给独立 MediaCodec 队列；不等待编码器、不延长 ImageProxy 所有权。公开 API 路径清晰，但与 ML Kit 共享分析输入，复制、分辨率及掉帧成本更明显，HD/SD 连续性需单独验证。 |

两者都保持固定依赖和手动 JPEG 主路径；都属于**替换 Live 媒体管线**，不是给现有 Recorder 补一个回调。不建议为读取锚点引入受限接口、反射或维护 SDK 补丁。

A 的建议文件边界：

- [LiveRecordingController.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/LiveRecordingController.kt)：替换录制后端，保留代次、上限和 finalize 合同。
- 新增 `LiveSurfaceProcessor`：输入时间、渲染变换、Surface 生命周期与有界分流。
- 新增 `SilentVideoEncoder`：MediaCodec 输出、关键帧、EOS、无音轨 mux。
- 新增 `MotionFrameTimeline`：输入帧—编码样本—文件时间对应关系。
- [CameraBinder.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraBinder.kt)、[CameraLocks.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraLocks.kt)：会话能力、JPEG 身份与 sensor 时间关联、回退。
- [MotionTimestampMapping.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/MotionTimestampMapping.kt)、[Mp4Clipper.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/Mp4Clipper.kt)：消费可验证映射及实际裁剪起点；原 JPEG/XMP/发布互斥合同保持。

**应冻结的时间映射合同**

1. 每条记录包含会话代次、camera 身份、帧 sensor 时间、输入 PTS、输出 PTS和实际 mux PTS；跨会话不能混用。
2. 只有已确认时间基准且能关联的输入帧才建立映射。JPEG 时间必须属于对应捕获；未知、歧义、重绑或不连续时回退 JPEG。
3. 应用显式提交帧 PTS，并从真实编码输出确认对应样本。`BufferInfo.presentationTimeUs` 来源于对应输入时间；不能用回调到达时刻代替。[MediaCodec.BufferInfo](https://developer.android.com/reference/android/media/MediaCodec.BufferInfo#presentationTimeUs)、[EGL presentation time](https://registry.khronos.org/EGL/extensions/ANDROID/EGL_ANDROID_presentation_time.txt)
4. 设输入 sensor 原点为 `S0`，输入 PTS 为 `(S−S0)/1000`。若最终文件以实际首个保留样本 `Q0` 归零，则封面时间为 `(Scapture−S0)/1000−Q0`；再减去裁剪**实际写入的起点**。不能用请求裁剪起点代替前一个同步帧起点。
5. 检查真实入片样本覆盖快门前后窗口，处理首帧丢弃、掉帧、关键帧等待、重复/乱序 PTS及 EOS。缺失窗口不能通过钳制时间值伪装成功。
6. 数值换算误差、最近视频帧间距、JPEG 曝光语义和产品允许偏差分别记录。**产品时长及封面容差仍需冻结，不能自行填一个数作为验收标准。**

**无需真机即可推进的验证**

| 层次 | 必须验证的内容 |
|---|---|
| JVM 时间合同 | 非零起点、首帧丢弃、变帧率、重复/乱序、代次切换、边界、溢出、未知时间基准及 JPEG 落在视频帧之间。 |
| 模拟器真实媒体组件 | 生成带已知帧编号与非均匀时间间隔的图案，经真实 EGL/MediaCodec/MediaMuxer 编码，再用 MediaExtractor/解码器核对每帧与 PTS；故意改变回调延迟，结果必须不变。 |
| 裁剪与封装 | 非同步帧位置裁剪、实际起点补偿、可解码完整 MP4、无音轨、封面时间范围、原 JPEG 压缩数据保持、XMP Length/末尾布局。 |
| 故障与恢复 | 编码失败、零样本、无关键帧、磁盘不足、关闭/重绑、旧 finalize、超限及进程中断；最终至多一个主资产，恢复源不丢失。 |
| 生产接线 | 调用真实后端适配器和时间记录器，不能只给 `MotionTimestampMapping` 手工传入一个正确 anchor。 |

当前 `MotionPhotoFileInstrumentedTest` 使用的是 **12 字节 ftyp 片段**，能覆盖封装字节和 JPEG 解码，不能证明视频可播放、真实裁剪或编码时间映射。现有映射 JVM 用例也仅证明“给定正确锚点后的算法”。这些是需要补齐的非真机证据，不是已经通过的验收。

**交总控的准确待决点**：是否批准 A 的 Live 编码架构替换，以及何时冻结媒体时长/封面容差。若不调整现有 Recorder 架构，可以完成回退、封装及模拟媒体测试，但正常 Live 生产映射仍须保留 **Blocked**。模拟器通过后，目标机 sensor/编码器行为、连续性、热与内存、HyperOS 识别播放仍单列真机验收。

本次 `Changed: none`；仅检查源码、固定 AAR/注解/字节码与官方资料，未构建、运行测试或启动模拟器。`javap` 输出已取得，但存在缓存关闭时的 AccessDenied 非零退出；未将其记作测试成功，关键接口注解另由直接解析 AAR 验证。
~~~~

## 01a09622-e404-7e00-bc1e-3cd5fd19c7e9

~~~~text
**该候选在 CameraX 1.6.1 的公开 API 层面可接线，但它是“双编码媒体管线”，不是修复 Recorder 自身的时间锚点。可列为架构选项，不宜默认采用。**

本次固定版本字节码确认了一个官网说明遗漏的细节：

- `CameraEffect.SURFACE_PROCESSOR_TARGETS` 实际包含 **1、2、3、7**，其中 **2 就是单独 `VIDEO_CAPTURE`**。
- `UseCaseGroup.Builder` 接受该目标；`VideoCapture.getSupportedEffectTargets()` 返回包含 2 的集合。
- 四参数 `CameraEffect` 构造、`SurfaceProcessor` 输入/输出回调、`SurfaceOutput.getSurface()` 和变换接口均为公开接口。

因此，可以在保留 `VideoCapture<Recorder>` 的同时，增加仅作用于视频的 Effect；无需调用受限的 `VideoOutput.getMediaSpec`、muxer factory 或 Timebase 重载。此结论以缓存 **1.6.1 AAR** 为准，不受官网目标组合列举不完整的影响。

具体数据流可以是：

```text
CameraX 视频输入 → SurfaceTexture
                    ├─ 正常变换、转发 → Recorder → 有界临时文件，最终丢弃
                    └─ 有界分流 → 自有 MediaCodec → MediaMuxer → 最终 Live 视频
Preview / ImageAnalysis / ImageCapture 保持各自路径
```

**时间锚点可以在自有编码支路建立，但必须满足以下条件：**

1. 记录每次实际消费的 SurfaceTexture 帧时间、会话代次与提交 PTS，并从自有编码器输出确认样本。用实际入片样本建立映射，不能把提交成功当成编码成功。
2. 确认输入时间与同一相机的 `SENSOR_TIMESTAMP` 对应；新增视频帧 capture-result 关联，不能仅凭数值接近推断时钟偏移。未知、换相机、重绑或中间处理导致无法关联时回退。
3. 自有编码支路独立决定首个有效样本、文件时间原点、时长和裁剪覆盖。**不能继续使用 Recorder 的 Start 或 RecordingStats 计算自有视频的缓存充分性和封面位置。**
4. 将 JPEG 时间映射到自有文件，并按裁剪实际保留的同步帧起点再次换算。Recorder 的归零行为不再影响这个映射。

平台区分不同 Surface 的时间基准；SurfaceTexture 的默认 sensor 时间语义及编码器 Surface 的时间基准不能混用。[OutputConfiguration](https://developer.android.com/reference/android/hardware/camera2/params/OutputConfiguration#TIMESTAMP_BASE_DEFAULT) 自有编码输出 PTS可与对应输入时间建立关系，但仍需执行真实媒体组件验证。[MediaCodec.BufferInfo](https://developer.android.com/reference/android/media/MediaCodec.BufferInfo#presentationTimeUs)

**这条路线的实际代价**

- 同时运行两套编码器、muxer 和临时文件写入，增加 GPU 带宽、编码器实例、内存、功耗、热量及存储峰值；四用例协商成功并不证明双编码资源足够。
- 任一编码支路的阻塞不能同步传回主线程或分析线程。Effect 内串行执行两个可能阻塞的 `eglSwapBuffers` 也不能视为充分隔离；需有界缓冲、明确丢帧和超载回退策略。
- Recorder 与自有编码器必须共享会话代次，但分别管理结束事件。旧支路结束不能清理新支路；退出、轮换、部分启动失败和超限都须结束并清理两套资源。
- 预览 Effect 已有美颜用途。视频专用 Effect 与其目标不重叠，但当前 [CameraSessionCoordinator.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraSessionCoordinator.kt) 只接收一个 Effect，且预检未加入 Effect，需要改为使用一致的完整候选配置进行预检和绑定。
- 自有编码器或时间关联失败时，应直接恢复普通 JPEG 路径；不能为了等待第二编码器而延迟快门。

**与三用例分析帧编码的取舍**

| 项目 | 四用例＋视频 Effect 双编码 | 三用例＋分析帧编码 |
|---|---|---|
| 视频帧来源 | 独立视频支路，不依赖 ML Kit 分析完成 | 与分析共享输入，容易受 latest-only 丢帧影响 |
| 编码与写盘 | 两套，资源成本明显更高 | 一套，但增加 YUV 复制、转换成本 |
| 时间记录 | 需验证 SurfaceTexture→sensor，再关联自有输出 | `ImageInfo.timestamp` 直接随分析帧进入自有队列 |
| 指导风险 | 不直接占用分析输入，但竞争系统 GPU/编码资源 | 编码复制不得拖延分析或 ImageProxy 释放 |
| 架构改动 | 保留四用例，Recorder 从最终视频生产者变为辅助支路 | 移除 Live 四用例要求，改用分析输入生产视频 |
| 主要验收难点 | 双编码资源、热量、双生命周期及故障隔离 | 视频连续性、HD/SD 输入能力、分析性能 |

**建议交用户评审的决定是：是否愿意为保留独立视频输入和四用例协商承担双编码成本。** 若选择此方案，先完成模拟器上的真实双编码、时间往返和故障隔离验证，再决定是否进入目标机验证；不能仅因 API 可接线就采用为默认生产实现。

先前提出的三用例 Preview Effect＋单自有编码器仍是第三个选项：避免双编码，也不依赖分析帧，但需要处理预览渲染及已有美颜 Effect 的组合。

本次仅确认 **API 可行性与设计边界**，没有运行双编码或验证设备资源。正常 Live 的 O09 阻塞尚未解除，媒体容差也未被改写；全程只读。
~~~~


## 01a09669-acbc-7740-bdb8-7ea30546dfa9 / msg_071c4d1c622436d1016aa57ca115b887d0bab1a2bee5ac4aa8

~~~~text
**四用例＋双编码器方案具备固定 CameraX 1.6.1 的 API 基础，但上线条件必须包含时基证据、实际编码样本账本和有界资源释放。** 已分批将具体建议发给唯一 Writer；本轮仅做设计审查。

| 合同 | 最小要求 |
|---|---|
| 相机图 | 保持 `Preview + ImageAnalysis + ImageCapture + VideoCapture<Recorder>`。使用公开 `CameraEffect(VIDEO_CAPTURE, executor, SurfaceProcessor, errorListener)`，向 Recorder 的 `SurfaceOutput` 和自有 MediaCodec 输入 Surface 双路绘制。最终 Motion 文件来自自有编码器。 |
| Effect 组合 | 当前 [CameraSessionCoordinator.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraSessionCoordinator.kt:29) 的支持查询未包含 effect，绑定只接收单 effect。应让预检和实际绑定使用相同集合：PREVIEW 美颜与 VIDEO_CAPTURE Live 目标不重叠；失败候选完整释放。 |
| 公开 API 边界 | 固定 AAR 的 SurfaceProcessor 目标集合为 **1、2、3、7**，包含 VIDEO_CAPTURE-only。无需自定义依赖受限接口的 VideoOutput、替换 Recorder 内部 muxer或调用私有 API。内部字节码仅用于审查依据。 |

**时基证据比 `realtime=true/false` 更具体。**

本轮固定 AAR 检查确认：

- `CameraGraphConfigProvider` 创建输出配置时 timestampBase 使用默认 null；camera2-pipe 仅在非 null 时调用平台 `setTimestampBase`。所检普通图没有因 VideoCapture 用途强制指定 MONOTONIC。
- `DefaultSurfaceProcessor` 将 `SurfaceTexture.getTimestamp()` 直接传给 `OpenGlRenderer.render`，后者用于 `eglPresentationTimeANDROID`。
- 平台 API 33+ 的 DEFAULT 对 SurfaceTexture 保留 SENSOR 域，对编码器 Surface 则通常选择 MONOTONIC，不能混为一谈。[平台合同](https://developer.android.com/reference/android/hardware/camera2/params/OutputConfiguration#TIMESTAMP_BASE_DEFAULT)

运行时仍须建立以下证据：

1. 记录 camera/session generation、实际输入 Surface 来源和时间戳模式；重绑、物理相机切换或不明变换使旧证明失效。
2. 对视频输入时间戳与该相机实例的 `TotalCaptureResult.SENSOR_TIMESTAMP` 做**精确帧关联**，允许回调乱序和丢帧，但不允许“最近时间”“差值较小”或回调到达时间充当关联。
3. JPEG 时间戳必须属于当前 captureId 的实际曝光结果；多曝光、迟到结果或归属不明时保持 Unknown。
4. 若采用 readout 时间，必须另外证明与曝光开始时间的关系，不能直接代换。

UNKNOWN 不允许与 elapsedRealtime 或其他子系统比较，却允许同一相机设备的 SENSOR 时间戳相互比较。因此不能仅因 UNKNOWN 永久否定同域映射，也不能仅凭数值接近批准它。[SENSOR_TIMESTAMP 合同](https://developer.android.com/reference/android/hardware/camera2/CaptureResult#SENSOR_TIMESTAMP)

**真正首样本必须从最终保留的编码数据确定。**

建议账本包含：

`generation → 输入 sensorNs → EGL 输入 presentationNs → 编码输出 PTS/flags → mux 写入 → 裁剪后保留样本`

- 排除 codec-config 和空输出；空 EOS 不提供有效时间戳。编码输出 PTS 源于对应输入，但必须检查实际输出。[BufferInfo 合同](https://developer.android.com/reference/android/media/MediaCodec.BufferInfo#presentationTimeUs)
- 首输入、首次输出回调、Recorder.Start 均不能替代最终首样本。
- 剪辑必须从可解码关键帧开始，再按实际保留首样本重算时间原点及 XMP 快门位置。重复、乱序、无法关联、缺关键帧或快门落在有效范围外时回退 JPEG。
- 使用编码输出和 Extractor 结果核验文件，不能仅凭配置成功或录制 duration 宣布映射成功。

**双编码器压力和生命周期合同：**

- main、analysis、JPEG 保存执行器不得同步等待 GL swap、codec drain 或 mux。
- 受限纹理池、每输出有界队列和明确 fence 所有权；不能跨线程复用会被 `updateTexImage` 替换的裸 OES 纹理。
- 单 GL 线程串行双 swap 仍有背压风险，独立线程也无法保证硬件永不阻塞。须有独立超时/进度监测，在队列、编码器或热压力异常时停止 Live，保留分析和普通快门。
- 配额覆盖 **Recorder 临时文件＋自有编码文件＋裁剪/封装副本＋在途纹理和样本**，不能只套用现有单录制文件上限。
- 所有回调携带冻结的 session/capture/codec generation；旧 Finalize 只能清理旧资源，不能发布到新捕获。
- `SurfaceOutput` 先停止写入、释放自己的 EGL window，再 `close()` 确认；不能主动释放 CameraX 拥有的底层 Surface。[输出释放合同](https://developer.android.com/reference/androidx/camera/core/SurfaceOutput#close())
- 输入 Surface 等 `provideSurface` 完成回调后释放，GL 执行器需存活到相关确认完成。[输入所有权合同](https://developer.android.com/reference/androidx/camera/core/SurfaceRequest)
- 自有编码结束经过 EOS/drain、mux 收尾和文件验证。异常、时基未证实或资源耗尽均保存**同 capture 的 JPEG**，不等待 Recorder Finalize 才允许快门。

| 测试闭环 | 可证明的范围 |
|---|---|
| 正向合成生产 backend | EGL 生成带编号/颜色的确定帧，经生产 SurfaceTexture、双路绘制及自有 codec/mux，再 Extractor、实际解码、裁剪和封装。覆盖非零起点、输入丢帧、首关键帧改变、旋转/镜像及 XMP 定位。 |
| 故障与生命周期 | 覆盖队列满、编码失败、配额满、输出关闭先到、输入释放迟到、重绑、旧 Finalize、重复停止和 EOS 超时；核对无跨代发布、无资源增长、JPEG 与分析继续。 |
| 当前 UNKNOWN AVD 真实绑定 | 验证四用例及实际 effect 集合绑定、分析交付和快门；无法取得充分同域证据时，验证明确降级及普通 JPEG。不能把合成时钟证明转成真实相机映射 Pass。 |
| 目标机 | 双硬件编码器持续压力、热量、实际传感器关联和相册 Motion 播放仍需独立验证；本轮 NotRun。 |

质量 Fresh 门禁继续独立保持，不能因 Live 获得同 SENSOR 域证据而修改其 elapsedRealtime 比较结论。旧的 12 字节 `ftyp` 测试只能证明容器拼装边界，不能证明视频可播放。

本轮固定 AAR SHA-256：

- camera-core：`a36f1c323851b51215ba476640e1eb4153882b0c368c0376e9fb905e689df84a`
- camera-camera2：`10d8f0102468d10a311fdf73695fc29a6a69a738bb0445fbcffd9b9c037cb36b`
- camera-camera2-pipe：`4254e6f684678a55fafa67cc12197c266250fabb59880ec6a72b521165cf2146`
- camera-video：`d0c2718b722c7ef9fafe1fcfaa352d4da0a1c77d0b175e43739b363996fa1fa1`

**Changed：none。** 仅读取生产源码、固定 AAR/字节码及官方原始资料；未升级依赖、修改文件、构建或操作设备。以上是实施与测试合同，尚不构成新 backend 已交付或验收通过。
~~~~
