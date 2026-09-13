# 固定依赖权限只读复核

Reader turn: 01a09545-233b-7d61-90ab-3e8cf0e5f026。以下为指定终态原文，非设备验证。

**FR-02 当前不符合：合并 APK 确实申请了 CAMERA 之外的 5 项系统权限。** 此前“CAMERA-only”若依据源 Manifest，应撤回；离线主路径不等于安装包无联网权限。证据见[合并清单:27](E:/Code/Agent/photo-coach/androidApp/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml:27)和[权限来源报告:544](E:/Code/Agent/photo-coach/androidApp/build/outputs/logs/manifest-merger-debug-report.txt:544)。

| 权限 | 固定依赖来源 | 处置建议 |
|---|---|---|
| `INTERNET` | Pose common `18.0.0-beta5`、DataTransport CCT `2.3.3` | 移除，并关闭遥测上传入口；验证远程配置失败不影响推理 |
| `ACCESS_NETWORK_STATE` | Pose、WorkManager `2.7.0`、Media3、DataTransport | 移除需配套处理上传入口，不能孤立删除后就宣称安全 |
| `WAKE_LOCK` | WorkManager `2.7.0` | 当前候选可移除；同时排除未使用的前台与旧版 Alarm 执行路径 |
| `RECEIVE_BOOT_COMPLETED` | WorkManager `2.7.0` | 可作为移除候选；不需要开机恢复 SDK 基准测试，业务保存恢复不依赖它 |
| `FOREGROUND_SERVICE` | WorkManager `2.7.0` | 可作为移除候选；当前业务及已检查的 Pose 基准 worker 没有前台执行请求 |
| 应用自有 `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | AndroidX Core `1.16.0` | **保留声明和使用项，保持 `signature`** |

自有签名权限用于限制内部广播接收，并不授予网络、麦克风等系统能力。固定 Core 字节码也确认：兼容路径发现缺少此权限会抛异常。不能为了让权限列表只剩一行而删除它。[AndroidX 官方实现](https://raw.githubusercontent.com/androidx/androidx/androidx-main/core/core/src/main/java/androidx/core/content/ContextCompat.java)

给 Writer 的最小候选方案如下，**不升级依赖、不改 FR-02、不使用反射或修改 SDK**：

1. 在[应用 Manifest](E:/Code/Agent/photo-coach/androidApp/src/main/AndroidManifest.xml)添加 `tools` 命名空间，对上述 5 项系统权限逐项使用 `tools:node="remove"`。合并标记是官方支持的处理方式。[Manifest 合并规则](https://developer.android.com/build/manage-manifests)

2. 同步移除当前无业务用途的 DataTransport Manifest 入口，保留其类依赖，避免链接失败：
   - `com.google.android.datatransport.runtime.backends.TransportBackendDiscovery`
   - `com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService`
   - `com.google.android.datatransport.runtime.scheduling.jobscheduling.AlarmManagerSchedulerBroadcastReceiver`

   固定 `transport-runtime:2.2.6` 的 `DefaultScheduler` 对未注册 backend 有失败回调；但 `Uploader.isNetworkAvailable()` 会直接访问网络状态，因此**只移除网络权限、仍留下上传入口不是充分方案**。覆盖安装遗留的任务和事件也必须验证。

3. 移除当前不用的 WorkManager Manifest 入口：
   - `androidx.work.impl.foreground.SystemForegroundService`
   - `androidx.work.impl.background.systemalarm.SystemAlarmService`
   - `androidx.work.impl.background.systemalarm.RescheduleReceiver`

   当前 `minSdk=26`，固定 WorkManager 在 API 23 以上选择 `SystemJobScheduler`。已检查的 Pose 基准任务没有前台或 expedited 请求。该判断只适用于当前依赖和任务集合，未来新增 worker 必须重新核查。

4. **保留** `WorkManagerInitializer`、`SystemJobService`、ML Kit remote worker，以及 ML Kit 初始化与组件发现服务；不要删除整个 `androidx.startup.InitializationProvider`。同时保留 CameraX 元数据服务、TTS 查询声明和内部签名保护。

特别纠正一个危险的简化方案：**“Pose 改 CPU，再删除 WorkManager 初始化”没有固定版本依据。**

本机字节码显示：

- `pose-detection-common:18.0.0-beta5` 的 `zzh.load()` 可进入加速管理器；
- `acceleration:16.0.0-beta2` 的 `zzl` 构造函数直接调用 `WorkManager.getInstance(context)`；
- `setPreferredHardwareConfigs(CPU)` 只设置硬件偏好，不能证明绕过上述初始化。

官方支持 CPU 推理，但这不是“无 WorkManager 依赖”的保证；官方删除默认初始化的文档也要求调用方提供适当初始化。[Pose 硬件配置](https://developers.google.com/ml-kit/vision/pose-detection/android)、[WorkManager 初始化](https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration)

验收必须包含：

- Debug、Release 的**合并清单和实际 APK**逐项核对：系统权限仅 CAMERA；自有权限仍为 signature；移除的组件没有被其他依赖重新合入。
- 在任何联网启动之前，首次启动并执行真实 Face、Pose 推理、拍照和保存；不能只测试相机能打开。
- 覆盖安装含旧 WorkManager／DataTransport 数据的版本后，验证启动、后台返回、重启及 SDK 基准任务；检查 `SecurityException`、WorkManager 未初始化、缺失服务等错误。
- 新增构建产物权限检查，替代只读取源 Manifest 的 CAMERA-only 断言。

**本轮只读复核已完成。上述是有固定版本依据的候选修复方案，尚未构建或运行验证，不能先将 FR-02 标记通过。** 未修改文件，也未干扰第二轮 Writer。