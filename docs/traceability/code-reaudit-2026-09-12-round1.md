# 首轮独立复验原文

Reader turn: 01a09530-ae15-7733-a459-4a1f724576c9。复验未通过，以下为指定终态原文，不是第二轮修复后验收。

**首轮修复已完成，但独立复验未通过，还需要一轮修复。**

已核实的成果：

- 257 项 JVM 测试通过，失败、跳过均为 0。
- 两个 APK 的哈希与报告一致；lint 为 0 错误、9 警告。
- .NET 3 项通过目前依据修复方报告，本轮未独立重跑。
- Live 正常时间轴仍明确阻塞，当前降级为 JPEG；真机验收尚未执行。

本轮复验确认了以下 7 项缺口，可交回原修复任务处理：

1. **[P1] 原片发布后仍锁住新快门。**  
   [CameraBinder.kt:835](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraBinder.kt:835) 等待配方、效果生成和发布全部结束才释放状态；[AppViewModel.kt:1171](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/AppViewModel.kt:1171) 只更新保存文字和缩略图。副本处理缓慢时，普通新拍仍被阻止。需要分离捕获与后续保存状态，并用“原片已发布、效果处理挂起”的生产链路验证新快门。

2. **[P1] 快速连拍下一张会绕过研究轮次的参数准备。**  
   [GuidanceSession.kt:377](E:/Code/Agent/photo-coach/coach/src/main/kotlin/com/photocoach/coach/GuidanceSession.kt:377) 在 Saved 状态按快门时直接新建下一轮并进入 Capturing；[AppViewModel.kt:864](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/AppViewModel.kt:864) 此前只检查旧轮的准备状态。下一次定时检查才发现轮次变化，可能在捕获后才恢复参数，导致事件顺序和实验初值错误。需覆盖保存成功后 1.5 秒内再次按屏幕、音量键快门的测试。

3. **[P2] 研究准备等待侵占指导时间。**  
   [AppViewModel.kt:418](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/AppViewModel.kt:418) 确认参数后仅更新事件起点，没有调整已启动的指导计时。准备超过 1.5 秒会使动态观察立即到期；超过 8 秒会使静态第一步立即超时。应从参数确认后开始指导，或完整暂停并恢复计时；测试慢重绑和慢 EV 回执。

4. **[P2] 静态研究卡仍被实时信号覆盖。**  
   [ViewfinderScreen.kt:1463](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/ui/viewfinder/ViewfinderScreen.kt:1463) 优先显示实时镜头遮挡提示，STATIC 并未排除。虽然 GuidanceSession 忽略候选，最终界面仍随画面改变。需隔离静态实验的指导输出，并验证镜头遮挡等输入不会替换固定卡。

5. **[P2] AF/AE 超时状态没有通知界面。**  
   [CameraLocks.kt:95](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/CameraLocks.kt:95) 先调用会递增代次的 `timeout()`，随后检查旧代次是否相等，因此不会发送失败状态。界面可能一直显示请求中。需修正通知顺序，测试真实超时回调及随后到达的旧结果。

6. **[P2] 必做姿势的证据失效检查仍不完整。**  
   [GuidanceSession.kt:818](E:/Code/Agent/photo-coach/coach/src/main/kotlin/com/photocoach/coach/GuidanceSession.kt:818) 对 OPEN_EYES 只检查人脸可靠性，没有检查睁眼分类是否仍存在；TURN_TO_WINDOW 也遗漏身体角度证据检查。分类或关键点消失时，旧提示可能继续保留。应统一必要证据检查，并补这两个失效分支的回归。

7. **[P2] 副本导出绕过持久暂存配额。**  
   [InterruptedSaveRecovery.kt:48](E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/InterruptedSaveRecovery.kt:48) 的导出生成路径没有空间、配额检查；失败副本会被保留供重试，连续另存新副本可继续累积。需在导出及恢复生成前统一执行预算检查，验证达到配额后拒绝新增、保留已有恢复源。

现有新增测试没有覆盖上述完整链路；原片反馈仪器测试只断言文字和缩略图，也尚未在设备上执行。下一步应由总控把这 7 项交回原修复任务，修复后再次独立复验。