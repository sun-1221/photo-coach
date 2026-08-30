# 感知信号

改 `androidApp/.../analysis/` 的人脸、姿态、帧统计、镜头遮挡、叠线坐标或隐私行为时读本文件。

## 检测器与帧生命周期

- 当前依赖以版本目录为准：bundled ML Kit Face 和 beta Pose。依赖升级必须单独评估模型行为与小米 14 Pro 性能，不在普通修复中顺带升级。
- 实时 Face 使用 `PERFORMANCE_MODE_FAST`，关闭不需要的 landmark、contour、classification。Contour 只覆盖最显著人脸，不要和 tracking 同时开。
- Pose 使用 `STREAM_MODE`。P-1 只需要单人骨骼信号，不建立多人姿势评分。
- 优先用一个 `MlKitAnalyzer` 同时运行 Face + Pose，并用 `COORDINATE_SYSTEM_VIEW_REFERENCED` 得到 PreviewView 坐标，避免两套 `ImageProxy` 生命周期。
- `ImageAnalysis` 保持 `STRATEGY_KEEP_ONLY_LATEST`。自写 analyzer 时，只有所有异步检测完成后才关闭 `ImageProxy`；不要关闭 `Media.Image`。
- 帧亮度/方差等同步统计必须有上限，不能延迟把帧交给 ML Kit。口令选择和创意处理不得运行在分析执行器。

## 信号语义

口令引擎消费项目定义的有限信号，不消费检测器对象或身份信息：

| 信号 | 含义 | 禁止推断 |
| --- | --- | --- |
| `faceCount` | 检出的脸数量 | 姓名、身份或真实人数承诺 |
| `faceRatio` | 最大脸占画面比例 | 焦距毫米或审美分 |
| `faceDarkerThanScene` | 脸明显暗于背景 | ISO 或绝对曝光参数 |
| `tiltDegrees` | 水平倾角 | 泛化“构图质量”分 |
| `coarseScene` | 项目已有粗场景 | 未批准的细分类 |
| `poseAvailable` | 单人骨骼足够可靠 | 多人姿势或人体身份 |
| `focusOnFace` | 当前对焦/测光是否落脸 | 人脸识别 |
| `lensObscured` | 连续多帧呈极暗且低方差 | 镜头盖、手指、暗室或污渍的确定诊断 |

镜头遮挡只在连续多帧同时满足条件后置位，恢复也需连续清晰帧。用户文案用“镜头可能被挡住或需要擦一下”，不得给出确定原因。

## 人数与姿势边界

- P-1 验收目标是一张脸。
- `faceCount != 1` 时不得进入 P-1 三个单人场景或单人姿势匹配；两张脸以上只可给水平、切边、曝光等与身份无关的安全回退。
- 无脸、遮挡或骨骼不完整时不猜姿势、不显示匹配分。
- 检测脸不等于识别脸。禁止 embedding、比对、底库、身份字段、年龄/性别/外貌分类和人脸坐标事件日志。

## 数据边界

- P-1 所有分析在端侧完成，只申请 `CAMERA`。
- 研究事件不记录预览帧、照片、人脸框、关键点或身份。
- P0 “再讲细”是单帧、按需、单独同意的独立路径；拒绝或撤回后不得后台重试。
- 新增第三方 SDK、模型下载或网络行为前，必须更新实际数据流、需求披露与上架数据安全核对，不能仅修改隐私文案。
