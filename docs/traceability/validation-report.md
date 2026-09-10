# 文档重构验证报告

后续独立实现记录：[2026-09-03 自然上镜实现与验证](beauty-validation-2026-09-03.md)。下文保留原文档重构时点的证据，不把新增模拟器测试倒填为历史真机通过。

- 日期：2026-08-31
- 范围：`docs/` 需求与架构体系
- 自动化结论：Pass
- Android instrumented：NotRun
- 小米 14 Pro / HyperOS 真机：NotRun
- P-1 用户对照实验：NotRun

## 1. 基线与迁移完整性

| 检查 | 结果 |
| --- | --- |
| 重构前需求正文 | 已保存为带非规范声明的历史正文，供人工逐项复核 |
| 重构前架构正文 | 已保存为带非规范声明的历史正文，供人工逐项复核 |
| 历史正文与重构前 Git 基线比较 | Pass（忽略历史文件顶部的非规范声明并统一换行后，正文逐字相同） |
| Retained / Moved 原文块包含检查 | Pass |
| 旧章节、表格、规则、技术决定映射 | Pass；见[迁移登记](migration-register.md) |
| 迁移状态词 | 只使用 Retained、Moved、Consolidated、ConflictPending |
| 冲突双方保留 | Pass；CP-01～CP-05 均为 ConflictPending |

## 2. 编号、阶段与追踪

| 检查 | 结果 |
| --- | --- |
| FR 权威定义 | 31 个，FR-01～FR-31 完整且各一次 |
| UX 权威定义 | 38 个，UX-01～UX-38 完整且各一次 |
| P-1 验收与 P1 隔离 | Pass；P-1 表不含 UX-26～30、UX-33～37 |
| FR → UX | 除明确说明的 FR-14、FR-15～17 外，每个 FR 至少一条现有 UX |
| FR-14 无 UX | 研究事件数据契约，改由自动化与数据/隐私审计，不伪造 UI 用例 |
| FR-15～17 无 UX | 完整 P0 GateLocked，专属用例为显式 acceptance gap |
| Scope / Delivery / Verification | 已分列；代码路径存在仍记完整性 Unknown |

## 3. 命令与结果

| 命令/检查 | 结果 |
| --- | --- |
| `git diff --check` | Pass |
| 全部 Markdown 相对文件链接 | Pass |
| Markdown 尾随空白 | Pass |
| 变更路径仅限 `docs/` | Pass |
| `.\gradlew.bat :coach:test` | Pass（BUILD SUCCESSFUL） |
| `.\gradlew.bat :androidApp:testDebugUnitTest` | Pass（BUILD SUCCESSFUL） |
| `dotnet test .\ExplainApi.sln --no-restore` | Pass（3 passed / 0 failed / 0 skipped） |

Gradle 输出显示任务为 UP-TO-DATE 仍代表本次门禁命令成功；它不升级依赖，也不等于设备验证。

## 4. 范围与文件保护

- Git 状态与 diff 路径仅包含 `docs/`；业务代码、Gradle、版本目录、`.csproj`、Skill 文件均未修改。
- 技术版本仅从 `gradle/libs.versions.toml`、Gradle 与 `.csproj` 抄录到架构入口：CameraX 1.6.1、AGP 8.11.1、Kotlin 2.1.20、minSdk 26、targetSdk 36、ExplainApi net10.0。
- P-1、P0、已批准 P1、其他 P1/后期的 Scope 没有扩大；UX-30 的非目标设备措辞仍在 CP-05 等待拍板。
- P-1 验收不包含已批准 P1 用例；P1 自动化结果不进入 P-1 Go/No-Go。

## 5. 仍为 NotRun / Unknown

以下证据不因文档重构或 JVM/.NET Pass 而改变：

- 小米 14 Pro 地区版本、Android/HyperOS、Build fingerprint 与 App 版本记录。
- 快捷焦段按钮、预览/成片视角、EXIF、Camera2 能力、真实 Zoom/EV。
- Extensions 与 ImageAnalysis 三用例、AE/AF 3A、4:3/16:9 成片、倒计时、音量键、连续 100 张保存。
- 遮挡/脏污在暗景、纯色墙与真实污渍下的误报率。
- 十二种风格与七项编辑的真实颜色、广色域、屏幕色彩管理和 HyperOS 相册观感。
- 三张连拍的间隔、成功率、热量与推荐有效性。
- HyperOS MediaStore 分阶段保存、收藏、回收站、完整质量/省空间副本与进程恢复。
- Live 四用例绑定、无声编码、约 3 秒裁剪、Motion Photo 播放、普通 JPEG 回退、广色域/Ultra HDR。
- Android instrumented 测试、P-1 三组用户对照实验、市场指标与 Delivery 完整性审计。

