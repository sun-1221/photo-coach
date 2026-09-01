# Trigger evaluation

MeasuredStatus: NotRun
DefinitionRevision: 2026-09-01-a
DescriptionChanged: false

只统计全新会话的隐式触发。同会话推理、静态校验、显式 `$photo-coach-coding` 和 SKILL.md 自述都不能当实测。本次保持 description 不变：继续覆盖仓库实现、修复、重构、测试和只读合规检查，并排除 Skill 包维护、纯产品文案、通用概念、跨平台选型与第一版 iOS 实施。

方法：正例和近邻负例各至少 10 条，Train 60% + Holdout 40%；每条新会话至少 3 次；正例 trigger rate ≥ 0.5，负例 < 0.5。只用 Train 调 description，Holdout 只做最终验证。当前没有包外 fresh-session harness artifact，因此所有动态结论保持 `NotRun`。

## Should trigger

### Train

| ID | Query | Coverage |
| --- | --- | --- |
| T1 | 按需求实现拍照教练 P-1 Android 取景器 | 仓库 + P-1 实现 |
| T2 | photo-coach 的 CameraX ImageAnalysis 卡住了帮我修 | CameraX 缺陷 |
| T3 | 给 scenes.json 补两步口令回归单测 | coach 回归 |
| T4 | 实现 P0 再讲细 ExplainApi 的失败隔离 | Minimal API + 范围门禁 |
| T5 | 按开发规范只读检查取景器目录是否合规 | 仓库只读合规 |
| T6 | photo-coach 取景器黑屏了 | 口语化恢复问题 |
| T7 | 修 P1 三张连拍失败后还继续拍的问题 | 已批准创意层 |
| T8 | 给 photo-coach 实现无声 Motion Photo，四用例不支持时回退普通 JPEG | P1 Live + 能力回退 |
| T9 | 检查 CameraX 修复是否漏读新的架构附件和验收矩阵，只读 | 新文档附件路由 |
| T10 | 核对 coach 口令实现有没有把 JVM Pass 写成小米真机 Pass | 状态合规检查 |

### Holdout

| ID | Query | Coverage |
| --- | --- | --- |
| H1 | Fix the CameraX preview black screen in E:\\Code\\Agent\\photo-coach\\androidApp | 英文 + Windows 路径 |
| H2 | 老板让我补 P-1 口令引擎对/错样张测试 | 个人语境 + 回归 |
| H3 | photo-coach Explain API 挂掉时取景器仍要离线工作 | 跨层失败隔离 |
| H4 | 帮我看创意风格导出失败会不会把原片删掉，只读 | P1 只读合规 |
| H5 | 快捷焦段别再写死 2x，按小米 14 Pro 能力改 | 能力驱动镜头 |
| H6 | 修 captureId 分阶段保存恢复后重复发布照片的问题 | P1 保存幂等 |
| H7 | 审查这个 Motion Photo 变更是否错误引用了 docs/history 旧架构 | 历史权威边界 |

## Should not trigger

### Train

| ID | Query | Why |
| --- | --- | --- |
| N1 | 解释 Kotlin 协程，不改仓库 | 通用概念 |
| N2 | 审核 Shop 仓库这个 PR | 错误仓库 |
| N3 | 根据 photo-coach 技术栈创建一个 SKILL.md | 创建 Skill；owner: OtherSkill |
| N4 | 只改 requirement.md 的市场文案，不写实现 | 纯产品文案 |
| N5 | Codex Rules 和 Skills 有什么区别 | 产品概念 |
| N6 | 用 Flutter 做第一版取景器选型方案 | 跨平台选型 |
| N7 | 整理 docs/history 的重构前快照说明，不检查代码 | 纯文档历史维护 |

### Holdout

| ID | Query | Why |
| --- | --- | --- |
| HN1 | 审核 photo-coach-coding 这个 Skill 包，先别改代码 | Skill 包审核 |
| HN2 | CameraX KEEP_ONLY_LATEST 是什么，只讲概念 | 通用技术解释 |
| HN3 | 给 InHouse/ABP 项目补 XML 注释 | 兄弟仓库 |
| HN4 | 第一版同步做 iOS 取景器，按跨平台方案实施 | 被排除的平台实施 |
| HN5 | 帮我润色拍照教练的产品一句话 | 纯文案 |
| HN6 | 根据 research/evidence.md 写一份市场汇报，不碰实现 | 纯研究文案 |

## 结果记录

尚未执行全新会话隐式触发矩阵，保持 `NotRun`。

```markdown
| Query ID | Expected | Runs | Correct | Trigger rate | Result | Notes |
| --- | --- | ---: | ---: | ---: | --- | --- |
```
