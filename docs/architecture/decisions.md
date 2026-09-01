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

## 原始决策依据

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
