# 自然上镜 v1：实现与验证记录

日期：2026-09-03。结论：开发版实现及本地自动化通过；尚未达到小米 14 Pro 发布验收。不是肤质/颜色/功耗实测报告。

## Scope / Delivery

- Scope：用户批准的独立 P1 `ApprovedSeparate`；只限当前后摄单人普通 Photo，默认 OFF，提供自然/柔和两档。没有扩大 P-1/P0、前摄、其他机型、权限、网络、人物属性推断或重美颜范围。
- Delivery：独立开关、复用端侧关键点的 PREVIEW-only GPU 平滑、有界 CPU 成片处理、捕获配方冻结、原片优先与按需 SDR 副本已接入。目标机效果与端到端恢复完整性仍为 Unknown。
- 依据 `photo-coach-coding` 技能，先在需求/架构登记 CameraEffect/局部纹理处理的有限例外，维持十二种风格不磨皮语义、最新帧分析、原片不可覆盖和真机证据隔离。技能文件未修改。

## Changed

| 文件/区域 | 修改目的 |
| --- | --- |
| [beauty/](../../androidApp/src/main/java/com/photocoach/app/beauty) | 档位、纯数值几何、脸部保护蒙版、时序衰减、EGL/GLES 处理器及 CPU guided-filter 导出 |
| [CoachAnalyzer.kt](../../androidApp/src/main/java/com/photocoach/app/analysis/CoachAnalyzer.kt)、AnalysisFrameMetadataStore、ClosingAnalyzerExecutor | 按 ML Kit 结果时间戳匹配帧元数据；复用关键点；关闭后完成清理但不再发 UI 结果 |
| CameraBinder、CameraSettings、ThermalPolicy、MainActivity | 普通三用例与效果绑定/回退、独立持久化、拍摄中延迟重绑、热暂停；修复热监听旧 API 初始化问题 |
| AppViewModel、ViewfinderScreen | 显式选择、互斥说明、参数冻结、效果失败可见提示、拍后美颜另存说明 |
| CreativeImageProcessor、EditRecipe、SaveJournalStore | 原片先发布；效果重新检测拍摄图；旧记录 OFF、版本化配方与重试沿用捕获值 |
| JVM / androidTest | 新增 23 项 JVM、12 项仪器用例；同步旧 UI 测试的 Ready 前提、现行口令与滚动菜单操作 |
| 需求/架构/验收文档 | FR-38、UX-46～49、ADR-007、有限范围例外及[真机验收操作单](../acceptance/beauty-validation.md) |

没有提交、推送、发布、创建 worktree 或改动依赖/权限。基线 commit 为 `d23ba825e1c7634850b5af07e65756dd2e941b7a`，本报告针对其上的未提交工作区改动，不代表该 commit 自身含有美颜。

## Verification / Validated

| 层级 | 实际结果 | 边界 |
| --- | --- | --- |
| coach JVM | 55/55 Pass | 已用 `--rerun-tasks` 执行，不仅依赖缓存 |
| Android JVM | 133/133 Pass，0 failures/errors/skips | 几何/时序、旧配方、保存阶段、设置与关闭竞态；不是 Android 真机执行 |
| Debug APK / androidTest APK | Build Pass | 沿用 pinned CameraX 1.6.1、ML Kit face 16.1.7，无新增依赖 |
| Android lintDebug | Pass，0 error、4 条现有 UseKtx 建议 | 没有屏蔽检查或添加 lint baseline；低版本实际运行仍 NotRun |
| Android 全量仪器 | 37/37 Pass，最终一次 66.543 秒 | 只在本任务启动的 Android 16 API 36.1 x86_64 模拟器执行 |
| 小米 14 Pro / HyperOS | NotRun | 无目标机连接；模拟器不形成其他机型兼容承诺 |
| 产品验收 / P-1 对照实验 | NotRun | 自动化不能代替真实人物盲评、保存恢复、性能或 Go 决策 |

实际命令（仓库根目录）：

```powershell
.\gradlew.bat :coach:test :androidApp:testDebugUnitTest --offline --rerun-tasks
.\gradlew.bat :coach:test :androidApp:testDebugUnitTest :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest :androidApp:lintDebug --offline
.\gradlew.bat :androidApp:testDebugUnitTest --offline
.\gradlew.bat :androidApp:assembleDebugAndroidTest --offline
.\gradlew.bat :androidApp:lintDebug --offline
adb -s emulator-5580 install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb -s emulator-5580 install -r androidApp/build/outputs/apk/androidTest/debug/androidApp-debug-androidTest.apk
adb -s emulator-5580 shell am instrument -w -r com.photocoach.app.test/androidx.test.runner.AndroidJUnitRunner
git -c safe.directory=E:/Code/Agent/photo-coach diff --check
```

分次运行中，最后的应用逻辑改动之后重新通过 Android JVM、构建、lint 和完整仪器回归；最后的菜单测试调整只改 androidTest，并重建/重新安装了测试 APK。命令中的 `adb` 实际使用本机 Android SDK 的绝对路径。

