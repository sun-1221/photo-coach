# 相机界面与布局优化（2026-09-03）

## Changed

- `androidApp/.../ui/viewfinder/ViewfinderScreen.kt`：顶部图标化控制与意图胶囊；中性深色指导卡、明确步骤色；64dp 白色圆环快门；两侧等宽让快门居中；焦段、创意和最近照片分组；图标语义与选中状态；大字号指导卡增高；按实际底栏高度避让 EV 和降级提示；横屏侧栏避让状态栏。
- `androidApp/.../ui/theme/Theme.kt`：暖金点缀、深灰面板、统一圆角，避免默认菜单色与相机主题不一致。
- `androidApp/.../ui/theme/CameraAccessLayout.kt`、`CameraConsentScreen.kt`、`PermissionDeniedScreen.kt`：共用可滚动说明页布局、明确主次按钮，保留原同意和权限文案及回调。
- `androidApp/.../MainActivity.kt`：系统栏使用适合深色相机的浅色图标；未改变相机绑定、TTS、捕获或保存逻辑。
- `ViewfinderScreenTest.kt`：新增快门居中/语义标签、EV/错误提示避让、大字号可达性三项回归。

## 联网参考（Research only）

- [Samsung：Galaxy S25 相机控制布局](https://www.samsung.com/in/support/mobile-devices/changes-in-camera-controls-layout-on-galaxy-s25/)：常用操作集中、设置按需展开。本次仅借鉴操作层级，不照搬功能或扩大产品范围。
- [Android：Compose 无障碍默认行为](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)：至少 48dp 触控目标、图标描述与明确交互语义。

## Scope / Delivery

- Scope：现有 P-1 UI 为 InScope；已批准 P1 入口只做视觉整理，维持 ApprovedSeparate。未新增页面、权限、依赖、网络、场景、前摄或机型承诺；P0 仍 GateLocked。
- Delivery：上述展示代码已实现、安装包已构建；不据此声明相机或 P1 业务完整交付。
- CP-01：已向用户请求统一底栏比例，但本轮尚未得到决议。保留原代码默认 `0.20f` 约束和原测试，不把它升级成唯一规范；大字号增高服从既有无障碍例外。交互规范与 UX-15 的两种原始口径均未改写。
- CP-03：已请求确认参数建议的信息架构，本轮未重新组织参数卡或改变数量，保留现有菜单结构和未决状态。

## Verification / Validated

| 层 | 本轮结果 | 证据与边界 |
| --- | --- | --- |
| Android JVM | Pass，139 tests / 0 failures / 0 errors | `:androidApp:testDebugUnitTest`；本轮未改业务测试 |
| 应用/仪器 APK | Pass | `:androidApp:assembleDebug :androidApp:assembleDebugAndroidTest` |
| Android lint | 0 errors，4 warnings | `:androidApp:lintDebug`；4 条 UseKtx 位于未修改的 CameraBinder、CreativeImageProcessor、StylePreferenceStore |
| 模拟器竖屏 UI | Pass，24/24 | `adb shell am instrument -w -e class com.photocoach.app.ui.viewfinder.ViewfinderScreenTest com.photocoach.app.test/androidx.test.runner.AndroidJUnitRunner` |
| 模拟器横屏 UI | Pass，3/3 | requiredStepKeepsSkipAndShutterReachable、shutterIsCenteredAndControlsHaveAccessibleLabels、largeTextKeepsGuidanceSkipAndShutterInsideScreen |
| 视觉检查 | 已查看竖/横屏实际截图 | Medium_Phone_API_36.1，1080×2400；取景恢复、控件位置和状态栏已核对，不是小米验收 |
| 格式 | Pass | `git diff --check`；只有 CRLF 提示 |

首轮 UI 回归发现底栏约 144.38dp 超出既有 144dp 检查，已收紧垂直间距，最终回归通过；未放宽该测试。

APK：`androidApp/build/outputs/apk/debug/androidApp-debug.apk`。

SHA-256：`3214BE2089582EFE70661F6038F5FEEC2B1A070D98BC00D1A8368C2D58D74B10`。

本地截图：`androidApp/build/ui-refresh/portrait-final.png` 与 `landscape.png`（构建产物，不作为产品真机证据）。模拟器旋转设置已恢复为运行前值。

## Rules

使用 photo-coach-coding；实际读取：

- `docs/requirement.md`、`docs/architecture.md`。
- `docs/requirements/interaction-guidance-and-scenarios.md`、`docs/acceptance/acceptance-plan.md`、`docs/traceability/requirements-matrix.md`、`docs/traceability/decisions-and-conflicts.md`。
- `docs/architecture/camera-and-perception.md`、`docs/requirements/p1-creative.md`、`docs/architecture/creative-and-storage.md`。
- 技能 references：`android-camerax.md`、`p1-creative.md`、`self-check.md`。

## NotRun / Residual risk

- 小米 14 Pro、HyperOS、实际快门保存、成片、音量键、长时相机/美颜/Live、颜色、热量与产品实验均 NotRun；不能用模拟器 UI Pass 替代。
- 未执行 coach JVM / .NET（对应业务未修改）；首次说明与权限页未单独执行新的仪器用例，只有编译和静态布局检查。
- 实际模拟器在仪器测试后一次打开相机出现 `StandaloneCoroutine was cancelled`；随后冷启动恢复，已核对恢复后的真实预览截图。本轮未定位此相机初始化取消原因，也未改相机管线，后续需专项复核。
- CP-01 / CP-03 仍待用户决议；其他未决规范未由本轮裁决。
