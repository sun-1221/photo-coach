# 点脸对焦、调亮入口与美颜反馈修复

日期：2026-09-05。结论：修复包已覆盖安装；自动化局部通过，真人实拍验收未完成。

## Changed / Delivery

- FaceFocusSignalState / AppViewModel / AnalyzerExtras / Signals：点脸动作只有成功 AF/AE 回调才记为本轮已测光；失败或点脸外不算成功。初始自动对焦假定与用户成功动作分开。
- CueSelector / GuidanceSession：暗脸触发的点脸动作可以在成功测光后完成，不因初始 focusOnFace 为 true 而无法退出；持续暗脸不重复选已执行的点脸动作，也不确认画面已达标。
- ReadinessIssue：仍暗时显示「脸部偏暗，可点『调亮』增加曝光」。
- CoachAnalyzer：叠线和点脸命中区域由 FIT_CENTER 修正为与 PreviewView 一致的 FILL_CENTER 居中裁切。真实脸框/机型变换仍需复测。
- ViewfinderScreen：常驻「调亮」入口，打开后滑杆保持，可向亮侧拖动、重置和收起；点脸短时滑杆仍保留。美颜入口明确关闭/预设；启用后的实际处理状态在预览可见。
- 美颜算法没有在本轮增强调参。读取目标机偏好时 beauty_preset=OFF、creative_style=NATURAL_PORTRAIT、mode_preference=AUTO、Live=false、保存策略 ORIGINAL_WITH_RECIPE。自然人像颜色风格不等于美颜开启；这些设置未被擅自更改。
- 补回归，并在当前交互/美颜附件记录此次反馈修复；保留任务开始时已有文档改动。

## Scope

P-1 对焦/曝光为 InScope；P1 美颜状态反馈为 ApprovedSeparate。P0 仍 GateLocked。没有扩大机型承诺或更改美颜默认 OFF、原片优先、强度及新权限。

## Verification / Validated

| 检查 | 结果 | 边界 |
| --- | --- | --- |
| coach JVM | 61/61 Pass | 包含暗脸测光成功后不重复提示、仍暗不确认 Ready |
| Android JVM | 146/146 Pass | 包含成功/失败/脸外/重置与预览坐标映射 |
| debug / androidTest APK 构建 | Pass | 当前工作区构建，非仅使用旧包 |
| lintDebug | Pass | 不能替代运行验证 |
| 模拟器 UI 专项 | 4 个用例最终 Pass | 调亮可独立打开并保持、重置入口、滑杆与提示不压住操作区、美颜实际状态可见 |
| 小米安装 | Pass | adb install -r 返回 Success；保留照片/应用数据 |
| 小米启动请求 | Pass（有限） | am start -W 返回 Status ok，771ms；不是可操作时延或 p95 |
| 小米 UI instrumented | 未完成 | 首个用例无返回后主动 force-stop；随后确认设备 Dozing / mIsScreenOn=false，不能计为通过，也不能将人工终止后的 Process crashed 当成业务崩溃 |
| 真人点脸与美颜观感 | NotRun | 尚无本轮修复后真人场景/样张证据 |

模拟器首轮 3/4 通过；美颜等待人脸用例使用默认 UNKNOWN 温度，因此实际显示热暂停，断言未通过。测试明确设置 NORMAL 后单独重跑通过，没有放宽产品热门禁或修改算法。

命令：

```powershell
.\gradlew.bat :coach:test :androidApp:testDebugUnitTest :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest --offline --console=plain
.\gradlew.bat :androidApp:assembleDebugAndroidTest :androidApp:lintDebug --offline --console=plain
adb -s 84d82dff install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb -s 84d82dff install -r androidApp/build/outputs/apk/androidTest/debug/androidApp-debug-androidTest.apk
adb -s 84d82dff shell am start -W -n com.photocoach.app/.MainActivity
git diff --check
```

专项日志在忽略目录 `androidApp/build/device-validation-20260905-focus/`：ui-tests.log、emulator-ui-tests.log、emulator-beauty-rerun.log。UI 专项为 ViewfinderScreenTest 的 brightnessCanBeOpenedWithoutTappingFaceAndRemainsAvailable、selectedBeautyShowsActualGateOutsideMenu、portraitEvSliderStaysAboveOperationPanel、previewWarningAndExposureStayAboveTheDock。证据不进入照片/人脸资料库。

## 设备 / 构建身份

- 小米 14 Pro，23116PN5BC / shennong，国行，Android 16，HyperOS OS3.0.308.0.WNBCNXM。
- Fingerprint：Xiaomi/shennong/shennong:16/BP2A.250605.031.A3/OS3.0.308.0.WNBCNXM:user/release-keys。
- App：1.0.0 / versionCode 1；基线 HEAD def2b9cbe5db8fbaf859d01c0fdfb3fbad9504ac 加当前未提交修复。
- APK SHA-256：1B0B8618CA0545F7D2C90C6AE4B7CE4B3A49FEF165A2855C850880A0D6117030。

## NotRun / Residual risk

.NET 未运行。真人对焦反馈、脸框在不同位置/旋转/画幅的命中、实际 EV 亮度响应、美颜真实纹理与相册效果副本、连续拍摄、温升和完整产品验收均未完成。没有用本轮结果覆盖旧 Live/保存/美颜绑定失败或宣称 P-1 Go。CP-01 的布局比例门槛未裁决。

## Rules

使用 photo-coach-coding；本对话读取 requirement.md、architecture.md、acceptance-plan.md、requirements-matrix.md、interaction-guidance-and-scenarios.md、camera-and-perception.md、guidance-and-explain.md、p1-beauty.md、architecture/beauty.md、acceptance/beauty-validation.md，以及 android-camerax、mlkit-signals、coach-engine、self-check 执行 references。报告保留 JVM、instrumented、目标机与产品验收的差异。