模拟器：`Medium_Phone_API_36.1`，仅本次 `emulator-5580`；Android 16，SwiftShader 软件 GPU。Fingerprint：`google/sdk_gphone64_x86_64/emu64xa:16/BE4B.251210.005/14574095:user/release-keys`。

[调试 APK](../../androidApp/build/outputs/apk/debug/androidApp-debug.apk) SHA-256：`A385F16ECBB31E2C7EA3FC8CAC7928FA8DABE534A1C4DAC9ABB12AA3CC9E8A63`。自动化报告目录：[Android JVM](../../androidApp/build/reports/tests/testDebugUnitTest/index.html)、[coach JVM](../../coach/build/reports/tests/test/index.html)、[lint](../../androidApp/build/reports/lint-results-debug.html)；build 产物可被后续构建替换。

专项验证内容：

- 默认 OFF、非法旧设置安全恢复、Live/非普通模式双向拒绝、不锁快门、设置重置保留其他同意项。
- 四方向/两画幅/镜像的仿射数学、无效/溢出矩阵、多人/侧脸/缺关键点、旧帧衰减、运动/旋转/缩放突变重置。
- GPU/OES 着色器实际执行；OFF/缺蒙版/过期像素原样透传；有蒙版确实变化且保护区不变；常量色/尺寸切换/重复 close；CPU 保护区域及无脸说明。
- 两轮真实 CameraX Preview + ImageAnalysis + ImageCapture + PREVIEW-only effect 绑定，输出收到帧、分析仍工作、捕获非空 JPEG，解绑后 Surface 所有权回调和美颜线程释放。该 harness 不包含真实人脸质量、应用完整保存链或长时负载。
- 拍摄期间效果失败/设置重置不改变捕获预设、版本、保存策略和质量；旧 recipe/journal 缺字段 OFF；同 captureId 重试只替换一份 journal。
- 完整仪器回归同时覆盖现有缩放、取景器、TTS、文件打包、有界解码和 MediaStore 基础资产校验；不能由此推断 HyperOS 行为。
- FR 定义 38 行且唯一、UX 定义 49 行且唯一；Manifest、Gradle 依赖及 `.agents/.codex` 无差异；`diff --check` 通过。

## 发现并修正的问题

1. ML Kit 关闭 ImageProxy 后才发送结果，单个 lastFrame 可被下一帧覆盖：改为有界的时间戳元数据表。
2. 全量仪器初跑发生退出崩溃：迟到的 ML Kit task 回调被已关闭分析线程拒绝。加入关闭安全 executor 和竞态单测；后续完整 37 项未再出现。
3. lint 暴露既有 `context.mainExecutor` 的 minSdk 不匹配；改用 ContextCompat，API 29 监听器仅在受保护路径延迟创建，不调整热策略。
4. 旧仪器测试将中性 Ready 当作“可以拍了”、硬编码旧短口令并假设整个长菜单同时可见。保留原有断言目的，补正确状态输入、实际规则文本和显式滚动；没有删除失败用例或改变产品文案以迎合测试。

## Rules

实际读取入口：`docs/requirement.md`、`docs/architecture.md`。

实际读取相关规范：`requirements/p1-creative.md`、`requirements/p1-beauty.md`、`requirements/interaction-guidance-and-scenarios.md`、`architecture/camera-and-perception.md`、`architecture/creative-and-storage.md`、`architecture/beauty.md`、`architecture/decisions.md`、`traceability/requirements-matrix.md`、`traceability/decisions-and-conflicts.md`、`acceptance/acceptance-plan.md`。

技能 references：`version-scope.md`、`android-camerax.md`、`mlkit-signals.md`、`p1-creative.md`、`sources.md`、`self-check.md`。API/算法依据见[美颜架构](../architecture/beauty.md)中的官方文档与论文链接；资料与编译通过不构成真机验收。

## NotRun / Residual risk

- 小米 14 Pro 真实人物效果、不同光照/肤色、遮挡/胡须/发丝/眼镜、快速运动、两画幅和已验收镜头的真实蒙版对齐。几何椭圆不是皮肤或遮挡分割，不能宣称精准肤色保护。
- 帧率与延迟 p50/p95/p99、导出耗时、峰值堆/原生/GPU 内存、20 分钟取景、温升/功耗及热降级恢复。预览 GPU 和导出 CPU 精度/采样不同，JPEG 编码还会引入差异。
- 真机强制 GPU 失败、前后台/切镜头压力、100 张连续保存、拍摄中杀进程、磁盘不足、HyperOS 相册与完整 beauty journal 恢复。当前进程的未应用说明有覆盖；进程重启后的后台恢复提示尚未专项验证。
- 原有广色域/Ultra HDR、Extensions、Motion Photo 播放与设备兼容承诺没有扩展；.NET 本轮未运行且未修改。
- CP-01、CP-03、CP-04、CP-05 保持既有 ConflictPending，本轮没有替用户裁决布局、参数卡、口令数量或其他机型范围。

下一步按[专项操作单](../acceptance/beauty-validation.md)做目标机校准；在取得样张与性能证据前，不将本开发版表述为已完成产品/发布验收。
