# 讲解 API 执行检查

改 `ExplainApi/`、`ExplainApi.Tests/` 或 Android 客户端“再讲细”调用时读本文件。该文件不授予 P0 范围。

## 强制规范集合

- 先读两个入口，再读 `docs/architecture/guidance-and-explain.md`。
- 客户端交互、同意、提示过滤或离线行为：读 `docs/requirements/interaction-guidance-and-scenarios.md`。
- FR、Phase、组件、交付和证据：读 `docs/traceability/requirements-matrix.md`；验收与设备边界读验收计划。
- 技术选型或供应商边界变化时读 `docs/architecture/decisions.md`；未决口径读冲突登记。

## 范围与实现边界

- 以需求追踪矩阵确认 ExplainApi 的当前 `Scope`。服务路径或 .NET 测试存在只说明 `Delivery` 事实，不能把 GateLocked P0 写成已获准或端到端已交付。
- 保持当前架构规定的 .NET Minimal API、公共合同和可替换 provider 边界；不得顺手引入 Controller、数据库、身份系统或把供应商字段泄漏进客户端合同。
- 候选上限、听众、术语、原因与客户端两步过滤以当前需求/架构附件为准；服务返回候选不改变客户端动作预算。
- 上传只发生在当前规范允许的用户主动、单独同意路径；拒绝、撤回、超时、无效响应和服务不可用时，端侧指导、离线主路径和快门继续。禁止连续预览上传、图片落库或后台持续重试。

## 验证

- 从仓库根运行 `dotnet test ExplainApi.sln`，覆盖合同、非法输出、provider 失败和序列化兼容等触碰行为。
- .NET Pass 不证明 Android 侧同意、压缩、撤回、离线、超时、无后台重试或两步再过滤；这些没有相应证据时保持 `NotRun` 或 `Unknown`。
