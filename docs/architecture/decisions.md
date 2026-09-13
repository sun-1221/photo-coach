# 架构决策记录与历史依据

- 状态：当前架构规范附件
- 权威入口：[../architecture.md](../architecture.md)
- 迁移方式：Consolidated；旧方案对比、iOS 方向和明确不选项完整保留

## ADR 索引

| ADR | 状态 | 决定 | 边界/后果 |
| --- | --- | --- | --- |
| ADR-001 原生取景器 | Accepted | 第一版 Android 取景器采用 Kotlin + Compose + CameraX/ML Kit | P-1/P0/P1 只验收小米 14 Pro；不等于扩展其他 Android |
| ADR-002 平台无关口令 | Accepted | 场景 JSON、信号和口令模型保持平台无关 | iOS 立项后新写原生取景器，不预先承诺 KMP |
| ADR-003 可选讲解服务 | Accepted / GateLocked P0 | ExplainApi 使用 C# / ASP.NET Core，按需单帧、供应商可替换 | 主路径不得依赖网络，P-1 不启用 |
| ADR-004 标准能力与回退 | Accepted | 使用 CameraX/Camera2/MediaStore 标准能力探测 | 不使用小米私有 API；Extensions/Live/效果失败须回退 |
| ADR-005 非破坏 P1 创意 | Accepted / ApprovedSeparate | 原片优先、确定性矩阵、captureId 分阶段保存、无声 Motion Photo | 不覆盖原片、不倒灌 P-1 指标、不宣称未做的真机通过 |
| ADR-006 暂不选择项 | Accepted | 不用 MAUI/Flutter CameraView 做主取景器，不自研 ISP/RAW，不为纯 C# 牺牲 CameraX 可控性 | 若将来推翻必须新 ADR 和产品范围决策 |
| ADR-007 自然上镜 v1 | Accepted / ApprovedSeparate | 独立默认 OFF，PREVIEW-only GPU 与按需 CPU 纹理平滑 | 仅普通后摄单人，Live/Extensions 互斥；不改变旧风格、不声称真机通过 |

## 2026-09-05 产品决议对架构的约束

CP-03 已选择独立参数面板，CP-04 已选择按覆盖推导口令数量，CP-05 已将非目标设备用例限定为非阻断健壮性检查。完整历史双方及用户决议保留在[需求决策与冲突记录](../traceability/decisions-and-conflicts.md)。对应架构目标合同已同步；不把规范修订当作代码迁移、回归测试或真机通过。CP-01 于 2026-09-12 按用户选择统一为整个屏幕高度的约五分之一；目标机验证 NotRun。

## 原始决策依据

ADR-007（2026-09-03，用户批准，ApprovedSeparate）：自然上镜 v1 独立默认关闭，PREVIEW-only GPU 局部平滑及按需 SDR 副本。仅对此允许 CameraEffect/局部纹理处理；原有风格不磨皮、P-1/P0 隔离、原图与权限门禁不变。详见[beauty.md](beauty.md)，目标机验收 NotRun。

以下段落从重构前架构整段迁入，保留当时的比较、数据点与明确不选项；它们是决策依据，不替代当前依赖事实和验收状态。

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

---

## 7. 明确不选

- 用 MAUI / Flutter CameraView 当主取景器
- 自研 ISP / RAW 成片
- 通过 `CameraEffect`、LUT、小米私有 API 或覆盖原片实现 P1 风格
- 第一版就上 KMP 整包 UI
- 为了「纯 C#」把 CameraX 绑定当唯一实现

2026-09-12：本轮仅按用户逐项审核/优化需求的要求修订公开API语义和验收边界，未替换技术选型或升级依赖。AF/AE分项确认、画质/速度优先、持久源与备份排除、系统热撤回和Live缓存不足回退见[修订记录](../traceability/requirements-optimization-2026-09-12.md)。既有ADR范围不扩大；本审核不代裁实验/校准项；CP-01 后续按用户明确选择完成决议，见冲突记录。

## 8. Live 四用例双编码器（2026-09-13 用户明确裁决）

用户选择“保留四用例＋双编码器（资源开销更大）”。固定 CameraX 1.6.1，采用已核查合法的 VIDEO_CAPTURE-only CameraEffect/SurfaceProcessor；保留 Preview/ImageAnalysis/ImageCapture/VideoCapture<Recorder>。Recorder承担公开协商并生成有界丢弃临时输出，自有 MediaCodec/Muxer 产生唯一正式视频。不得升级依赖或使用受限/私有API、反射；Live与美颜仍互斥。

两路独立所有权与有界分流，共享会话代次。输入sensor时间须与同相机代次CaptureResult精确关联，再记录提交PTS和实际保留编码/mux样本；Start/Stats不能当锚点。UNKNOWN同sensor域仅在精确关联证据成立时可用，质量Fresh门禁保持独立。资源、样本不足或关联失败保留原片并回退JPEG；不等待第二编码器而锁住快门。

影响FR-31/UX-36、相机会话与创意存储专题；当前Delivery为实现中，非真机/目标机验证分别记录。历史O09阻塞与[两次公开API研究](../traceability/live-public-api-research-2026-09-12.md)保留。用户本次未冻结媒体时长/封面偏差容差，未批准目标机通过。
