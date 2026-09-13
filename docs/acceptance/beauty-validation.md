# 自然上镜专项验收操作单

本文件提供 UX-46～UX-49 的执行方法，不创建新的 FR/UX，也不代替[分阶段验收规范](acceptance-plan.md)。Scope 为 ApprovedSeparate；目标机只能记录小米 14 Pro。默认 OFF，P-1 对照实验始终关闭。当前证据见[验证报告](../traceability/beauty-validation-2026-09-03.md)。

## 1. 使用入口

点取景器“美颜·风格”，菜单内可显式“关闭 Live”和“切到普通模式”，再主动选择“自然上镜”的关闭/自然/柔和。它是独立开关，与十二种颜色风格分开。启用后检查菜单内实际处理状态，不能只看勾选。默认只保存原片和 App 私有配方；需要效果照片时主动“另存副本”，或拍摄前选择“原片 + 效果自动保存”。拍后预览暂不渲染美颜，界面会说明另存时应用。

自然与柔和是待真机校准的保守初值，不提供无上限滑杆，不宣称美白、瘦脸、皮肤分割或自动遮挡识别。热状态未知/升温时可能暂停美颜预览，保留原始预览和快门。

## 2. 每轮证据身份

记录日期、地区/机型、Android/HyperOS、Build fingerprint、App 版本/构建 SHA、APK SHA-256、已验收后摄焦段、画幅、方向、环境温度、电量和热状态。分清 OFF 对照与 NATURAL/SOFT，锁定相同机位、光源、曝光与颜色风格。

只使用知情同意的测试人物/照片；照片不进入 Git。不要记录人脸坐标、身份、帧内容或生物特征日志；证据使用匿名样张编号和必要设备/性能数据。不上传图片，不增加相册或麦克风权限。

## 3. 执行矩阵

| 项目 | 执行方法 | 必须保留的证据 |
| --- | --- | --- |
| 开关/兼容 | 全新设置、旧配方、三档重启/重置；Live、自动和全部可用 Extensions 双向互斥；语音/字幕四组合；未完成指导时拍照 | 明确拒绝原因、原开关不被暗改；重置 OFF；快门与指导独立 |
| 几何 | 4:3/16:9 × 0/90/180/270° × 默认/已验收后摄；正脸、侧脸、远近移动、转头、多人进出；前后台/旋转/切换开关各 20 次 | 蒙版无偏移/翻转；眼鼻嘴与背景不磨；无可靠单人和旧帧及时退出；无黑屏或线程持续增长 |
| 质量 | 同景 OFF/自然/柔和；窗光、暖光、逆光、暗光；不同自然肤色、眼镜、胡须、发丝和手挡脸；随机顺序盲评 | 未磨掉真实结构/纹理；无明显晕边、塑料感和偏色；记录不合格样本，不用合成图推断肤质效果 |
| 原片/副本 | 三档 × 原图/至少两种风格 × 两保存策略 × 两质量档；比较原始捕获 JPEG 与公开原片的压缩图像字节；打开 SDR 副本、检查尺寸/方向/安全 EXIF | 原片不被美颜改写；默认只有原片；显式策略最多一份对应效果副本；拍后换当前开关不改变旧照片配方 |
| 故障/恢复 | 原片发布、配方、效果检测/生成、效果发布各阶段注入失败或杀进程；模拟磁盘不足/无脸/未知引擎；重启和重试 | 原片保留、不重复公开项；错误不冒充成功；当前进程无有效单人时给未应用说明；恢复日志中的 captureId/预设/版本一致 |
| 性能/热 | 三档各连续取景 20 分钟；导出 20 次；单拍与连拍；系统热状态 Light/Moderate/Severe/Critical 及恢复 | 原始帧/阶段时延 p50/p95/p99、掉帧、导出耗时、堆/原生/GPU 内存、温度与电量；无 ANR/OOM，Severe 10 秒内降载；不从模拟器 SwiftShader 推算真机性能 |

使用 Android Studio/Perfetto 检查 `beauty-gl` 与相机分析线程，用 Memory Profiler 或 `adb shell dumpsys meminfo com.photocoach.app` 辅助记录。系统级内存/帧率不等于单个着色器耗时，应保留采样方法与单位。当前未建立真机帧率/功耗基线，不能填写推测值或给出达标结论。

## 4. 可复现自动化

在仓库根目录执行：

```powershell
.\gradlew.bat :coach:test :androidApp:testDebugUnitTest --offline
.\gradlew.bat :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest :androidApp:lintDebug --offline
```

将两个 debug APK 安装到明确选择的测试设备后，可运行以下专项（替换 `<测试设备序列号>`，不要操作未知设备）：

```powershell
adb -s <测试设备序列号> shell am instrument -w -r -e package com.photocoach.app.beauty com.photocoach.app.test/androidx.test.runner.AndroidJUnitRunner
```

另有 `ViewfinderScreenTest#beautyMenuOffersExplicitPresetsWithoutBlockingShutter` 与 `#beautyFallbackIsVisibleAndKeepsShutterAvailable` 两个 UI 用例。GPU 测试使用合成纹理/常量色，三用例测试只证明短时管线与资源释放，不证明真实人脸对齐、肤色/观感或 HyperOS 保存恢复。

## 5. 结果模板

每项记录 `Pass / Fail / NotRun`、证据路径、设备身份、样本数、实际观测、复现步骤与剩余风险。无目标机、无照片授权或无性能采样时填 NotRun，不使用“代码已实现”或“模拟器通过”代填产品验收。

2026-09-12：质量与相对OFF的时延/内存/温升判据、样本量须在专项采样前冻结；原有初值不视为真机门槛已通过。新增模拟系统热撤回/Emergency/Shutdown，验证停止新捕获与恢复记录，不以真实加热到关机作验收目标。持久源及备份排除按创意存储合同检查。
