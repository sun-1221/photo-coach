# 自然上镜 v1 架构

产品依据：[规格](../requirements/p1-beauty.md)。沿用 CameraX 1.6.1 和 ML Kit face 16.1.7，不增加实时检测器、MediaPipe/Media3/SDK。

1. CoachAnalyzer 复用已有 landmarks，发布纯数值 BeautyFaceFrame 和 sensorToAnalysis 变换，AtomicReference 最新快照。没有 Face 对象、图片或身份日志。ML Kit 回调以 `result.timestamp` 从容量 3 的元数据表取对应分析帧（不保留 ImageProxy），避免 close 后下一帧覆盖单个 lastFrame 的错配。分析旋转坐标逆变换回 sensor 后映射输出像素，Android/GL Y 轴显式转换；输入采样 crop/rotation/mirror 交给 SurfaceOutput.updateTransformMatrix，蒙版独立用输出 `sensorToBufferTransform`，不复用 fitCenter 叠线映射。
2. 独立 EGL/GLES 线程管理 PREVIEW-only SurfaceProcessor。OES→RGBA→1/4 尺寸统计→可分离盒滤波→guided coefficients→均值 coefficients→细节保留与蒙版融合。现有 PreviewView 全局颜色矩阵在美颜之后，Compose 指引层不处理。不使用不支持的 PREVIEW|IMAGE_CAPTURE。
3. 输入仅在 SurfaceRequest 结果后回收，输出按关闭事件释放，close 后忽略迟到帧，GL 错误经主线程可见回退标准三用例。OFF 不绑效果，热降级保留透传。
   分析关闭时，`ClosingAnalyzerExecutor` 允许迟到的 ML Kit 完成/清理回调执行；`CoachAnalyzer.closed` 禁止这些结果进入 UI。不能直接把完成回调提交到已 shutdown 的相机分析线程，否则退出时可能发生 RejectedExecutionException。
4. 可选 BeautyStillProcessor 在有界拍摄图重新检测；原片先发布。美颜先于既有颜色矩阵/保守强度检查；CPU 导出采用低分辨率 guided coefficients 和逐行融合，保留最多两个全尺寸 Bitmap 上限，不占 analysis 执行器。配方/journal 只保存预设/engineVersion；原图、captureId、分阶段幂等、SDR 与安全 EXIF 不变。

资源与失败：预览最多 8,294,400 像素，1 个全尺寸 RGBA8 和 6 个宽高各 1/4 的 RGBA16F FBO；不支持 ES3/半浮点渲染或尺寸超限时显式关闭效果并重绑普通预览。输入输出的系统缓冲及驱动开销另计。热策略暂停时透传，拍摄中通过既有延迟重绑门禁保住正在保存的原片。输入资源须等 CameraX 结果回调才释放；上下文丢失也须尽力释放所有 Surface 并关闭线程。

CPU 与 GPU 的低分辨率采样、浮点精度及 JPEG 编码不同，只共享算法/预设/几何约定，不承诺逐像素一致。关键点椭圆不是皮肤/遮挡分割：眼镜、胡须、发丝、手挡脸与快速运动需真实样张验证。

API 依据：[CameraEffect](https://developer.android.com/reference/androidx/camera/core/CameraEffect)、[SurfaceProcessor](https://developer.android.com/reference/androidx/camera/core/SurfaceProcessor)、[SurfaceOutput](https://developer.android.com/reference/androidx/camera/core/SurfaceOutput)、[Fast Guided Filter](https://arxiv.org/abs/1505.00996)。接口按 pinned release 编译检查，设备效果/性能 NotRun，不承诺像素完全一致。
