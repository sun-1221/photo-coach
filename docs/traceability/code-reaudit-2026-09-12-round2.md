# 第二轮独立复验与补充确认原文

本文件仅归档指定两个 completed turn 的 final。C2 未通过一个 P1，以下不是第三轮修复后验收。原文及其 /E:/ 链接保持不变；正确实现入口另见 implementation-2026-09-12.md 的第三轮记录。

归档外的当前入口：[第三轮实施记录](implementation-2026-09-12.md)、[MainActivity](../../androidApp/src/main/java/com/photocoach/app/MainActivity.kt)、[AppViewModel](../../androidApp/src/main/java/com/photocoach/app/AppViewModel.kt)。以下两段原文以字面源码展示，不改写其链接或正文；当前可点击入口使用上方链接。

## Reader turn 01a09559-c280-7ab0-962c-3c8fd493d80b

```markdown
目前是**第二轮修复完成、独立复验发现 1 项残留缺陷，尚未全部验收通过**。

- 修复任务已结束并停止写入。
- 已核对 JVM 测试结果：**264 项通过，0 失败、0 跳过**。
- Debug、Release、AndroidTest APK 已构建；lint 为 **0 错误、9 警告**。
- 真机测试尚未执行；Live 时间锚点、正式研究及产品校准仍有阻塞。

本轮 8 项复验结论：

| 项目 | 结论 |
|---|---|
| 原片发布后允许新拍、隔离旧保存结果 | **不通过：重试入口仍漏传捕获身份** |
| Saved 后快速快门先完成研究准备 | 源码及 JVM 验证通过 |
| 准备结束后才开始指导计时 | 源码及 JVM 验证通过 |
| STATIC 固定提示不被实时警告覆盖 | 源码通过；界面测试仅编译，未执行 |
| 相机锁定超时通知 | 生产调度接线及 JVM 验证通过 |
| 眼睛／身体角度证据未知时撤销口令 | 源码及 JVM 验证通过 |
| 捕获、导出、恢复共用配额与预留预算 | 源码及 JVM 验证通过；真实媒体路径待设备验证 |
| FR-02 权限裁剪 | 实际 APK 静态检查通过；运行安全尚未验证 |

残留缺陷位于 [MainActivity.kt:494](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/MainActivity.kt:494)：`retrySave` 的失败回调没有传入原照片的 `captureId`，而 [AppViewModel.kt:1145](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/AppViewModel.kt:1145) 默认取**回调发生时的新捕获身份**。

触发顺序是：照片 A 保存失败 → 重试后原片成功并释放快门 → 用户拍摄 B → A 的后台副本失败。此时可能把 B 错误标为保存失败，甚至把最近照片指回 A。现有测试没有覆盖这个实际重试回调。

下一步需要固定重试事务身份、统一失败处理与恢复记录刷新，并补充上述交错顺序的回归测试。其余 O01–O12 已检查范围内未发现新增回归，但不能据此替代真机验收。本轮只读复验没有修改文件或重跑构建。
```

## Reader turn 01a09560-53b4-7db0-b0f1-63991833a905

```markdown
已核实：**重试回调缺陷成立；恢复 lease 未发现第二个并发缺陷。**

1. **P1：旧重试失败可能误伤新捕获。**  
   [MainActivity.kt:494](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/MainActivity.kt:494) 未传 `captureId`，[AppViewModel.kt:1145](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/AppViewModel.kt:1145) 因此读取回调时的当前身份。A 原片发布失败 → 重试发布成功并释放捕获占用 → 开始 B → A 副本失败，确实可能把 B 标为保存失败，并用 A 的原片更新最近照片。首次 `performCapture` 的显式身份保护不能覆盖此入口。

   最小修复：在重试启动时固定事务身份，所有异步失败显式传入该身份，并统一恢复记录刷新。回归必须覆盖上述 A/B 交错，断言 B 状态、身份与最近照片不被 A 覆盖，A 仍可通过恢复记录重试。

2. **恢复记录包含活跃事务，但实际恢复有互斥。**  
   `loadRecoveryRecords` 确实未过滤活跃日志；不过 [InterruptedSaveRecovery.kt:36](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/InterruptedSaveRecovery.kt:36) 在修改捕获事务前获取同一事务键的 lease，获取失败直接跳过。原片发布后，Binder 将原 lease 转入 `backgroundLeases`，直至后台完成回调结束才释放，没有因释放快门而释放保存所有权。获得 lease 后也会重新读取日志，避免使用已完成或删除的旧快照。

   显式导出及其恢复另外统一经过 [ExportTransactionRunner.kt:25](/E:/Code/Agent/photo-coach/androidApp/src/main/java/com/photocoach/app/camera/ExportTransactionRunner.kt:25) 的事务 lease。因此，列表显示活跃记录本身不足以判定重复保存或并发写入缺陷。

**C2 仍不通过，需修复第 1 项。** 本次仅只读核查，未修改文件或执行测试。
```

