# 收尾自检

只检查和运行触碰层。命令通过不能替代范围、隐私和真机口径；失败即该层未通过。

## 默认命令

当前环境是 Windows PowerShell：

- 口令引擎：`.\gradlew.bat :coach:test`
- Android JVM 单测：`.\gradlew.bat :androidApp:testDebugUnitTest`
- 讲解 API：`dotnet test ExplainApi.sln`

在 Unix shell 把 `.\gradlew.bat` 换成 `./gradlew`。不要并行启动会争同一 Gradle 输出或同一 `bin/obj` 的重复构建。

## 范围与版本

- [ ] 已标明 P-1 / P0 / P1；没有把后续页面、权限、网络、场景或设备混入当前层。
- [ ] P-1 仍是后摄单人、两个意图、三个场景、最多两个必做动作、手动快门和离线保存。
- [ ] 候选最多三条没有变成同屏三条、3/3 或快门前置条件。
- [ ] 用户意图锁、跳过、Ready 和每轮最多一条可选建议仍成立。
- [ ] 没有合影入口、角色切换入口、第一版 iOS、其他 Android 厂商分支或不可用“即将推出”。

## Android 相机

- [ ] 仍是 Kotlin + Compose + CameraX；依赖版本变更若非任务目标已撤销。
- [ ] 默认同时绑定 Preview + ImageAnalysis + ImageCapture，画幅策略一致；只有显式无声 Live 能进入四用例。
- [ ] Live 四用例继续保留 Analysis，强制普通 Photo、关闭 Extensions、不启用音频；任何不支持或失败都回退普通 JPEG并清除假 Live 状态。
- [ ] Analysis 是 `STRATEGY_KEEP_ONLY_LATEST`；只关闭 `ImageProxy`，正常/异常路径都释放。
- [ ] Extensions 同时检查 availability 与 ImageAnalysis support；失败回标准 Photo 且指导继续。
- [ ] 没有用不支持 Analysis 的 `ExtensionSessionConfig` 取代三用例主路径。
- [ ] EV、Zoom、快捷焦段和扩展模式来自当前能力；没有固定 2x 或伪光学标签。
- [ ] AE/AF 锁定、重绑解锁、倒计时取消、音量键快门和捕获偏好保持可恢复。
- [ ] 权限说明先于 CAMERA；拒绝、占用、绑定和保存失败有可见恢复路径。
- [ ] P-1 与无声 Live 未新增其他权限，尤其没有 `RECORD_AUDIO`；最近照片只用本次 URI。
- [ ] MediaStore 发布正确处理 API 29+ `RELATIVE_PATH/IS_PENDING` 和失败清理。
- [ ] TTS 只播 subject；字幕与拍摄者动作卡不依赖语音成功。

## 感知与口令

- [ ] Face FAST、Pose STREAM；没有无需求 contour/tracking/classification。
- [ ] 两张脸以上或姿态不足时不进入单人姿势，不输出身份或评分。
- [ ] 镜头遮挡用多帧且文案保守。
- [ ] `coach/` 无 Android 依赖；P0/P1 场景不会参与 P-1 匹配。
- [ ] 场景对/错/必现/禁现、两步预算、去抖、相反方向和 TTS 节流测试通过。

## Explain API

- [ ] 只有 P0 按需路径调用；P-1 和离线主路径不依赖服务。
- [ ] 仍是 Minimal API；没有顺手加入数据库、身份或持续上传。
- [ ] 合同稳定、最多三条候选、非法 audience 过滤、provider 失败隔离。
- [ ] 客户端单独同意、撤回不重试、压缩和两步再过滤有实现或明确未覆盖。

## P1 创意层

- [ ] `CreativeStyle` 是批准的十二种；预览、导出和七项编辑共用同一确定性矩阵，支持 undo / redo / reset；低于 API 31 或失败回原图。
- [ ] 默认先发布原片并私有保存配方，效果 JPEG 只在明确另存或用户主动开启自动双保存时生成；任何失败不撤销或覆盖原片。
- [ ] 每次捕获使用不可复用 `captureId`；保存分阶段可见、部分成功只重试失败阶段，journal 恢复不重复发布。
- [ ] 原片写 `DATE_TAKEN`；派生图只复制安全 EXIF，排除 GPS、缩略图、MakerNote 和未知标签。
- [ ] 完整质量/省空间副本均明确为兼容 SDR；解码不超过对应像素上限、最多两个受限 Bitmap、临时文件全路径清理、OOM 受控。
- [ ] 三张只在用户明确开启后顺序捕获；重入、单张失败、重试和已保存照片处理符合状态机。
- [ ] 选优不显示美学分、不自动删照片；P1 数据不计入 P-1 Go。
- [ ] 打开、分享、收藏和回收站只操作本 App 本次 URI；不读整本相册，不新增定位，平台不支持时不伪装成功。
- [ ] Motion Photo 只发布一个 `…MP.JPG` 主文件，XMP 含版本/封面时间戳和仅 Primary/MotionPhoto 两项；视频长度准确且紧密位于文件末尾。
- [ ] Motion Photo 录制、裁剪、XMP、发布、取消和恢复路径都清理临时视频/pending 行；失败可靠回退普通 JPEG且至多发布一个主文件。

## 结果口径

- [ ] 自动化结果逐条列出，不用“全部通过”掩盖未运行层。
- [ ] 没有小米 14 Pro 记录的快捷焦段/EXIF、真实 Zoom/EV、Extensions、3A、画幅、连续保存、遮挡误报、十二种风格/七项编辑真实颜色、连拍热量、HyperOS 分阶段保存/收藏/回收站，以及 Live 四用例/编码/裁剪/Motion Photo 播放写 `NotRun`。
- [ ] 真机通过项包含地区版本、Android/HyperOS、Build fingerprint 和 App 版本。
