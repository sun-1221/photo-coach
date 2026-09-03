# Ready、Live/保存与自然上镜：联合真机回归

日期：2026-09-03。结论：部分通过，整体未通过；不是产品验收完成报告。

## Scope / Delivery / Changed

- Scope：P-1 既有 Ready/保存回归与独立批准的 P1 第二阶段、Live、自然上镜。P0 仍 GateLocked；没有扩大机型、权限或网络。
- 与任务“实现 P1 自然上镜美颜”（01a064ef-b114-75e1-a428-ff592c3caa5b）合并测试。向该任务发送停止代码/构建/ADB 操作的交接请求，观察其终态后开始构建；开始时工作区干净。未创建任务或 worktree。宿主无法提供协调 Skill 要求的顶层控制接口，因此只做普通显式交接，未建立或宣称 SQLite/物理 writer lease。
- Delivery：应用基线 `329c01dca93b7b5adc9abbeb0ce020e8f8459fd7`，包含此前 Ready/HyperOS Motion 修复及自然上镜；已覆盖安装，未清空 App 数据或删除用户照片。代码存在不等于端到端验收通过。
- Changed：只修改 BeautyCameraBindingTest 的前置权限检查：已授予 CAMERA 时不重复调用 UiAutomation 授权，仍断言 CAMERA 必须已授权。未更改相机业务实现、阈值或安全权限。新增本报告及矩阵入口。

## 设备和构建身份

- 小米 14 Pro，23116PN5BC / shennong，序列号 84d82dff，国行固件 WNBCNXM。
- Android 16，HyperOS OS3.0.307.0.WNBCNXM。
- Fingerprint：`Xiaomi/shennong/shennong:16/BP2A.250605.031.A3/OS3.0.307.0.WNBCNXM:user/release-keys`。
- App 1.0.0 / versionCode 1。安装前仍为 2026-09-01 旧包；本次新包 SHA-256：`A385F16ECBB31E2C7EA3FC8CAC7928FA8DABE534A1C4DAC9ABB12AA3CC9E8A63`。
- 修正后 androidTest APK SHA-256：`A26C4803FBEC4A622C45B762F80EB20DACCE6C698FC0D4F25625B5C538285A7E`。
- 测试前 USB 充电、电量 49%、battery service 温度 33.2°C、thermal status 0。环境温度未知，thermal 缓存温度与 battery service 不同，不能据此推断温升或散热达标。
- 初始设置：AUTO、4:3、原图、Live OFF、单张、原片+效果自动保存、完整质量、语音+字幕。设置前后核对一致，仅旧设置缺省的 beauty OFF 被明确持久化；没有清除同意或用户偏好。

## Verification / Validated

| 层级/用例 | 结果 | 证据边界 |
| --- | --- | --- |
| coach JVM | 55/55 Pass | 本轮使用 --rerun-tasks，非缓存替代执行 |
| Android JVM | 133/133 Pass | 本轮重新执行；不替代设备 |
| 构建 / lintDebug | Pass | 初次门禁大部分 UP-TO-DATE；测试修正后单独重建 androidTest 成功 |
| 真机核心 instrumented | 14/14 Pass，1.052 秒 | BeautyRendererTest 6、BeautyStateTest 3、MotionPhotoFileInstrumentedTest 1、PublishedAssetVerifierInstrumentedTest 1、BoundedImageDecoderInstrumentedTest 3 |
| 真机 GPU / CPU | 上表范围内 Pass | 合成纹理蒙版、保护区、OFF/过期透传、OES 输入、CPU 无脸说明；不是人物观感/动态对齐 |
| 真机 MediaStore | 合成 8×6 JPEG 发布校验 Pass | 该用例清理自身测试 URI/临时图；不是实际相机照片连续保存、效果副本或恢复矩阵 |
| 真机美颜相机绑定 | Fail | 首次重复授权被系统拒绝；修正权限前置后出现 ImageCaptureException: Not bound to a valid Camera。需进一步区分 harness 生命周期/应用绑定竞争与业务故障，不将其忽略或归为 Pass |
| 全量 37 项 / UI 探针 | 未完成 | 全量卡在首个 ViewfinderActivityTest；单独 requiredStepKeepsSkipAndShutterReachable 仍不返回。手动终止测试进程，日志末尾 Process crashed 是 force-stop 结果，不作为应用自行崩溃证据 |
| 正常启动 / 无人画面 | 有限观察通过 | 两次 am start 冷启动分别 TotalTime 810/763ms；预览截图无额外黑带，卡片为“请露出脸，或靠近一点”，快门 enabled。单一无人场景不证明 Ready 阈值/抖动全部修复，也不构成启动 p95 |
| 实际按快门 / Live / 相册 | NotRun | adb input tap 被 INJECT_EVENTS SecurityException 拒绝，未产生本轮实际捕获样张；不能把尝试点击算作成功拍摄 |

