# 保存恢复与紧凑横屏修复（2026-09-05）

基线：359c9b39d926340159ffcc54fde5153f9802f0ab；本次修改在本地 checkout，未提交、推送或创建 worktree。

## Changed / Delivery

- `SaveTransactionRegistry`：按 journal 目录、captureId、序号提供进程内事务所有权。CameraBinder 从捕获到保存完成/放弃持有所有权；失败保留重试上下文，实例释放后在已有保存工作结束时释放所有权。恢复跳过活动事务，取得所有权后重新读 journal，避免重放已完成的旧快照。
- `CameraBinder` / `MainActivity`：已释放页面的保存任务继续完成；录制退出时已捕获 JPEG 回退保存；待处理保存未完成前不关闭其执行器，旧页面不会继续发起下一张连拍。
- `InterruptedSaveRecovery`：Motion 包恢复/发布失败时清理失败行，持久化普通 JPEG 回退选择和阶段状态，再以原始 JPEG 字节发布。已删除的 MediaStore 行视为已清理；删除被拒绝时保留可恢复记录，不发布替代项。原片已成功时只续写剩余阶段。
- `ViewfinderScreen`：紧凑横屏给创意菜单和最近照片固定 116dp，侧栏最小 256dp（含像素取整余量），保住 48dp 焦段/最近照片触控区域。
- 新增 `SaveTransactionRegistryTest`、`InterruptedSaveRecoveryTest`，加强紧凑横屏的最近照片点击、尺寸与不重叠断言。

## Scope

P-1 UI 修复为 InScope；保存/Live 为 ApprovedSeparate P1。P0 保持 GateLocked，后期保持 Deferred；无依赖升级、权限、网络、设备兼容承诺或产品范围扩展。

## Verification / Validated

使用本机已有 Gradle 缓存，`GRADLE_USER_HOME=C:\Users\19744\.gradle`，Gradle 命令附 `--offline --console=plain`。

| 层 | 实际验证 | 结果与边界 |
| --- | --- | --- |
| coach JVM | `gradlew.bat :coach:test` | Pass，59 项；本次任务 UP-TO-DATE，无 coach 变更 |
| Android JVM | `gradlew.bat :androidApp:testDebugUnitTest` | Pass，145 项，0 failures/errors；含并发所有权与旧 lease 迟到释放测试 |
| 编译 | `:androidApp:assembleDebug :androidApp:assembleDebugAndroidTest` | Pass，应用与仪器 APK 均成功 |
| lint | `:androidApp:lintDebug` | Pass，0 错误、4 项 UseKtx 警告；不作为真机证据 |
| Instrumented | `InterruptedSaveRecoveryTest` 与 `ViewfinderScreenTest` | 首次 29 项中 27 通过、2 失败；修复已删除 URI 的重复删除与像素取整后，重跑恢复全部 3 项及紧凑横屏 1 项，4/4 Pass。其余 25 项 UI 已在前次通过，未重复运行 |
| 格式 | `git diff --check` | Pass |
| .NET | ExplainApi 未触碰 | NotRun |
| 小米 14 Pro / 产品验收 | 无目标机验证 | NotRun |

仪器环境：`Medium_Phone_API_36.1`，Android 16 / API 36，`emulator-5554`；fingerprint：`google/sdk_gphone64_x86_64/emu64xa:16/BE4B.251210.005/14574095:user/release-keys`。

最终定向仪器命令（两次 `adb install -r` 安装本次应用和 test APK 后）：

```text
adb -s emulator-5554 shell am instrument -w -r -e class com.photocoach.app.camera.InterruptedSaveRecoveryTest,com.photocoach.app.ui.viewfinder.ViewfinderScreenTest#compactLandscapeUsesAdaptiveRailAndKeepsGuidanceSkipAndShutterReachable com.photocoach.app.test/androidx.test.runner.AndroidJUnitRunner
```

日志：`androidApp/build/save-recovery-layout-instrumented.log`。初次 wrapper 受沙箱网络限制失败，改用已有缓存后完成验证；不是依赖下载成功证据。

本次应用 APK SHA-256：`20E31CA0D64F62A7D11AC3AD01A4C64FCB8FDC0834BF526C38966C8929F5899D`。

## Rules

沿用本任务先前已完整读取的 `photo-coach-coding` Skill 和规范：`docs/requirement.md`、`docs/architecture.md`；交互/P1 创意/自然上镜需求；相机与感知、创意与保存、自然上镜、指导与讲解架构；验收计划、需求矩阵及冲突登记。执行 references：`android-camerax.md`、`p1-creative.md`、`version-scope.md`、`coach-engine.md`、`mlkit-signals.md`、`self-check.md`。

## NotRun / Residual risk

- 进程内所有权与 MediaStore 恢复已有自动化证据；真实 CameraX 捕获中 Activity 重建、进程终止、HyperOS 存储压力/相册、Live 四用例与播放、连续保存和热稳定性仍需小米 14 Pro 验收，均 NotRun。
- 模拟器的原 JPEG 字节、恢复幂等和 UI 断言不证明目标机成片、性能或产品实验通过。
- CP-01/03/04/05 保持 ConflictPending；未裁决竖屏比例、参数三卡、短口令数量或非目标设备兼容范围。
