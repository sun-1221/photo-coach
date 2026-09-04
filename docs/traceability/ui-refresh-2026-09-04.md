# 第二轮相机界面系统优化（2026-09-04）

## Changed

- `docs/design/README.md`：删除未经决议的 38% 规范化表述，明确其为 CP-01 的非规范探索值；交互规格“约五分之一屏幕高度”和 UX-15“约四分之一取景器高度”继续并存，未裁决。
- `ui/theme/Theme.kt`：集中语义颜色、排版、4dp 间距层级、圆角和遮罩令牌；保留既有 3dp 紧凑底栏校准为命名令牌，未放宽 144dp 回归门禁。
- `ui/theme/CameraAccessLayout.kt`、`ui/viewfinder/PermissionDeniedScreen.kt`、`MainActivity.kt`、`strings.xml`：权限拒绝分为可再次请求和需系统设置；前者主操作“重新授权”，后者唯一主操作“打开设置”，冷启动不循环弹权限。
- `ui/viewfinder/ViewfinderScreen.kt`：横屏侧栏由固定 320dp 改为按窗口计算的 252–360dp，并优先保留预览宽度；指导、跳过、快门顺序与行为不变。
- `ui/viewfinder/CreativeResultPanel.kt`：从超大 ViewfinderScreen 拆出拍后选片、七项编辑、受限图片预览和预览效果回退；保留回调、测试标签、非破坏另存和原片优先行为。
- P1 创意菜单未改变参数建议数量或呈现方式；拍后编辑只增加“风格与轻量编辑 / 拍后操作”视觉分组，CP-03 继续 ConflictPending。
- `CameraAccessScreenTest.kt`、`ViewfinderScreenTest.kt`：新增授权/两类拒绝、400×300dp 紧凑横屏、720dp 宽度策略、相机错误恢复，并保留竖屏、2.0x 字体、创意菜单与编辑器覆盖。

## Scope / Delivery

- Scope：P-1 现有 UI 为 InScope；P1 创意层只整理 ApprovedSeparate 的现有入口与编辑器；完整 P0 仍 GateLocked。
- 未新增页面、权限、网络、场景、前摄、设备兼容承诺或 P0 能力。
- Delivery：展示、权限恢复、自适应布局和编辑器拆分代码已编译并通过本地自动化；不据此声明 CameraX、HyperOS、真实保存或 P1 创意业务完整交付。
- CP-01 / CP-03 均未由代码、设计文档或测试静默裁决。

## Verification / Validated

| 层 | 结果 | 边界 |
| --- | --- | --- |
| Android JVM | Pass，143 tests / 0 failures / 0 errors | `:androidApp:testDebugUnitTest` |
| Android instrumented | Pass，47/47 | `Medium_Phone_API_36.1` API 36 模拟器；包含 UI、权限、相机文件与美颜基础仪器集 |
| Android lint | Pass，0 errors / 4 warnings | 4 个既有 UseKtx：CameraBinder 2、CreativeImageProcessor 1、StylePreferenceStore 1 |
| APK | Pass | `:androidApp:assembleDebug :androidApp:assembleDebugAndroidTest` |
| 格式 | Pass | `git diff --check`，仅工作区 CRLF 提示 |

应用 APK：`androidApp/build/outputs/apk/debug/androidApp-debug.apk`

SHA-256：`1A98C3CECE88CC4BCC527ECDDFB85831051C4DE9D286D4A243807114CFAC223B`

模拟器视觉回归产物：

- `androidApp/build/ui-refresh-round2/consent-portrait.png`
- `androidApp/build/ui-refresh-round2/permission-denied-retry.png`
- `androidApp/build/ui-refresh-round2/permission-denied-settings.png`
- `androidApp/build/ui-refresh-round2/viewfinder-portrait.png`
- `androidApp/build/ui-refresh-round2/viewfinder-landscape.png`
- `androidApp/build/ui-refresh-round2/viewfinder-font-2x.png`

模拟器字体与旋转设置已恢复为 1.0 和竖屏。截图是模拟器视觉证据，不替代目标机验收。

## Rules

使用 `photo-coach-coding` Skill；实际完整读取：

- `docs/requirement.md`、`docs/architecture.md`
- `docs/requirements/interaction-guidance-and-scenarios.md`
- `docs/acceptance/acceptance-plan.md`
- `docs/traceability/requirements-matrix.md`
- `docs/traceability/decisions-and-conflicts.md`
- `docs/requirements/p1-creative.md`
- `docs/architecture/camera-and-perception.md`
- `docs/architecture/creative-and-storage.md`
- `docs/design/README.md`
- Skill references：`android-camerax.md`、`p1-creative.md`、`self-check.md`

## NotRun / Residual risk

- 小米 14 Pro、HyperOS、实际快门保存、成片、音量键、真实权限系统差异、焦段/EXIF、Zoom/EV、Extensions、3A、画幅、连续保存、颜色、广色域、连拍/Live/美颜长时热量与产品实验均 NotRun。
- CP-01 与 CP-03 仍为 ConflictPending；当前实现只保持既有 0.20f/144dp 回归基线和参数建议现状，不能把它们解释成产品决议。
- 模拟器截图已导出；本地图片查看工具受 Windows sandbox helper 故障影响，未在代理图像查看器内二次打开，视觉状态由 Compose 语义/边界断言和实际 UI 层级共同验证。
- 工作期间出现与本轮 UI 无关的未追踪 `PendingCaptureTest.kt` 更新；本轮未修改或归因该文件，最终工作区仍需按所有者确认其来源。