真实执行命令及日志在忽略目录 `androidApp/build/device-validation-20260903/`：

```powershell
.\gradlew.bat :coach:test :androidApp:testDebugUnitTest :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest :androidApp:lintDebug --offline --console=plain
.\gradlew.bat :coach:test :androidApp:testDebugUnitTest --offline --rerun-tasks --console=plain
adb -s 84d82dff install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb -s 84d82dff install -r androidApp/build/outputs/apk/androidTest/debug/androidApp-debug-androidTest.apk
adb -s 84d82dff shell am instrument -w -r com.photocoach.app.test/androidx.test.runner.AndroidJUnitRunner
adb -s 84d82dff shell am instrument -w -r -e timeout_msec 45000 -e class com.photocoach.app.beauty.BeautyRendererTest,com.photocoach.app.beauty.BeautyStateTest,com.photocoach.app.camera.MotionPhotoFileInstrumentedTest,com.photocoach.app.camera.PublishedAssetVerifierInstrumentedTest,com.photocoach.app.creative.BoundedImageDecoderInstrumentedTest com.photocoach.app.test/androidx.test.runner.AndroidJUnitRunner
adb -s 84d82dff shell am instrument -w -r -e timeout_msec 30000 -e class com.photocoach.app.beauty.BeautyCameraBindingTest com.photocoach.app.test/androidx.test.runner.AndroidJUnitRunner
adb -s 84d82dff shell am start -W -n com.photocoach.app/.MainActivity
git -c safe.directory=E:/Code/Agent/photo-coach diff --check
```

`build.log`、`jvm-rerun.log`、`instrumented-core.log`、`instrumented-binding.log`、`instrumented-binding-rerun.log`、`instrumented-ui-probe.log` 保存对应输出。ADB 命令 exit 0 不等于仪器通过，以上以 OK/FAILURES 和完整执行状态为准。截图/设置备份均仅本地、不进入 Git。

## NotRun / Residual risk

- 直接阻塞：HyperOS 拒绝 shell/UiAutomation 的模拟输入与本次重新授权调用。正常 App CAMERA 权限已授予。未修改系统调试安全设置，也未通过其他通道绕过输入限制；需用户允许设备自动控制或手动完成拍摄。
- 相机重绑仪器用例仍失败、UI 仪器仍未完成；不能宣称 37/37 或美颜三用例目标机通过。
- Ready 正脸/遮挡/多人/低比例/失焦/水平恢复与反复跳变、前三批用户样张的后修复对照均未完成。
- 新版实际普通原片/效果副本保存、Live 捕获及降级、Motion 文件尾部、无音轨、HyperOS 识别/播放均 NotRun。历史失败未被本轮合成文件测试消除。
- 三档美颜真实人物、不同光照、画幅/镜头/旋转、遮挡、20 分钟性能/发热、100 张保存、故障与杀进程恢复、.NET、P-1 用户对照实验未运行。
- 启动观察出现“中文语音不可用，已使用字幕”；未做本轮 TTS 专项，不能声称语音恢复。
- CP-01/03/04/05 保持未决；预览截图不裁决布局比例门槛。整体产品/发布验收未完成。

## Rules

使用 photo-coach-coding：完整读取 requirement.md、architecture.md；interaction-guidance-and-scenarios、p1-creative、p1-beauty；acceptance-plan、beauty-validation；requirements-matrix、decisions-and-conflicts；camera-and-perception、creative-and-storage、guidance-and-explain、beauty 架构；android-camerax、mlkit-signals、coach-engine、p1-creative、self-check 执行 references。它要求按层保留失败/NotRun，因此本报告不以自动化或开发版已提交替代真机验收。任务协调 Skill 只用于识别并发风险与交接，其完整运行时规范受宿主接口限制未执行。
