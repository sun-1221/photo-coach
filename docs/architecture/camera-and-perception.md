# 相机与感知专题

- 状态：当前架构规范附件
- 权威入口：[../architecture.md](../architecture.md)
- 产品约束：[产品需求总纲](../requirement.md)
- 原始位置：重构前架构第 4.1、4.2 节
- 迁移方式：Moved；绑定组合、视口、Extensions、感知与隐私边界完整保留

---

### 4.1 相机管线

- 每个 CameraBinder 只解绑本实例登记的 use cases（包括部分失败的绑定），不调用进程级 `unbindAll`；旧页面释放不能解绑新页面或其他相机会话。实际前后台/重绑恢复仍需目标机验收。

- CameraX 普通主路径：`Preview` + `ImageAnalysis` + `ImageCapture`
- 显式 Live 且能力允许时：`Preview` + `ImageAnalysis` + `ImageCapture` + 无音轨 `VideoCapture<Recorder>`；Live 一律使用标准 Photo 选择器，不启用 Extensions。2026-09-13 用户选择保留四用例＋双编码器：VIDEO_CAPTURE-only 公开 Effect 有界分发给 Recorder 与自有编码器，最终视频只取自有支路。先用 `SessionConfig.Builder` 构造同一 ViewPort、四用例与实际启用的 Effect 配置，并以 `CameraInfo.isSessionConfigSupported` 预检；按 HD、SD 顺序尝试，实际绑定仍失败才回普通三用例并向 UI 返回明确降级原因。Live 与美颜仍互斥，时间账本与双路资源合同见创意存储专题。
- `PreviewView` 使用 `FILL_CENTER` 覆盖长屏取景区；竖屏操作区叠在预览底部，避免 4:3 / 16:9 surface 居中产生额外黑带。三用例放入带 `PreviewView.viewPort` 的 `UseCaseGroup`，使预览可见裁切、ML Kit view-referenced 坐标和捕获裁切使用同一视口。
- 分析策略：`STRATEGY_KEEP_ONLY_LATEST`，分析完必须 `ImageProxy.close()`
- `Preview`、`ImageAnalysis`、`ImageCapture` 使用同一 4:3 或 16:9 `AspectRatioStrategy`；分析分辨率只作为性能偏好，不能用 UI 裁切伪造画幅。
- 成片：`ExtensionsManager` 查询后，扩展可用且 `isImageAnalysisSupported` 为真，才开 `NIGHT` / `BOKEH` / `HDR` 并同时绑分析；扩展不可用，或扩展可用但分析绑不上，则标准 Preview + Analysis + Capture，口令照给。不要为成片卸掉分析（需求：保口令、弃扩展成片）
- 不要用 `ExtensionSessionConfig` 当默认绑定（该会话不支持 ImageAnalysis）
- P-1/P0 和原有全局风格不开 `FACE_RETOUCH` 或自研 LUT/CameraEffect 调色层；需求第 4.6 节另行批准的[自然上镜](beauty.md)仅标准 Photo 添加 PREVIEW-only 局部纹理处理。
- 第 12.2 节 P1 创意风格不进入 CameraX use case：原片仍由上述捕获管线生成，取景预览仅在支持时对 `PreviewView` 使用共享颜色矩阵的显示效果，导出在捕获后的受限内存处理器中另存兼容 SDR 副本；任何效果失败都回退原片，不能卸载或替换 `ImageAnalysis`
- 能力状态在每次镜头/画幅/模式重绑后重建：`ZoomState` 提供连续缩放范围，`ExposureState` 提供 EV 索引范围与步长，Extensions 双查询提供可选模式。旧设置越界时夹紧，旧模式不可用时回到“自动/普通”，不沿用陈旧能力。
- AF/AE 分别建立能力、请求与确认状态。`FocusMeteringAction` 的 AF+AE 指定测光区域，`disableAutoCancel` 只关闭自动取消；不能由这两项推导 AE 已锁定。当前固定 CameraX 1.6.1，官网 `setLockingMode` 标为 1.7.0-alpha03，不能未经升级决策直接采用。当前版本若以标准 Camera2 补足 AE 锁，须检查支持能力与真实 CaptureResult；未核验时标未知/不支持，不显示双锁成功。解除恢复对应自动控制；任何 use case 重绑使原锁定确认失效。
- “画质优先”映射 `CAPTURE_MODE_MAXIMIZE_QUALITY`，“速度优先”映射 `CAPTURE_MODE_MINIMIZE_LATENCY`；两者都不改变三用例同绑和手动快门优先。
- `ImageCapture` 显式使用 `OUTPUT_FORMAT_JPEG`。当前 Live 封面只接受普通 SDR JPEG，不请求 `OUTPUT_FORMAT_JPEG_ULTRA_HDR`，避免 v1 两项 Container Directory 与 GainMap 条目冲突。
- 倒计时只延迟一次用户发起的捕获，支持关闭/3 秒/10 秒；再次按屏幕或音量键快门取消，不进入自动连拍。
- 这是第三方走厂商计算摄影的正路，对应需求「成片优先走系统计算摄影，口令分析不能停」

