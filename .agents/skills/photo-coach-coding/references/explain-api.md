# 讲解 API

改 `ExplainApi/`、`ExplainApi.Tests/` 或 Android 客户端“再讲细”调用时读本文件。该能力属于完整 P0；P-1 主路径不调用，即使服务代码已存在。

## 范围与契约

- 服务保持 .NET 10 ASP.NET Core Minimal API；不要为一个补充端点顺手引入 Controller、EF、ABP、登录或用户表。
- 端点是 `POST /v1/explain`。请求包含压缩 JPEG、端侧 `signals` 与已有 `currentTips`；响应包含 `tips`、一句 `reason` 和 `available`。
- 服务端输出最多三条候选，每条保留 text、audience、channel。只接受 `shooter`、`subject`、`proxy` audience；未知值丢弃。
- 合同字段会被 Android 和可替换供应商消费。改名、空值策略或枚举语义是跨端契约变更，必须同时改测试和客户端。
- API 的三条是候选，不是 P0 必做三步；客户端仍需过滤长度、术语、重复、听众、方向冲突与两步预算。

## 隐私与失败隔离

- 用户在客户端明确点“再讲细”且首次单独同意后，才上传当前一帧；短边压到 512–768，并去除非必要元数据。
- 不连续上传预览，不持久化图片，不建立人脸库或用户表，不把 provider 日志当作图像存储。
- 拒绝或撤回后不发请求、不后台重试。供应商超时、异常、无效 JSON 或服务不可用时返回/显示“讲解暂时不可用”，端侧指导和快门继续。
- Provider URL 为空时允许 stub；真实 provider 必须可替换，不能把供应商特有字段泄漏进公共合同。
- 不要吞掉诊断所需的内部日志，但对客户端返回稳定、无敏感细节的失败合同。

## 验证

至少覆盖：

- 正常补充并限制为三条。
- provider 失败返回 `available=false` 且端点不抛 5xx。
- 非法 audience、超长、摄影术语和重复候选的服务/客户端责任边界。
- 合同序列化兼容。
- Android 侧拒绝、撤回、离线和超时仍保留内置两步指导。

从仓库根目录运行 `dotnet test ExplainApi.sln`。服务测试通过不能证明客户端同意流程、压缩、无后台重试或飞行模式主路径；未覆盖项必须单独报告。