详细真机表以[分阶段验收](../acceptance/acceptance-plan.md#92-目标机真机矩阵)为准；逐 FR 状态以[功能需求定义与追踪矩阵](requirements-matrix.md)为准。

## 6. 语义超集结论

当前规范集合保留重构前全部内容：入口保留总纲/门禁/隐私/架构基线，整段细则迁入附件，FR/UX 扩展为追踪与分阶段结构，研究与 ADR 保留原始依据，冲突未决项同时保留双方。新增内容仅为权威路由、状态分离、迁移映射和验证证据；未发现删除、弱化或把 NotRun/Unknown 改写为通过。

## 7. 2026-09-01 P1 第二阶段修复验证

本节仅登记 ApprovedSeparate 的 FR-32～FR-37 代码与 JVM 证据，不计入 P-1 Go/No-Go。

| 命令/检查 | 结果 |
| --- | --- |
| `.\gradlew.bat :coach:test --rerun-tasks` | Pass（51 tests，0 failed / 0 skipped） |
| `.\gradlew.bat :androidApp:testDebugUnitTest --rerun-tasks` | Pass（110 tests，0 failed / 0 skipped） |

已覆盖姿势证据计时与换姿势重置、背景细节与手持稳定信号、风格分组与拍前强度、热状态 ML 降级与拍摄中延迟重绑、原片/效果副本分域校验及旧日志兼容。Android instrumented、小米 14 Pro / HyperOS 真机、连续 100 张与保存阶段杀进程矩阵仍为 NotRun。

## 8. 2026-09-01 Ready 与 Motion Photo 修复验证

| 检查 | 结果 |
| --- | --- |
| `.\\gradlew.bat :coach:test` | Pass（55 tests） |
| `.\\gradlew.bat :androidApp:testDebugUnitTest :androidApp:assembleDebug` | Pass（110 tests；Debug APK 生成成功） |
| Ready 回归 | Pass：无可靠人物超时保持恢复口令；Pose-only 问题消失后回找人；未达最低条件只标“随时可拍”；快门仍可用 |
| 小米原生 Motion Photo 样本 | 已读取目标机 `MVIMG_20260829_114022.jpg`；确认 `GCamera`、嵌套 `Container:Item`、真实 Length 与 `Padding=0` |
| 修复后真机安装与拍摄 | NotRun：ADB daemon 重启后设备掉线，`adb devices -l` 无目标机；APK 未安装 |

修复前目标机证据仍保留为 Fail：原 App Motion Photo 在 HyperOS 相册中只显示静态照片，且前九张存在明显 false Ready。该失败不能被本轮 JVM Pass 覆盖；必须在同一台小米 14 Pro 重新连接后完成安装、遮脸/无人画面、Live 拍摄、MediaStore 校验和相册播放回归。

## 2026-09-05 需求内容修订记录

- 用户批准实施既定需求修订方案；同步 OBJ-GUIDANCE、OBJ-EXPERIMENT、OBJ-CAPTURE 和 OBJ-SPEC-CONTROL 的需求、架构、UX、FR、决策及迁移关系。
- CP-03/04/05 已按用户决议收口，CP-01 仍为 ConflictPending。实验公式待决项、最低条件校准与旧保存日志映射明确为 Unknown。
- 本轮仅文档修改；代码实现符合性 Unknown，JVM/.NET、仪器、目标机和产品实验均 NotRun；保留历史证据，不升级当前产品结论。
- 静态验证：38 个 FR、49 个 UX 定义完整且唯一；FR 表格列数正确；CP-01 留在 ConflictPending，CP-02/03/04/05 位于 Resolved 且表格连续；新增/变更本地链接目标存在；无 Unicode 替换字符；git diff --check 通过。仅证明本轮检查范围内的文档结构，不证明业务实现或产品实验通过。