### 4.2 感知

- ML Kit Face：小米 14 Pro 单人主路径使用精确模式、5% 最小脸尺寸偏好、睁眼分类与帧间 tracking；仍保留多人计数，用于「两张脸以上不要套单人剪影」。不同时启用 contour，避免实时管线负担失控。分类模式仍由 ML Kit 同时返回微笑概率，但业务层明确忽略该值，不从低微笑推断表情。
- ML Kit Pose：单人骨骼，STREAM_MODE 跟踪最显著人物，不是多人检测器；Face 检测漏检但 Pose 新鲜有效与实际遮脸是两个条件。实际遮挡导致 Pose 无效时进入恢复，不能用旧骨骼维持可拍确认。
- Face 与 Pose 独立生成可靠性：Face 未命中但 Pose 上半身可靠且未检测到多张脸时，允许保守单人姿势提示；两者都不可用时进入可恢复的“请露出脸，或靠近一点”状态，不得落入“可以拍了”。
- 另算：水平倾角、脸占比、脸框横向是否居中、特写脸框是否明显落在下半部、粗场景，以及一张脸时的左右转头和持续闭眼。每帧从 Y 平面按固定步长保留有上限的亮度网格，按 `rotationDegrees` 映射到 ML Kit 原始坐标；逆光只比较脸框中心采样与扩张脸框外背景采样，样本不足保持未知，不再根据脸框纵向位置猜亮度。
- Pose 只输出有直接可见证据的上半身方向、耸肩和双手挡住身体等信号；单帧 2D 髋部/脚踝中心不再输出“重心均匀”，也不自动选择“重心换到后腿”。两张脸以上的规则路径只允许切边、水平和严重曝光提示。
- 不做人脸比对，不存底库，不识别「这是谁」
- 镜头检查只在连续多帧同时满足极暗、低方差时置位；单帧不触发，恢复也需连续清晰帧。该信号只能说明“可能遮挡或弄脏”，不能区分镜头盖、手指、暗室或污渍，文案必须保守。

2026-09-08：曝光请求经 ExposureController 量化、夹紧并分配代次；CameraControl Future 成功后更新确认值，失败回传可见错误。重绑/释放使旧回调失效，场景初始化不提交 EV 请求。高光提示使用 Y≥245 的采样占比（暂定 ≥2%），配合脸部亮度可用性与暗脸抑制；此为待真机校准的防误报初值，不代表天空检测或曝光合格标准。

2026-09-12 合同补充：分析结果携带当前会话及原始捕获时间，重复/乱序/旧会话结果不得累加稳定帧数；坐标、占比度量、像素尺寸和有效期按交互6.2校准。锁定、画质/速度优先更名及新增分支尚未做代码符合性或目标机验证。API依据见[逐项审核来源](../research/requirements-line-audit-2026-09-12.md#6-新增联网来源)。
