# 曝光偏暗与摄影技巧更新修复

2026-09-08。当前交付为代码及自动化修复，不是小米实拍通过结论。

## 联网依据

- [CameraX configuration](https://developer.android.com/media/camera/camerax/configuration)：曝光补偿按设备 step/index 表达，调用异步返回结果。
- [CameraControl](https://developer.android.com/reference/androidx/camera/core/CameraControl)：用 ListenableFuture 确认执行；请求替代、相机关闭等失败不能当成成功。
- [CameraX transform output](https://developer.android.com/media/camera/camerax/transform-output)：分析与预览的裁切/旋转需要正确变换。此次保留前轮坐标修复，未把理想矩形单测升级为真机脸框标定。

## Changed / Delivery

- 新增 ExposureController：按能力夹紧/量化请求，成功 Future 返回后才回传实际 index 对应 EV；旧请求与旧相机会话回调失效；异常回传失败。
- CameraBinder / MainActivity / AppViewModel / ViewfinderState / ViewfinderScreen 接入异步结果。界面区分调整中、已确认和失败未确认；失败时滑杆回到上次确认值作为重试起点，但不宣称硬件已回滚。未确认状态持续到新请求或新会话，快门不被曝光反馈锁住。
- SceneApplyRequest 去除 EV 字段，场景初始化不再发曝光请求；窗边、逆光 JSON 的负曝光初值改为 0。用户调整不会在新照片轮次被场景覆盖，重绑按当前选择夹紧后重新确认；明确恢复默认会请求 0 EV。
- PhotoTechniqueEngine 各类动作使用独立 CueId，包括同属 LIGHT 的暗脸与高光动作，以及运动等待/已开启连拍两种动作。AppViewModel 接入真实 ID，使既有防抖/动作替换能识别新句。
- 新增 ExposureGuidancePolicy，SignalFactory 以接近饱和像素比例形成高光事实，替代顶部平均亮度的天空推断。降曝光许可与高光事实分开：暗脸/单脸采样未知不建议继续压暗，仍保留暗脸+高光时的 HDR 候选。提示改用画面高光而非未经识别的天空。
- 同步交互、相机架构及本报告。保留本轮开始前的未提交改动，不升级依赖、不提交 Git。

## Scope

P-1 曝光/相机控制 InScope；既有 P1 摄影技巧 ApprovedSeparate；完整 P0 仍 GateLocked。美颜算法和默认原片保存策略没有在本轮更改，仍需独立验证效果副本与真人观感。

## Verification / Validated

- coach JVM：73 tests，0 failures / errors / skipped。
- Android JVM：171 tests，0 failures / errors / skipped。
- 新回归覆盖曝光 Future 延迟、失败、乱序、重绑、能力范围；白墙无剪裁不要求降曝光、未知/暗脸不降曝光；场景不产生负初值；不同 LIGHT 技巧稳定替换且保持 1/2 和快门可用。
- debug APK、androidTest APK 与 lintDebug：全部通过；最终 Android 构建 BUILD SUCCESSFUL。
- debug APK SHA256：`95CB48A316C64E7AAD3B1912AA420253E3EB3CEC698A05C143F1EEA38BF208E2`。
- `git diff --check` 已执行；仅代表静态差异格式检查。
- `adb devices -l`：无设备。未安装，instrumented/目标机/真实照片对照 NotRun；没有将测试包构建当成仪器测试执行。

```powershell
.\gradlew.bat :coach:test :androidApp:testDebugUnitTest :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest :androidApp:lintDebug --offline --console=plain
```

## NotRun / Residual risk

小米实测 EV 回调与 EXIF、手动调亮后的真实脸部亮度、重绑/连续拍摄、高光阈值、脸框对齐、真人技巧有效性、美颜和相册副本、热量、.NET、完整产品验收未执行。Y≥245 且占比≥2% 是当前防误报工程初值，不是已校准的曝光合格标准。Face 与背景的亮度差仍不能证明真实肤色/用户偏好，原两张暗片的全部成因尚未通过同场景对照排除。

## Rules

沿用本对话已读取的 photo-coach-coding：requirement.md、architecture.md；交互指导、P1 创意/美颜、相机感知、指导/Explain、创意保存/美颜架构、acceptance-plan、beauty-validation、requirements-matrix；android-camerax、mlkit-signals、coach-engine、self-check。本轮追加读取 sources.md 并联网核对官方 API。没有调用完整 P0 或后期能力，也没有自动把真机 NotRun 升级为 Pass。
