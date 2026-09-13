# 第三轮独立复验原文

总控已用原生 wait 确认 Reader 01a09574-1209-7b11-b8e0-7e34ba3b683d completed/idle；cursor `5fa14a4a-e2b8-4e67-a8fc-fe042d973f1b:13`。以下逐字保留该 turn 的 final；审查通过只覆盖报告声明的工程范围。

**C3 通过：C2 唯一 P1 重试身份缺陷已修复，本次直接调用链未发现新增代码缺陷。** 结合 C2 其余结论，可以关闭已发现的 8 项可修复代码缺口；这不代表产品验收通过或可以 Go。

- **身份固定正确**：[MainActivity.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/MainActivity.kt) 在重试启动时读取 Binder 的 `pendingCaptureId`；[CaptureSaveCallbacks.kt](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/CaptureSaveCallbacks.kt:23) 在排程前固定该身份；Binder 再核对待保存事务是否匹配。VM 已删除动态读取当前身份的默认参数。
- **旧失败不再归给新捕获**：首次保存和重试共用失败处理与恢复刷新。A 发布原片后开始 B，再收到 A 副本失败时，回调仍携带 A，进入后台失败提示分支，不修改 B 的捕获阶段、身份或最近照片。
- **直接回归检查通过**：非可重试状态不会启动保存；无 pending 或 Binder 同步拒绝会退出 Capturing；页面销毁后的失败保留原身份且不刷新界面；连拍完成回调可按当前批次最近照片身份停止后续拍摄。未发现新的恢复 lease 冲突。

已独立读取现存证据：

| 证据 | 核对结果 |
|---|---|
| Android JVM | **184 项通过**，包含新增回调测试 **2/2** |
| Coach JVM | **82 项通过**，沿用未改模块的有效缓存结果 |
| 合计 | **266 项，0 失败、0 错误、0 跳过** |
| 三个 APK | SHA-256 均与第三轮报告一致 |
| lintDebug | **0 错误、9 警告** |

新增 JVM 测试执行了生产 `retryCapturedSave` 回调分发，没有绕过入口直接给 VM 填正确身份。新增 Android 用例进一步组合真实 VM、生产回调、保存管线与 MediaStore，检查 A/B 交错及 A 恢复记录，但**仅编译通过，执行 NotRun**，也未执行真实 CameraBinder/CameraX 拍摄。

Release 曾有一次增量打包异常，未改源码重试成功；具体原因仍为 **Unknown**。O09 真实 Live 时间锚点仍 **Blocked**；设备测试、权限裁剪后的运行验证、正式研究和产品校准仍 **NotRun/Unknown**。

本轮全程只读，未修改文件、重跑测试或操作设备。
