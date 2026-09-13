# 需求原文逐单元覆盖索引


> 此索引是 CP-01 用户裁决前的审核快照；保留当时原文、行号和 SHA-256，不代表后续文档仍与快照一致。2026-09-12 后续用户已统一为约整个屏幕高度五分之一，当前规范以需求/验收及决策记录为准。
日期：2026-09-12。此文件从修订后的当前文档逐条索引，防止只核查FR总表而漏掉附件。审核结论见[228项逐项矩阵](requirements-line-audit-2026-09-12.md)，本索引仅提供原文位置和对应审核行，不新增需求定义，也不把“已映射”当作实现或验收Pass。

粒度：每个表格数据行、列表项、正文段落分别一行；流程/布局图保留每个非空原文行并标为流程上下文。标题、空白、Markdown表头/分隔线仅用于结构，不单列需求。原文摘录限前100字符便于查找，完整规范以源文件为准；同一条文内部并列约束沿用所在审核行的完整检查内容。文件散列和行号只对应本次快照，之后改文档须重新生成索引。

## 文件清单

| 文件 | SHA-256 | 索引单元数 |
| --- | --- | --- |
| [docs/requirement.md](../requirement.md) | `cbc52c92d6d5c35b4196e5e8f12158b2182dc8d337731f5ac381e0958ee22d61` | 193 |
| [docs/requirements/interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) | `e79fafaa76ed32431a5cfaeff99eb4c694b5e1e6a6c48ccb988f851378170e47` | 339 |
| [docs/requirements/p1-creative.md](../requirements/p1-creative.md) | `48a3b751f0f2f13814ea561e1574a3d8fbd1db9b7a183e4056264c5f97283c61` | 105 |
| [docs/requirements/p1-beauty.md](../requirements/p1-beauty.md) | `7a976be882c32270da5da87371529120684c1736923ca2b1ec996c89965cb2af` | 10 |
| [docs/requirements/deferred-scope.md](../requirements/deferred-scope.md) | `60fd3c0ec690cb9e97dac50efb31a7aa394b20726fb72c2627aad737acd55ab0` | 16 |
| [docs/acceptance/acceptance-plan.md](../acceptance/acceptance-plan.md) | `26ea7ef3ebb77c79a707baf168b9ab42985b8ab6b0d824f6566a6f0aa01e0ab4` | 97 |
| [docs/traceability/requirements-matrix.md](../traceability/requirements-matrix.md) | `c33b28d9692a9c9e2eedec9da5ec45f0b46734875ee7075eaf3f290206e8f868` | 57 |

共 817 个原文单元，全部映射到既有逐项审核ID；没有将索引数量当作独立功能数量。

## docs/requirement.md

| 单元 | 原文位置 | 摘录（非权威副本） | 逐项审核ID |
| --- | --- | --- | --- |
| C-0001 | [requirement.md](../requirement.md) L3 · 拍照教练：产品需求总纲 · 正文/条款 | - 状态：P-1 验证版需求已修订；实验待决参数冻结并过线后才进入完整 P0 | N-44 |
| C-0002 | [requirement.md](../requirement.md) L4 · 拍照教练：产品需求总纲 · 正文/条款 | - 更新日期：2026-09-12 | N-44 |
| C-0003 | [requirement.md](../requirement.md) L5 · 拍照教练：产品需求总纲 · 正文/条款 | - 产品负责人：待填写 | N-44 |
| C-0004 | [requirement.md](../requirement.md) L6 · 拍照教练：产品需求总纲 · 正文/条款 | - 技术实现：architecture.md | N-44 |
| C-0005 | [requirement.md](../requirement.md) L7 · 拍照教练：产品需求总纲 · 正文/条款 | - 分阶段验收：分阶段验收规范 | N-44 |
| C-0006 | [requirement.md](../requirement.md) L8 · 拍照教练：产品需求总纲 · 正文/条款 | - 端到端追踪：功能需求定义与追踪矩阵 | N-44 |
| C-0007 | [requirement.md](../requirement.md) L9 · 拍照教练：产品需求总纲 · 正文/条款 | - 市场与官方依据：research/evidence.md | N-44 |
| C-0008 | [requirement.md](../requirement.md) L11 · 拍照教练：产品需求总纲 · 正文/条款 | 本文是产品行为、版本范围、验收口径与隐私的最高权威入口。细则可以位于规范附件，但附件不能静默扩大范围或覆盖本入口；技术选型与模块拆分以架构入口为准；Skill 只约束长期编码行为，不承载产品需求。读取… | N-44 |
| C-0009 | [requirement.md](../requirement.md) L13 · 拍照教练：产品需求总纲 · 正文/条款 | 版本术语： | N-44 |
| C-0010 | [requirement.md](../requirement.md) L17 · 拍照教练：产品需求总纲 · 正文/条款 |  /  P-1  /  不公开上架的需求验证包，只在小米 14 Pro 验证后摄单人、三类场景、两步指导和真实保存  /  | N-44 |
| C-0011 | [requirement.md](../requirement.md) L18 · 拍照教练：产品需求总纲 · 正文/条款 |  /  P0  /  P-1 全部门槛通过后才开发的第一版发布候选  /  | N-44 |
| C-0012 | [requirement.md](../requirement.md) L19 · 拍照教练：产品需求总纲 · 正文/条款 |  /  P1  /  与 P-1/P0 证据隔离的增强能力；已单独批准“小米 14 Pro P1 创意层”和第 4.5 节第二阶段扩展，但均不改变 P-1 的 Go 门槛  /  | N-44 |
| C-0013 | [requirement.md](../requirement.md) L20 · 拍照教练：产品需求总纲 · 正文/条款 |  /  后期  /  没有进入近期排期，只保留产品方向  /  | N-44 |
| C-0014 | [requirement.md](../requirement.md) L24 · 0. 权威结构与迁移控制 · 正文/条款 | - FR-01～FR-38 的唯一当前权威定义在功能需求定义与追踪矩阵；本入口不复制第二份定义。 | N-44 |
| C-0015 | [requirement.md](../requirement.md) L25 · 0. 权威结构与迁移控制 · 正文/条款 | - UX-01～UX-49 的唯一当前权威定义在分阶段验收规范；P-1 与已批准 P1 分表，完整 P0 的专属 UX 缺口明确保留。 | N-44 |
| C-0016 | [requirement.md](../requirement.md) L26 · 0. 权威结构与迁移控制 · 正文/条款 | - 交互、指导、场景、P1 创意与后期规格是当前规范附件；研究文件只承载依据，不改变 Scope、Delivery 或 Verification。 | N-44 |
| C-0017 | [requirement.md](../requirement.md) L27 · 0. 权威结构与迁移控制 · 正文/条款 | - 零丢失迁移登记逐项映射重构前章节、表格、规则和技术决定，状态只使用 Retained、Moved、Consolidated、ConflictPending。 | N-44 |
| C-0018 | [requirement.md](../requirement.md) L28 · 0. 权威结构与迁移控制 · 正文/条款 | - 历史快照是重构前完整副本，明确为非当前规范；唯一性检查排除历史目录。 | N-44 |
| C-0019 | [requirement.md](../requirement.md) L29 · 0. 权威结构与迁移控制 · 正文/条款 | - 所有未决冲突均在需求决策与冲突记录保留双方。入口不会用摘要或所谓“更严格解释”替代用户决策。 | N-44 |
| C-0020 | [requirement.md](../requirement.md) L37 · 1.1 一句话 · 正文/条款 | 面向拿小米 14 Pro 帮伴侣或朋友拍照的新手的拍中教练：先确认拍摄意图，不打分、不等待完美匹配，默认只给拍摄者和被拍者各一个动作，用户随时可以按快门，并在 30 秒内保存一张真实照片。 | N-01 |
| C-0021 | [requirement.md](../requirement.md) L41 · 1.2 要解决的问题 · 正文/条款 | 用户会打开系统相机和按快门，但经常遇到： | N-01 |
| C-0022 | [requirement.md](../requirement.md) L43 · 1.2 要解决的问题 · 正文/条款 | - 不知道站在哪里、手机应该往哪移动。 | N-01 |
| C-0023 | [requirement.md](../requirement.md) L44 · 1.2 要解决的问题 · 正文/条款 | - 不知道人物与地标应各占多少画面。 | N-01 |
| C-0024 | [requirement.md](../requirement.md) L45 · 1.2 要解决的问题 · 正文/条款 | - 不知道该怎样简单、自然地指挥被拍者。 | N-01 |
| C-0025 | [requirement.md](../requirement.md) L46 · 1.2 要解决的问题 · 正文/条款 | - 建议太多、术语太重时反而错过拍摄时机。 | N-01 |
| C-0026 | [requirement.md](../requirement.md) L47 · 1.2 要解决的问题 · 正文/条款 | - 第三方相机如果黑屏、卡顿或保存失败，任何指导都失去价值。 | N-01 |
| C-0027 | [requirement.md](../requirement.md) L49 · 1.2 要解决的问题 · 正文/条款 | 本产品首先解决“如何更快完成一次满意的拍摄”，不是建立摄影课程、姿势商城或评分游戏。 | N-01 |
| C-0028 | [requirement.md](../requirement.md) L55 · 1.3 核心产品原则 · 正文/条款 |  /  拍下来优先  /  快门始终可用；指导不能锁快门  /  | N-02 |
| C-0029 | [requirement.md](../requirement.md) L56 · 1.3 核心产品原则 · 正文/条款 |  /  最多两步  /  最多一个拍摄者动作 + 一个被拍者动作；结束后统一判定“可以拍了 / 随时可拍 / 恢复提示”，不以步骤完成代替画面判断  /  | N-03 |
| C-0030 | [requirement.md](../requirement.md) L57 · 1.3 核心产品原则 · 正文/条款 |  /  一次一件事  /  取景器同一时刻只显示一个主要动作；已批准 P1 参数面板为主动打开的独立查看上下文，不与指导卡/字幕或 TTS 同时输出  /  | N-04 |
| C-0031 | [requirement.md](../requirement.md) L58 · 1.3 核心产品原则 · 正文/条款 |  /  用户意图优先  /  用户选择后，自动识别不能覆盖本次选择  /  | N-05 |
| C-0032 | [requirement.md](../requirement.md) L59 · 1.3 核心产品原则 · 正文/条款 |  /  不追分  /  不显示美学分、匹配分、准备度分或 100% 对齐  /  | N-06 |
| C-0033 | [requirement.md](../requirement.md) L60 · 1.3 核心产品原则 · 正文/条款 |  /  真实成片  /  使用系统/厂商计算摄影能力；不生成换脸、换姿势或替换原片  /  | N-07 |
| C-0034 | [requirement.md](../requirement.md) L61 · 1.3 核心产品原则 · 正文/条款 |  /  主路径离线  /  意图、两步指导、快门和保存不依赖网络  /  | N-08 |
| C-0035 | [requirement.md](../requirement.md) L62 · 1.3 核心产品原则 · 正文/条款 |  /  隐私最小化  /  P-1 只申请相机权限；端侧分析，不识别身份、不建人脸库  /  | N-09 |
| C-0036 | [requirement.md](../requirement.md) L63 · 1.3 核心产品原则 · 正文/条款 |  /  先验证再扩展  /  P-1 未过线，不把完整 P0 或 P1 扩展混入 P-1；单独批准的 P1 必须独立开关、独立验收且不得计入 P-1 Go 证据  /  | N-10 |
| C-0037 | [requirement.md](../requirement.md) L69 · 1.4 已拍板 · 正文/条款 |  /  首个平台  /  Android；P-1、P0 和 P1 只验收小米 14 Pro  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0038 | [requirement.md](../requirement.md) L70 · 1.4 已拍板 · 正文/条款 |  /  其他机型  /  P-1、P0、P1 均不做兼容承诺，后期再决定是否立项  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0039 | [requirement.md](../requirement.md) L71 · 1.4 已拍板 · 正文/条款 |  /  第一角色  /  拍摄者是主用户，App 帮他指挥被拍者  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0040 | [requirement.md](../requirement.md) L72 · 1.4 已拍板 · 正文/条款 |  /  首个任务  /  单人人物特写和“人带景”  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0041 | [requirement.md](../requirement.md) L73 · 1.4 已拍板 · 正文/条款 |  /  提示方式  /  拍摄者动作卡在指导取景上下文始终显示，P1 独立参数面板期间按 CP-03 暂停；中文语音和被拍者字幕可独立开关，默认同时开启  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0042 | [requirement.md](../requirement.md) L74 · 1.4 已拍板 · 正文/条款 |  /  第一入口  /  打开后直接进入教练取景器，不设模板首页  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0043 | [requirement.md](../requirement.md) L75 · 1.4 已拍板 · 正文/条款 |  /  提示来源  /  端侧感知 + 内置规则库；云端只做 P0 可选“再讲细”  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0044 | [requirement.md](../requirement.md) L76 · 1.4 已拍板 · 正文/条款 |  /  相机控制  /  点按对焦、EV、可用的光学焦段、闪光状态、屏幕与音量键快门  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0045 | [requirement.md](../requirement.md) L77 · 1.4 已拍板 · 正文/条款 |  /  成片  /  保存到系统相册可见目录，无水印  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0046 | [requirement.md](../requirement.md) L78 · 1.4 已拍板 · 正文/条款 |  /  代拍  /  P-1 不做；完整 P0 才加入简化交接界面  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0047 | [requirement.md](../requirement.md) L79 · 1.4 已拍板 · 正文/条款 |  /  前摄、合影、角色切换  /  P-1 不做；按第 4、13、14 节推进  /  | N-01、N-10、N-18、N-19、N-20、N-21、N-22、N-27、N-32 |
| C-0048 | [requirement.md](../requirement.md) L89 · 3.1 待验证假设 · 正文/条款 | &gt; 对首个目标用户，如果 App 先确认“人物特写 / 人带景”，默认只要求一个拍摄者动作和一个被拍者动作，不显示分数、不等待完美匹配，那么用户能在 30 秒内完成一张比系统相机和静态提示卡更受… | N-01、N-16 |
| C-0049 | [requirement.md](../requirement.md) L91 · 3.1 待验证假设 · 正文/条款 | 实时语音、伴侣帮拍、陌生人模式、姿势叠图和端侧分析都已有重叠产品。第一版真正要验证的是：动态指导是否比静态提示卡有增量，以及“两步就拍”是否让用户愿意复用。 | N-01、N-16 |
| C-0050 | [requirement.md](../requirement.md) L95 · 3.2 P-1 对照实验 · 正文/条款 | - 至少 20 名符合首个目标用户定义的首次用户。 | N-11、N-12、N-13、N-14、N-15 |
| C-0051 | [requirement.md](../requirement.md) L96 · 3.2 P-1 对照实验 · 正文/条款 | - 每人完成室内窗边、室外人带景、逆光人像三个场景。 | N-11、N-12、N-13、N-14、N-15 |
| C-0052 | [requirement.md](../requirement.md) L97 · 3.2 P-1 对照实验 · 正文/条款 | - 同一设备、同一场景在用户内随机轮换三种条件：系统相机、静态提示卡、动态教练。 | N-11、N-12、N-13、N-14、N-15 |
| C-0053 | [requirement.md](../requirement.md) L98 · 3.2 P-1 对照实验 · 正文/条款 | - 静态卡与动态教练使用相同的第三方相机管线、每场景固定初始参数和保存方式；实验期间均关闭按实时场景自动应用初始参数。参数由研究员在每轮开始前按配置恢复，用户手动调整仍允许并记录。 | N-11、N-12、N-13、N-14、N-15 |
| C-0054 | [requirement.md](../requirement.md) L99 · 3.2 P-1 对照实验 · 正文/条款 | - 静态卡不消费画面信号来选句或判断动作改善；按与动态教练相同的 1/2、2/2 顺序显示固定 shooter / subject 句。两组共享手动跳过、最短展示、超时及提示通道规则；动态组独有的信号… | N-11、N-12、N-13、N-14、N-15 |
| C-0055 | [requirement.md](../requirement.md) L100 · 3.2 P-1 对照实验 · 正文/条款 | - 静态卡不显示依赖画面判断的“可以拍了”；流程结束显示中性“随时可拍”。相机权限、快门、保存错误反馈两组一致。是否维持相同后台分析负载作为实验配置项冻结，不把“不消费信号”与“不运行分析”混为一谈。 | N-11、N-12、N-13、N-14、N-15 |
| C-0056 | [requirement.md](../requirement.md) L101 · 3.2 P-1 对照实验 · 正文/条款 | - 每轮登记匿名会话编号、场景、条件、随机顺序、设备/构建和配置版本。实验配置关闭 P1 参数面板、风格、美颜、连拍、Live、扩展姿势及推荐；普通单拍、原片保存和提示通道设置保持一致。系统相机条件的… | N-11、N-12、N-13、N-14、N-15 |
| C-0057 | [requirement.md](../requirement.md) L102 · 3.2 P-1 对照实验 · 正文/条款 | - 每次更换条件时重新站位和构图，避免直接复制上一轮动作。 | N-11、N-12、N-13、N-14、N-15 |
| C-0058 | [requirement.md](../requirement.md) L103 · 3.2 P-1 对照实验 · 正文/条款 | - 保留原始人数、拍摄次数、失败数、中位耗时和盲评选择，不能只报告百分比。 | N-11、N-12、N-13、N-14、N-15 |
| C-0059 | [requirement.md](../requirement.md) L104 · 3.2 P-1 对照实验 · 正文/条款 | - 研究员要求的重复测试不计入自然复用。 | N-11、N-12、N-13、N-14、N-15 |
| C-0060 | [requirement.md](../requirement.md) L110 · 3.3 成功指标 · 正文/条款 |  /  30 秒完拍  /  首次用户 30 秒内保存成功率 ≥ 90%  /  30 秒内完成相册写入的有效动态教练轮次 / 全部有效动态教练轮次；从取景器可操作计时，超时、捕获和保存失败计未达标；… | M-01 |
| C-0061 | [requirement.md](../requirement.md) L111 · 3.3 成功指标 · 正文/条款 |  /  两步预算  /  ≥ 80% 的成功拍摄在两个必做动作内完成  /  最多一个 shooter 动作 + 一个 subject 动作；设备错误不计入  /  | M-02 |
| C-0062 | [requirement.md](../requirement.md) L112 · 3.3 成功指标 · 正文/条款 |  /  系统相机对比  /  盲评偏好教练照片的比例 ≥ 60%  /  系统相机与动态教练照片乱序二选一；拍摄者、被拍者分别记录  /  | M-03 |
| C-0063 | [requirement.md](../requirement.md) L113 · 3.3 成功指标 · 正文/条款 |  /  动态增量  /  相比静态卡，盲评偏好至少高 10 个百分点，或中位完拍时间至少缩短 20%  /  两项至少通过一项  /  | M-04 |
| C-0064 | [requirement.md](../requirement.md) L114 · 3.3 成功指标 · 正文/条款 |  /  自然复用  /  无提醒 D7 真实拍摄再次打开比例 ≥ 30%  /  不把研究安排的第二次打开算入  /  | M-05 |
| C-0065 | [requirement.md](../requirement.md) L115 · 3.3 成功指标 · 正文/条款 |  /  口令有效  /  可机器判断的口令在 5 秒内使目标信号改善 ≥ 70%  /  水平、人脸占比、脸部亮度等；姿势句人工抽检  /  | M-06 |
| C-0066 | [requirement.md](../requirement.md) L116 · 3.3 成功指标 · 正文/条款 |  /  错误口令  /  无法执行、听众错误、同屏冲突合计 ≤ 5%  /  真机录像复核  /  | M-07 |
| C-0067 | [requirement.md](../requirement.md) L117 · 3.3 成功指标 · 正文/条款 |  /  不打扰  /  被拍者认为语音“尴尬或打扰”的比例 ≤ 20%  /  拍摄后单题反馈  /  | M-08 |
| C-0068 | [requirement.md](../requirement.md) L118 · 3.3 成功指标 · 正文/条款 |  /  性能  /  打开相机 p95 &lt; 1 秒；稳定候选到内置提示显示 p95 ≤ 300ms  /  相机启动从打开请求到预览及快门可操作；提示从稳定门槛满足到首个可见动作帧，不含防抖等待… | M-09 |
| C-0069 | [requirement.md](../requirement.md) L119 · 3.3 成功指标 · 正文/条款 |  /  保存可靠性  /  快门到系统相册写入成功率 ≥ 99.5%  /  真机连续拍摄；失败可见且可重试  /  | M-10 |
| C-0070 | [requirement.md](../requirement.md) L120 · 3.3 成功指标 · 正文/条款 |  /  离线  /  飞行模式主路径通过率 100%  /  意图 → 两步指导 → 快门 → 相册  /  | M-11 |
| C-0071 | [requirement.md](../requirement.md) L124 · 3.3.1 指标数据合同与冻结门禁 · 正文/条款 | 每次实验使用固定版本的计算规则，不在看到结果后更换分母、排除项或达标路径。App 事件由端侧日志产生；条件、场景、盲评、单题反馈及跨会话匿名关联由研究侧记录产生。研究记录按匿名会话编号关联，不要求登录… | FR-14、N-31、M-03、M-04、M-05、M-10 |
| C-0072 | [requirement.md](../requirement.md) L128 · 3.3.1 指标数据合同与冻结门禁 · 正文/条款 |  /  两步预算  /  分子为两个必做动作内完成的成功动态轮次，分母为成功动态轮次；替换仍占原动作槽；跳过和可选附加建议单列，设备错误单列，不借此删除保存指标中的失败  /  | FR-14、N-31、M-03、M-04、M-05、M-10 |
| C-0073 | [requirement.md](../requirement.md) L129 · 3.3.1 指标数据合同与冻结门禁 · 正文/条款 |  /  盲评与动态增量  /  保留每对照片的条件、随机位置及 shooter / subject 各自选择；缺图、缺评、平局及双方分歧单列；汇总规则和“高 10 个百分点”的比较基准未冻结前，不得计… | FR-14、N-31、M-03、M-04、M-05、M-10 |
| C-0074 | [requirement.md](../requirement.md) L130 · 3.3.1 指标数据合同与冻结门禁 · 正文/条款 |  /  完拍时间比较  /  记录每轮可操作和原片发布时刻、超时及失败；有效配对、失败轮次如何进入时间比较须在实验前冻结，不能只保留更快的成功样本  /  | FR-14、N-31、M-03、M-04、M-05、M-10 |
| C-0075 | [requirement.md](../requirement.md) L131 · 3.3.1 指标数据合同与冻结门禁 · 正文/条款 |  /  D7 自然复用  /  由研究侧匿名参与者编号关联首次及后续会话，并记录是否研究安排、是否真实拍摄；失访不默认为复用，确切窗口和分母须冻结  /  | FR-14、N-31、M-03、M-04、M-05、M-10 |
| C-0076 | [requirement.md](../requirement.md) L132 · 3.3.1 指标数据合同与冻结门禁 · 正文/条款 |  /  口令有效与错误口令  /  每条口令事先声明可观察改善条件、起算事件和抽检方法；有效率按 5 秒内满足改善条件的可评估口令次数计算，无法观测单列；错误率按复核后至少命中一类错误的口令次数 / … | FR-14、N-31、M-03、M-04、M-05、M-10 |
| C-0077 | [requirement.md](../requirement.md) L133 · 3.3.1 指标数据合同与冻结门禁 · 正文/条款 |  /  不打扰  /  记录有效答卷和缺答数，比例为选择“尴尬或打扰”的有效答卷 / 全部有效答卷；缺答不得当成未打扰  /  | FR-14、N-31、M-03、M-04、M-05、M-10 |
| C-0078 | [requirement.md](../requirement.md) L134 · 3.3.1 指标数据合同与冻结门禁 · 正文/条款 |  /  保存可靠性与离线  /  记录被相机接受的捕获请求、首次发布、恢复结果、失败和重复资产；首试成功率与重试恢复率分开报告。离线分母为预先登记的完整主路径尝试，任一步失败均计失败  /  | FR-14、N-31、M-03、M-04、M-05、M-10 |
| C-0079 | [requirement.md](../requirement.md) L135 · 3.3.1 指标数据合同与冻结门禁 · 正文/条款 |  /  性能  /  使用同一单调时钟记录每个测量的起止事件、条件和样本数；超时/未完成单列，不得从分位数报告中静默删除  /  | FR-14、N-31、M-03、M-04、M-05、M-10 |
| C-0080 | [requirement.md](../requirement.md) L137 · 3.3.1 指标数据合同与冻结门禁 · 正文/条款 | 以下项为 Unknown，由产品负责人和研究执行者在正式采样前写入本节并冻结：盲评双方汇总与平局/缺评规则；动态增量比较基准；完拍时间有效配对与失败处理；D7 窗口、分母与匿名关联流程；长期保存可靠性… | FR-14、N-31、M-03、M-04、M-05、M-10 |
| C-0081 | [requirement.md](../requirement.md) L141 · 3.3.2 本轮数据合同补充（2026-09-12） · 正文/条款 | - 至少 20 名首次用户是当前产品实验下限，不等于统计效力已证明；完整 20人×3场景×3条件为180轮，其中动态60轮，同一参与者/被拍者的重复轮次和双方盲评不是独立用户。记录关联与顺序，不以轮次… | N-11、N-31、M-03、M-04、M-10 |
| C-0082 | [requirement.md](../requirement.md) L142 · 3.3.2 本轮数据合同补充（2026-09-12） · 正文/条款 | - 动态增量的比较基准、双方汇总、平局/缺评与失败时间处理仍按第3.3.1节待冻结；“偏好或时间”两项都报告，不采样后只展示更有利指标。若作统计推断，须预定关联数据和双路径选择的处理方法。 | N-11、N-31、M-03、M-04、M-10 |
| C-0083 | [requirement.md](../requirement.md) L143 · 3.3.2 本轮数据合同补充（2026-09-12） · 正文/条款 | - 研究资料数据字典须逐类列出 App 非图像事件、研究照片/录像、盲评答卷及匿名关联：字段、目的、来源、访问角色、存放位置、导出责任人、保留期限与删除流程。照片/录像不进入事件日志或 Git；未明确… | N-11、N-31、M-03、M-04、M-10 |
| C-0084 | [requirement.md](../requirement.md) L144 · 3.3.2 本轮数据合同补充（2026-09-12） · 正文/条款 | - 保存的首试成功率、恢复成功率、丢失/重复数分别报告。长期样本量、运行分层、允许失败数、置信目标和停止规则由研究负责人采样前冻结；100张专项及研究报告中的598零失败计算示例均不能自动替代长期验收… | N-11、N-31、M-03、M-04、M-10 |
| C-0085 | [requirement.md](../requirement.md) L145 · 3.3.2 本轮数据合同补充（2026-09-12） · 正文/条款 | - 示例复算必须覆盖超时、主动退出、捕获失败、保存失败后恢复、缺评、平局和D7失访；仅计算公式正确不等于真实指标通过。 | N-11、N-31、M-03、M-04、M-10 |
| C-0086 | [requirement.md](../requirement.md) L149 · 3.4 Go / No-Go · 正文/条款 | - 第 3.3 节全部通过，才把完整 P0 当发布候选。 | N-16 |
| C-0087 | [requirement.md](../requirement.md) L150 · 3.4 Go / No-Go · 正文/条款 | - 动态教练不胜过静态卡：停止扩大实时分析，评估改成更简单的拍照清单产品。 | N-16 |
| C-0088 | [requirement.md](../requirement.md) L151 · 3.4 Go / No-Go · 正文/条款 | - 成片稳定输给系统相机：停止扩教练功能，先修成片链路，或改成不负责拍照的系统相机伴随方案。 | N-16 |
| C-0089 | [requirement.md](../requirement.md) L152 · 3.4 Go / No-Go · 正文/条款 | - D7 自然复用不过线：不得用滤镜、评分、签到或姿势商城补救核心需求不足。 | N-16 |
| C-0090 | [requirement.md](../requirement.md) L153 · 3.4 Go / No-Go · 正文/条款 | - 语音打扰超标：停止默认外放，改为首次选择或默认静音后重新验证。 | N-16 |
| C-0091 | [requirement.md](../requirement.md) L163 · 4.1 范围矩阵 · 正文/条款 |  /  小米 14 Pro 后摄单人  /  是  /  是  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0092 | [requirement.md](../requirement.md) L164 · 4.1 范围矩阵 · 正文/条款 |  /  其他 Android 机型  /  否  /  否  /  否  /  后期评估  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0093 | [requirement.md](../requirement.md) L165 · 4.1 范围矩阵 · 正文/条款 |  /  人物特写 / 人带景  /  是  /  是  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0094 | [requirement.md](../requirement.md) L166 · 4.1 范围矩阵 · 正文/条款 |  /  室内窗边、室外人带景、逆光  /  是  /  是  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0095 | [requirement.md](../requirement.md) L167 · 4.1 范围矩阵 · 正文/条款 |  /  默认两步指导  /  是  /  是  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0096 | [requirement.md](../requirement.md) L168 · 4.1 范围矩阵 · 正文/条款 |  /  手动快门与真实保存  /  是  /  是  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0097 | [requirement.md](../requirement.md) L169 · 4.1 范围矩阵 · 正文/条款 |  /  前摄自拍  /  否  /  是  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0098 | [requirement.md](../requirement.md) L170 · 4.1 范围矩阵 · 正文/条款 |  /  只拍景、建筑、街拍  /  否  /  是  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0099 | [requirement.md](../requirement.md) L171 · 4.1 范围矩阵 · 正文/条款 |  /  代拍简化界面  /  否  /  是  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0100 | [requirement.md](../requirement.md) L172 · 4.1 范围矩阵 · 正文/条款 |  /  可选“再讲细”  /  否  /  是  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0101 | [requirement.md](../requirement.md) L173 · 4.1 范围矩阵 · 正文/条款 |  /  样片复刻、连拍选最佳、拍后一句原因  /  否  /  否  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0102 | [requirement.md](../requirement.md) L174 · 4.1 范围矩阵 · 正文/条款 |  /  专业参数建议、12 种稳定风格、非破坏编辑与保存  /  否  /  否  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0103 | [requirement.md](../requirement.md) L175 · 4.1 范围矩阵 · 正文/条款 |  /  无声 Android Motion Photo（Live）  /  否  /  否  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0104 | [requirement.md](../requirement.md) L176 · 4.1 范围矩阵 · 正文/条款 |  /  六类姿势库与摄影技巧建议  /  否  /  否  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0105 | [requirement.md](../requirement.md) L177 · 4.1 范围矩阵 · 正文/条款 |  /  场景化风格推荐、强度、最近与收藏  /  否  /  否  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0106 | [requirement.md](../requirement.md) L178 · 4.1 范围矩阵 · 正文/条款 |  /  人像保守强度、发布后校验与热降级  /  否  /  否  /  是  /  是  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0107 | [requirement.md](../requirement.md) L179 · 4.1 范围矩阵 · 正文/条款 |  /  独立默认关闭的自然上镜纹理平滑  /  否  /  否  /  单独批准，限第 4.6 节  /  后续另评估  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0108 | [requirement.md](../requirement.md) L180 · 4.1 范围矩阵 · 正文/条款 |  /  合影  /  否  /  否  /  否  /  见合影后期范围  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0109 | [requirement.md](../requirement.md) L181 · 4.1 范围矩阵 · 正文/条款 |  /  被拍者主用户 / 互拍切换  /  否  /  否  /  否  /  见角色切换后期范围  /  | N-10、N-33、N-41、N-42、N-43 |
| C-0110 | [requirement.md](../requirement.md) L185 · 4.2 P-1 只包含 · 正文/条款 | - 小米 14 Pro 后摄，一张脸。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0111 | [requirement.md](../requirement.md) L186 · 4.2 P-1 只包含 · 正文/条款 | - 人物特写、人物带景两个意图。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0112 | [requirement.md](../requirement.md) L187 · 4.2 P-1 只包含 · 正文/条款 | - 室内窗边、室外人带景、逆光人像三个场景包。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0113 | [requirement.md](../requirement.md) L188 · 4.2 P-1 只包含 · 正文/条款 | - 点按对焦、EV、小米 14 Pro 上经真机确认的快捷焦段、闪光状态、中文语音/被拍者字幕提示方式、屏幕快门和音量键快门。焦段名称以真机报告和实拍标定为准，不预设为 2x。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0114 | [requirement.md](../requirement.md) L189 · 4.2 P-1 只包含 · 正文/条款 | - 基础拍摄设置：AE/AF 长按锁定与可见解锁、三分网格、水平仪、关闭/3 秒/10 秒倒计时、4:3/16:9 画幅、画质优先/速度优先、设置持久化与一键恢复默认。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0115 | [requirement.md](../requirement.md) L190 · 4.2 P-1 只包含 · 正文/条款 | - 拍摄模式、快捷焦段、连续变焦和曝光补偿只展示标准 CameraX/Camera2 在当前后摄实际报告的能力；不支持或切换失败时回到普通 Photo，并保留实时指导。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0116 | [requirement.md](../requirement.md) L191 · 4.2 P-1 只包含 · 正文/条款 | - 一个拍摄者动作 + 一个被拍者动作；用户可跳过，快门始终可用。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0117 | [requirement.md](../requirement.md) L192 · 4.2 P-1 只包含 · 正文/条款 | - 端侧分析、系统/标准成片回退、写入系统相册、无水印、飞行模式可用。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0118 | [requirement.md](../requirement.md) L193 · 4.2 P-1 只包含 · 正文/条款 | - 一次性端侧人脸/身体关键点说明；只申请相机权限。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0119 | [requirement.md](../requirement.md) L194 · 4.2 P-1 只包含 · 正文/条款 | - 研究版的非图像事件记录，用于计算第 3.3 节指标。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0120 | [requirement.md](../requirement.md) L196 · 4.2 P-1 只包含 · 正文/条款 | P-1 不公开上架、不登录、不收费，不做前摄、只拍景、建筑/街拍、代拍、云端讲解、姿势商城、评分、自动拍、拍后课程。 | N-10、FR-09、FR-19、FR-20、FR-21 |
| C-0121 | [requirement.md](../requirement.md) L200 · 4.3 完整 P0 · 正文/条款 | 前置条件：P-1 全部门槛通过。 | FR-15、FR-16、FR-17 |
| C-0122 | [requirement.md](../requirement.md) L202 · 4.3 完整 P0 · 正文/条款 | 在 P-1 基础上增加： | FR-15、FR-16、FR-17 |
| C-0123 | [requirement.md](../requirement.md) L204 · 4.3 完整 P0 · 正文/条款 | - 前后摄和“只拍景”意图。 | FR-15、FR-16、FR-17 |
| C-0124 | [requirement.md](../requirement.md) L205 · 4.3 完整 P0 · 正文/条款 | - P0 人像、自拍、旅行、建筑和街拍场景。 | FR-15、FR-16、FR-17 |
| C-0125 | [requirement.md](../requirement.md) L206 · 4.3 完整 P0 · 正文/条款 | - 单人构图叠线与必要的姿势轮廓。 | FR-15、FR-16、FR-17 |
| C-0126 | [requirement.md](../requirement.md) L207 · 4.3 完整 P0 · 正文/条款 | - 拍摄者一键进入和退出的代拍简化界面。 | FR-15、FR-16、FR-17 |
| C-0127 | [requirement.md](../requirement.md) L208 · 4.3 完整 P0 · 正文/条款 | - 可选“再讲细”；首次上传前单独同意，失败不影响内置指导。 | FR-15、FR-16、FR-17 |
| C-0128 | [requirement.md](../requirement.md) L209 · 4.3 完整 P0 · 正文/条款 | - 小米 14 Pro 前后摄、竖横屏和目标系统版本的完整回归；不扩展其他 Android 机型。 | FR-15、FR-16、FR-17 |
| C-0129 | [requirement.md](../requirement.md) L211 · 4.3 完整 P0 · 正文/条款 | P0 仍不显示分数，不默认自动拍，不锁免费口令，不要求登录。 | FR-15、FR-16、FR-17 |
| C-0130 | [requirement.md](../requirement.md) L215 · 4.4 P1 与后期 · 正文/条款 | P1 和明确不做项见P1 创意功能需求；合影和角色切换见后期功能范围。 | N-33、N-41、N-42 |
| C-0131 | [requirement.md](../requirement.md) L219 · 4.5 P1 第二阶段扩展（ApprovedSeparate） · 正文/条款 | 2026-09-01 将以下能力作为小米 14 Pro 的独立 P1 第二阶段需求登记。它们不进入 P-1 对照实验，不改变完整 P0 的 GateLocked 状态，也不能用代码路径或自动化结果替代… | FR-32、FR-33、FR-34、FR-35、FR-36、FR-37 |
| C-0132 | [requirement.md](../requirement.md) L221 · 4.5 P1 第二阶段扩展（ApprovedSeparate） · 正文/条款 | - 特写、半身、全身、坐姿、走动和单人互动六类姿势库；每条口令必须有可观察触发、退出条件、低置信度回退和明确的自动完成边界。两张脸以上继续禁止单人姿势匹配。 | FR-32、FR-33、FR-34、FR-35、FR-36、FR-37 |
| C-0133 | [requirement.md](../requirement.md) L222 · 4.5 P1 第二阶段扩展（ApprovedSeparate） · 正文/条款 | - 构图、关节裁切、用光、焦段/距离、背景、夜景、运动和多人安全回退等摄影技巧；只建议当前公开能力和画面证据可执行的动作，不伪造机位高度、光学焦段或场景理解。 | FR-32、FR-33、FR-34、FR-35、FR-36、FR-37 |
| C-0134 | [requirement.md](../requirement.md) L223 · 4.5 P1 第二阶段扩展（ApprovedSeparate） · 正文/条款 | - 在现有十二种风格上增加场景化推荐、强度建议、最近使用和收藏；原图始终第一且不被覆盖，推荐不得自动套用。 | FR-32、FR-33、FR-34、FR-35、FR-36、FR-37 |
| C-0135 | [requirement.md](../requirement.md) L224 · 4.5 P1 第二阶段扩展（ApprovedSeparate） · 正文/条款 | - 人像场景只允许保守限制全局风格强度和检查脸部区域前后变化；在完成脸颊采样、肤色范围、受控光照与目标机样张验收前，不宣称局部“肤色保护”。 | FR-32、FR-33、FR-34、FR-35、FR-36、FR-37 |
| C-0136 | [requirement.md](../requirement.md) L225 · 4.5 P1 第二阶段扩展（ApprovedSeparate） · 正文/条款 | - 面部只输出转头、侧倾、睁眼等可观察动作；不从笑容、皱眉或其他面部运动推断开心、紧张、疲惫、愤怒等情绪，不做身份、年龄、性别或外貌评分。 | FR-32、FR-33、FR-34、FR-35、FR-36、FR-37 |
| C-0137 | [requirement.md](../requirement.md) L226 · 4.5 P1 第二阶段扩展（ApprovedSeparate） · 正文/条款 | - 保存增加 pending/发布后资产校验、方向/EXIF/相册可见性检查和逐阶段幂等恢复；Motion Photo 继续无声、单主文件、SDR JPEG 和普通 JPEG 回退。 | FR-32、FR-33、FR-34、FR-35、FR-36、FR-37 |
| C-0138 | [requirement.md](../requirement.md) L227 · 4.5 P1 第二阶段扩展（ApprovedSeparate） · 正文/条款 | - 连续取景、姿势分析、风格处理、连拍和 Live 接受目标机热状态驱动的分级降载；任何降载都不得取消普通快门、实时预览或已经捕获原片的保存。 | FR-32、FR-33、FR-34、FR-35、FR-36、FR-37 |
| C-0139 | [requirement.md](../requirement.md) L229 · 4.5 P1 第二阶段扩展（ApprovedSeparate） · 正文/条款 | 具体姿势、技巧、触发和退出边界见拍摄交互与指导需求第 11.6 节，风格、保存、Motion Photo 和热策略见P1 创意功能需求第 12.2.1 节，FR/UX 与证据状态分别以功能需求定义与追… | FR-32、FR-33、FR-34、FR-35、FR-36、FR-37 |
| C-0140 | [requirement.md](../requirement.md) L235 · 4.6 P1 自然上镜 v1（ApprovedSeparate） · 正文/条款 | 2026-09-03 用户明确批准自研并要求执行：新增独立默认关闭的自然上镜 OFF / NATURAL / SOFT，仅限当前小米 14 Pro 后摄单人标准 Photo。不解锁 P0，不扩展前摄、… | FR-38、N-40 |
| C-0141 | [requirement.md](../requirement.md) L237 · 4.6 P1 自然上镜 v1（ApprovedSeparate） · 正文/条款 | 这是既有禁止 CameraEffect/局部磨皮规则的有限例外：只允许 PREVIEW-only SurfaceProcessor 局部纹理平滑，以及明确另存/主动双保存产生的 SDR 副本；十二种全… | FR-38、N-40 |
| C-0142 | [requirement.md](../requirement.md) L241 · 5. 跨版本硬约束 · 正文/条款 | 以下规则跨 P-1、P0 与已批准 P1 生效；某阶段未获准时，只保留规则，不因此进入交付范围。 | N-02、N-03、N-06、N-07、N-08、N-09、N-10 |
| C-0143 | [requirement.md](../requirement.md) L243 · 5. 跨版本硬约束 · 正文/条款 | - P-1 主路径只验收小米 14 Pro 后摄单人、人物特写/人带景、三个场景包、一个 shooter 必做动作加一个 subject 必做动作。候选最多三条不等于同屏或必做三步；“再优化一下”最多… | N-02、N-03、N-06、N-07、N-08、N-09、N-10 |
| C-0144 | [requirement.md](../requirement.md) L244 · 5. 跨版本硬约束 · 正文/条款 | - 快门只因相机不可用或正在捕获而暂时禁用；指导、分数、网络和阈值不得锁快门。相机被系统热保护撤回、系统关闭组件或进程时，按真实不可用处理并尽可能保留恢复记录，不承诺硬件始终可用。P1 连拍执行期间防… | N-02、N-03、N-06、N-07、N-08、N-09、N-10 |
| C-0145 | [requirement.md](../requirement.md) L245 · 5. 跨版本硬约束 · 正文/条款 | - 普通相机链路必须同时保留 Preview、ImageAnalysis、ImageCapture；分析只保留最新帧。唯一批准的四用例例外是用户显式开启的无声 P1 Live，失败后可见回退普通 JP… | N-02、N-03、N-06、N-07、N-08、N-09、N-10 |
| C-0146 | [requirement.md](../requirement.md) L246 · 5. 跨版本硬约束 · 正文/条款 | - 快捷焦段只来自标准 CameraX/Camera2 实际能力与小米 14 Pro 真机标定；不得硬编码 2x、把数码裁切称作光学镜头、读取厂商传感器 ID，或把未验证焦段写成通过。 | N-02、N-03、N-06、N-07、N-08、N-09、N-10 |
| C-0147 | [requirement.md](../requirement.md) L247 · 5. 跨版本硬约束 · 正文/条款 | - Face/Pose 只产生端侧构图与口令信号，第 4.6 节另允许复用 Face 数值关键点作本地美颜；禁止身份识别、embedding、人脸库、外貌评分和姿势分。两张脸以上不得进入单人姿势匹配。 | N-02、N-03、N-06、N-07、N-08、N-09、N-10 |
| C-0148 | [requirement.md](../requirement.md) L248 · 5. 跨版本硬约束 · 正文/条款 | - P-1 只申请 CAMERA，不新增登录、麦克风、定位、读取整本相册、后台上传或持续重试。App 管理的研究事件、照片暂存、配方和恢复日志须排除云备份及设备迁移；私有目录本身不等于数据不离开设备。… | N-02、N-03、N-06、N-07、N-08、N-09、N-10 |
| C-0149 | [requirement.md](../requirement.md) L249 · 5. 跨版本硬约束 · 正文/条款 | - 已批准 P1 创意层不得改变 P-1 对照实验和 Go 指标。每次捕获使用不可复用 captureId；默认发布原片、私有保存配方，效果 JPEG 仅在明确另存或用户主动开启自动双保存时生成；分阶… | N-02、N-03、N-06、N-07、N-08、N-09、N-10 |
| C-0150 | [requirement.md](../requirement.md) L250 · 5. 跨版本硬约束 · 正文/条款 | - 已批准 P1 第二阶段姿势与摄影技巧继续服从一次一个主要动作、最多两个必做动作和随时快门；坐姿、走动、互动、肤色保护、复杂表情及 HyperOS Motion Photo 兼容性若缺少目标机证据，… | N-02、N-03、N-06、N-07、N-08、N-09、N-10 |
| C-0151 | [requirement.md](../requirement.md) L251 · 5. 跨版本硬约束 · 正文/条款 | - Scope、Delivery、Verification 必须分别报告。代码路径、自动化通过和设备通过是三个不同事实；Unknown/NotRun 不能写成通过。 | N-02、N-03、N-06、N-07、N-08、N-09、N-10 |
| C-0152 | [requirement.md](../requirement.md) L259 · 10.1 产品与成片约束 · 正文/条款 | - 产品不自研 ISP、RAW、LUT 或生成替换；第 4.6 节独立批准的局部纹理平滑是美颜的唯一例外，P-1/P0 和既有全局风格仍无美颜。 | N-07、N-43 |
| C-0153 | [requirement.md](../requirement.md) L260 · 10.1 产品与成片约束 · 正文/条款 | - 成片优先使用兼容的系统/厂商计算摄影；无法与实时分析共存时回退标准拍照。 | N-07、N-43 |
| C-0154 | [requirement.md](../requirement.md) L261 · 10.1 产品与成片约束 · 正文/条款 | - 口令出现的动作必须能在取景器执行。 | N-07、N-43 |
| C-0155 | [requirement.md](../requirement.md) L262 · 10.1 产品与成片约束 · 正文/条款 | - 自动快门 P-1 不存在；P0 即使实验也默认关闭，并设置最多三张、冷却和立即停止。 | N-07、N-43 |
| C-0156 | [requirement.md](../requirement.md) L263 · 10.1 产品与成片约束 · 正文/条款 | - 第一版只验收小米 14 Pro 的竖屏和横屏。 | N-07、N-43 |
| C-0157 | [requirement.md](../requirement.md) L265 · 10.1 产品与成片约束 · 正文/条款 | 具体 CameraX、ML Kit、扩展兼容和 API 结构以 architecture.md 为准。 | N-07、N-43 |
| C-0158 | [requirement.md](../requirement.md) L269 · 10.2 权限与数据 · 正文/条款 | - P-1 只申请 CAMERA。 | N-08、N-09、N-31、FR-17 |
| C-0159 | [requirement.md](../requirement.md) L270 · 10.2 权限与数据 · 正文/条款 | - 不申请麦克风、定位、联系人或读取整本相册。 | N-08、N-09、N-31、FR-17 |
| C-0160 | [requirement.md](../requirement.md) L271 · 10.2 权限与数据 · 正文/条款 | - 保存只插入本 App 新拍的照片；最近照片只使用本次写入得到的 URI。 | N-08、N-09、N-31、FR-17 |
| C-0161 | [requirement.md](../requirement.md) L272 · 10.2 权限与数据 · 正文/条款 | - 默认只做端侧人脸框、关键点和单人姿势，不做人脸比对、不存身份模板。 | N-08、N-09、N-31、FR-17 |
| C-0162 | [requirement.md](../requirement.md) L273 · 10.2 权限与数据 · 正文/条款 | - P-1 研究事件只记录时间（包括流程起止）、意图、提示 ID、完成/跳过/超时/主动退出、快门、保存结果和匿名会话编号；条件、场景、配置版本与跨会话匿名映射在研究侧按第 3.3.1 节管理。不记录… | N-08、N-09、N-31、FR-17 |
| C-0163 | [requirement.md](../requirement.md) L274 · 10.2 权限与数据 · 正文/条款 | - P0 云端讲解单独同意，单帧按需上传，用完不落库；撤回后不得后台重试。 | N-08、N-09、N-31、FR-17 |
| C-0164 | [requirement.md](../requirement.md) L275 · 10.2 权限与数据 · 正文/条款 | - 上架前以真实安装包核对网络和第三方 SDK 行为，商店数据安全披露必须与实际一致。 | N-08、N-09、N-31、FR-17 |
| C-0165 | [requirement.md](../requirement.md) L277 · 10.2 权限与数据 · 正文/条款 | 涉及人脸和儿童的能力上线前，须按届时有效法规完成专项法律复核和个人信息保护影响评估；不能仅依赖本文中的历史法规名称。 | N-08、N-09、N-31、FR-17 |
| C-0166 | [requirement.md](../requirement.md) L281 · 10.3 可靠性与信任 · 正文/条款 | - 拍中教练主路径免费可用，不能锁意图、基础口令、快门或保存。 | N-32 |
| C-0167 | [requirement.md](../requirement.md) L282 · 10.3 可靠性与信任 · 正文/条款 | - 短口令核与场景特有句全部免费。 | N-32 |
| C-0168 | [requirement.md](../requirement.md) L283 · 10.3 可靠性与信任 · 正文/条款 | - 核心能力不依赖登录。 | N-32 |
| C-0169 | [requirement.md](../requirement.md) L284 · 10.3 可靠性与信任 · 正文/条款 | - 不默认周订阅；若后期收费，优先买断或年付，并明确免费边界。 | N-32 |
| C-0170 | [requirement.md](../requirement.md) L285 · 10.3 可靠性与信任 · 正文/条款 | - 原片无水印，用户能在系统相册找到。 | N-32 |
| C-0171 | [requirement.md](../requirement.md) L286 · 10.3 可靠性与信任 · 正文/条款 | - 黑屏、自动拍停不下和保存失败是信任阻断问题，优先级高于增加场景和姿势。 | N-32 |
| C-0172 | [requirement.md](../requirement.md) L296 · 11. 当前规范附件索引 · 正文/条款 |  /  拍摄交互与指导需求  /  用户角色、页面、取景器、状态、参数、语音/字幕、保存、指导引擎、场景与短口令  /  文内逐项标识 P-1/P0/P1；不得因同文件提前  /  | N-44 |
| C-0173 | [requirement.md](../requirement.md) L297 · 11. 当前规范附件索引 · 正文/条款 |  /  P1 创意功能需求  /  P1 候选、单独批准的创意层、第二阶段强化、第一版非目标、决策变更规则  /  第 12.2 节及其第二阶段强化属于 ApprovedSeparate  /  | N-44 |
| C-0174 | [requirement.md](../requirement.md) L298 · 11. 当前规范附件索引 · 正文/条款 |  /  P1 自然上镜需求（v1）  /  独立美颜开关、保守局部纹理处理、回退与非破坏保存  /  第 4.6 节 ApprovedSeparate，不改变 P-1/P0 或十二风格语义  /  | N-44 |
| C-0175 | [requirement.md](../requirement.md) L299 · 11. 当前规范附件索引 · 正文/条款 |  /  后期功能范围  /  合影、被拍者主用户与互拍方向  /  全部 Deferred；第一版不渲染入口  /  | N-44 |
| C-0176 | [requirement.md](../requirement.md) L300 · 11. 当前规范附件索引 · 正文/条款 |  /  分阶段验收规范  /  UX 唯一定义、目标机矩阵、质量门槛  /  P-1 与已批准 P1 独立；P0 缺口显式  /  | N-44 |
| C-0177 | [requirement.md](../requirement.md) L301 · 11. 当前规范附件索引 · 正文/条款 |  /  功能需求定义与追踪矩阵  /  FR 唯一定义、Phase、UX、组件、自动化/真机证据和状态  /  分列 Scope/Delivery/Verification  /  | N-44 |
| C-0178 | [requirement.md](../requirement.md) L302 · 11. 当前规范附件索引 · 正文/条款 |  /  市场、交互与官方依据  /  证据分级、竞品、官方依据、边界与市场风险  /  Research only，不把依据写成验证通过  /  | N-44 |
| C-0179 | [requirement.md](../requirement.md) L303 · 11. 当前规范附件索引 · 正文/条款 |  /  需求决策与冲突记录  /  未决冲突双方、控制与已决历史  /  未决项保持 ConflictPending；已决项保留历史双方与决议  /  | N-44 |
| C-0180 | [requirement.md](../requirement.md) L304 · 11. 当前规范附件索引 · 正文/条款 |  /  迁移登记  /  旧章节/表格/规则/技术决定到新位置  /  只使用允许的迁移状态  /  | N-44 |
| C-0181 | [requirement.md](../requirement.md) L305 · 11. 当前规范附件索引 · 正文/条款 |  /  验证报告  /  快照、链接、编号、阶段、自动化、Git 范围与残余 NotRun  /  自动化与设备证据分开  /  | N-44 |
| C-0182 | [requirement.md](../requirement.md) L309 · 12. 当前 ConflictPending · 正文/条款 | - CP-01：竖屏底部操作区约五分之一与 UX-15 约四分之一的口径冲突。 | UX-15、N-44 |
| C-0183 | [requirement.md](../requirement.md) L310 · 12. 当前 ConflictPending · 正文/条款 | - CP-01 已同意区分设计目标与最大容差，但测量分母和具体阈值仍未确定，继续保持 ConflictPending。 | UX-15、N-44 |
| C-0184 | [requirement.md](../requirement.md) L312 · 12. 当前 ConflictPending · 正文/条款 | CP-03、CP-04、CP-05 于 2026-09-05 按用户同意的修订方案解决：参数建议采用主动打开的独立面板；口令数量由覆盖推导；非目标设备检查为非阻断健壮性检查。决议不等于实现或验收通过。… | UX-15、N-44 |
| C-0185 | [requirement.md](../requirement.md) L314 · 12. 当前 ConflictPending · 正文/条款 | CP-02 已于 2026-09-01 按用户要求解决：可靠识别人但未达到最低可拍条件时使用“随时可拍”，未可靠识别人时使用恢复口令，只有最低条件成立才使用“可以拍了”。完整历史双方、决议、影响和其余… | UX-15、N-44 |
| C-0186 | [requirement.md](../requirement.md) L318 · 13. 文档维护 · 正文/条款 | - 产品总纲、范围矩阵、P-1 假设、Go/No-Go、跨版本硬约束、隐私和附件权威关系：修改本入口。 | N-44 |
| C-0187 | [requirement.md](../requirement.md) L319 · 13. 文档维护 · 正文/条款 | - 交互、指导、场景、P1/后期细则：修改相应规范附件，并同步检查入口是否受影响。 | N-44 |
| C-0188 | [requirement.md](../requirement.md) L320 · 13. 文档维护 · 正文/条款 | - FR/UX 变更只在各自唯一权威文件修改，同时更新追踪矩阵；不得复制第二个权威定义。 | N-44 |
| C-0189 | [requirement.md](../requirement.md) L321 · 13. 文档维护 · 正文/条款 | - 技术选型、模块、数据结构、依赖方向和回退：修改架构入口及对应专题。 | N-44 |
| C-0190 | [requirement.md](../requirement.md) L322 · 13. 文档维护 · 正文/条款 | - 每次需求变更须同步检查范围矩阵、FR、UX、架构引用、迁移/决策记录和验证状态。 | N-44 |
| C-0191 | [requirement.md](../requirement.md) L323 · 13. 文档维护 · 正文/条款 | - 跨任务长期有效的编码规则只能经过独立审查后修改 Skill；文档路由变化本身不授权修改 Skill。 | N-44 |
| C-0192 | [requirement.md](../requirement.md) L324 · 13. 文档维护 · 正文/条款 | - 下一步仍是制作和验证 P-1 可用原型并执行三组用户对照测试，不是直接开发完整 P0。 | N-44 |
| C-0193 | [requirement.md](../requirement.md) L326 · 13. 文档维护 · 正文/条款 | 2026-09-12：按用户要求先建立逐项审核矩阵，再完成能力语义、离线/备份、持久恢复及热边界等文档优化；详见修订记录。仅文档修订，新增合同实现符合性 Unknown、动态验证 NotRun，CP-… | N-44 |

## docs/requirements/interaction-guidance-and-scenarios.md

| 单元 | 原文位置 | 摘录（非权威副本） | 逐项审核ID |
| --- | --- | --- | --- |
| C-0194 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L3 · 拍摄交互与指导需求 · 正文/条款 | - 状态：当前规范附件 | N-44 |
| C-0195 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L4 · 拍摄交互与指导需求 · 正文/条款 | - 权威入口：产品需求总纲 | N-44 |
| C-0196 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L5 · 拍摄交互与指导需求 · 正文/条款 | - 原始位置：重构前第 2、5、6、8、11 节 | N-44 |
| C-0197 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L6 · 拍摄交互与指导需求 · 正文/条款 | - 迁移方式：Moved；规则、表格、阈值、例外和文案完整保留 | N-44 |
| C-0198 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L8 · 拍摄交互与指导需求 · 正文/条款 | 阶段标签具有约束力：P-1、P0、P1 与后期内容不得互相提前。尚未拍板的矛盾由 需求决策与冲突记录 控制，不能用本附件中任一处静默覆盖另一处。 | N-44 |
| C-0199 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L16 · 2.1 首个目标用户 · 正文/条款 | 旅行、约会或日常出门时，拿小米 14 Pro 帮伴侣或朋友拍单人照的拍照新手。他会用系统相机，但不会选机位、控制人物与环境的比例，也不知道怎样开口指导姿势。 | N-01 |
| C-0200 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L18 · 2.1 首个目标用户 · 正文/条款 | Pix、SnapStance 等产品也直接面向伴侣帮拍。因此这只是容易验证的首个任务，不是无人竞争的空白市场。 | N-01 |
| C-0201 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L22 · 2.2 核心任务 · 正文/条款 | 当用户想拍“人物特写”或“人带景”时，App 应在不要求摄影术语、不依赖网络的前提下： | N-01、N-03 |
| C-0202 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L24 · 2.2 核心任务 · 正文/条款 | 1. 让用户快速确认想拍什么。 | N-01、N-03 |
| C-0203 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L25 · 2.2 核心任务 · 正文/条款 | 2. 只给最重要的一个拍摄者动作。 | N-01、N-03 |
| C-0204 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L26 · 2.2 核心任务 · 正文/条款 | 3. 再给一个被拍者动作。 | N-01、N-03 |
| C-0205 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L27 · 2.2 核心任务 · 正文/条款 | 4. 让用户随时拍下并可靠保存照片。 | N-01、N-03 |
| C-0206 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L33 · 2.3 角色 · 正文/条款 |  /  shooter  /  当前拿手机、看屏幕并按快门的人  /  主用户  /  主用户  /  | N-01、N-42、FR-16 |
| C-0207 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L34 · 2.3 角色 · 正文/条款 |  /  subject  /  画面中的被拍者，主要听语音口令  /  支持一人  /  支持一人  /  | N-01、N-42、FR-16 |
| C-0208 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L35 · 2.3 角色 · 正文/条款 |  /  proxy  /  临时接过手机代拍的路人或朋友  /  不支持  /  简化界面  /  | N-01、N-42、FR-16 |
| C-0209 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L37 · 2.3 角色 · 正文/条款 | 角色字段是内部组织方式，不是产品护城河。用户界面不显示 shooter、subject、proxy 这些技术名称。 | N-01、N-42、FR-16 |
| C-0210 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L41 · 2.4 非首个目标用户 · 正文/条款 | - 主要自拍且愿意浏览大量姿势模板的人。 | N-01、N-43 |
| C-0211 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L42 · 2.4 非首个目标用户 · 正文/条款 | - 需要 RAW、直方图和完整手动参数的摄影爱好者。 | N-01、N-43 |
| C-0212 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L43 · 2.4 非首个目标用户 · 正文/条款 | - 以美颜、滤镜或 AI 重绘为目的的人。 | N-01、N-43 |
| C-0213 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L44 · 2.4 非首个目标用户 · 正文/条款 | - 多人合影组织者。 | N-01、N-43 |
| C-0214 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L45 · 2.4 非首个目标用户 · 正文/条款 | - 需要单反、微单机身或镜头数据库的人。 | N-01、N-43 |
| C-0215 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L55 · 5.1 页面 · 正文/条款 | P-1 只有三个用户可感知页面： | N-17 |
| C-0216 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L59 · 5.1 页面 · 正文/条款 |  /  首次说明  /  说明端侧人脸/身体关键点分析并取得同意  /  登录、订阅、课程轮播  /  | N-17 |
| C-0217 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L60 · 5.1 页面 · 正文/条款 |  /  教练取景器  /  确认意图、显示当前动作、拍照  /  模板信息流、评分、多个同时滚动的建议  /  | N-17 |
| C-0218 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L61 · 5.1 页面 · 正文/条款 |  /  保存反馈  /  告知保存成功，或提供可恢复的失败操作  /  强制编辑、强制分享、付费墙  /  | N-17 |
| C-0219 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L63 · 5.1 页面 · 正文/条款 | 后续启动在已同意且权限有效时直接进入取景器，不设置首页。 | N-17 |
| C-0220 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L67 · 5.2 首次进入 · 流程上下文 | 打开 App | N-17、FR-01 |
| C-0221 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L68 · 5.2 首次进入 · 流程上下文 | ↓ | N-17、FR-01 |
| C-0222 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L69 · 5.2 首次进入 · 流程上下文 | 端侧分析说明 | N-17、FR-01 |
| C-0223 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L70 · 5.2 首次进入 · 流程上下文 | ├─ 暂不使用 → 退出，不申请权限 | N-17、FR-01 |
| C-0224 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L71 · 5.2 首次进入 · 流程上下文 | └─ 同意并继续 | N-17、FR-01 |
| C-0225 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L72 · 5.2 首次进入 · 流程上下文 | ↓ | N-17、FR-01 |
| C-0226 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L73 · 5.2 首次进入 · 流程上下文 | 系统相机权限 | N-17、FR-01 |
| C-0227 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L74 · 5.2 首次进入 · 流程上下文 | ├─ 拒绝 → 解释用途 + “前往系统设置” | N-17、FR-01 |
| C-0228 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L75 · 5.2 首次进入 · 流程上下文 | └─ 允许 → 教练取景器 | N-17、FR-01 |
| C-0229 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L77 · 5.2 首次进入 · 正文/条款 | 首次说明必须在系统 CAMERA 权限弹窗之前出现。文案说清： | N-17、FR-01 |
| C-0230 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L79 · 5.2 首次进入 · 正文/条款 | - 本机分析人脸和身体关键点，只用于构图与短口令。 | N-17、FR-01 |
| C-0231 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L80 · 5.2 首次进入 · 正文/条款 | - 不识别“这是谁”，不建立人脸库。 | N-17、FR-01 |
| C-0232 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L81 · 5.2 首次进入 · 正文/条款 | - P-1 不上传取景画面。 | N-17、FR-01 |
| C-0233 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L82 · 5.2 首次进入 · 正文/条款 | - 不同意则不进入取景器。 | N-17、FR-01 |
| C-0234 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L86 · 5.3 P-1 主流程 · 流程上下文 | 取景器可操作 | N-03、N-05、N-23 |
| C-0235 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L87 · 5.3 P-1 主流程 · 流程上下文 | ↓ | N-03、N-05、N-23 |
| C-0236 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L88 · 5.3 P-1 主流程 · 流程上下文 | 顶部轻量选择“人物特写 / 人带景” | N-03、N-05、N-23 |
| C-0237 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L89 · 5.3 P-1 主流程 · 流程上下文 | ↓ | N-03、N-05、N-23 |
| C-0238 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L90 · 5.3 P-1 主流程 · 流程上下文 | 最重要的拍摄者动作 1/2（醒目显示；语音开启时也播报） | N-03、N-05、N-23 |
| C-0239 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L91 · 5.3 P-1 主流程 · 流程上下文 | ├─ 信号改善 → 自动完成 | N-03、N-05、N-23 |
| C-0240 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L92 · 5.3 P-1 主流程 · 流程上下文 | ├─ 跳过 → 进入下一步 | N-03、N-05、N-23 |
| C-0241 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L93 · 5.3 P-1 主流程 · 流程上下文 | └─ 任意时刻按快门 → 直接拍 | N-03、N-05、N-23 |
| C-0242 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L94 · 5.3 P-1 主流程 · 流程上下文 | ↓ | N-03、N-05、N-23 |
| C-0243 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L95 · 5.3 P-1 主流程 · 流程上下文 | 被拍者动作 2/2（按设置显示字幕 / 播放中文语音） | N-03、N-05、N-23 |
| C-0244 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L96 · 5.3 P-1 主流程 · 流程上下文 | ├─ 口令完成或动作失效 → 按最低可拍条件显示“可以拍了 / 随时可拍” | N-03、N-05、N-23 |
| C-0245 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L97 · 5.3 P-1 主流程 · 流程上下文 | ├─ 跳过 → “随时可拍”；画面已满足最低条件时可显示“可以拍了” | N-03、N-05、N-23 |
| C-0246 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L98 · 5.3 P-1 主流程 · 流程上下文 | └─ 任意时刻按快门 → 直接拍 | N-03、N-05、N-23 |
| C-0247 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L99 · 5.3 P-1 主流程 · 流程上下文 | ↓ | N-03、N-05、N-23 |
| C-0248 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L100 · 5.3 P-1 主流程 · 流程上下文 | “可以拍了 / 随时可拍” + 常驻快门 | N-03、N-05、N-23 |
| C-0249 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L101 · 5.3 P-1 主流程 · 流程上下文 | ├─ 再优化一下 → 最多补一条可选建议 | N-03、N-05、N-23 |
| C-0250 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L102 · 5.3 P-1 主流程 · 流程上下文 | └─ 按快门 → 保存反馈 | N-03、N-05、N-23 |
| C-0251 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L104 · 5.3 P-1 主流程 · 正文/条款 | 新一张照片开始时重置两步预算。这里的“拍摄轮次”从本张开始到捕获/结束；“相机会话”是用户进入取景器后连续拍摄的上下文，直到退出取景器或进程结束。短暂前后台和镜头/画幅/模式重绑不单独重置会话。手选意… | N-03、N-05、N-23 |
| C-0252 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L108 · 5.4 取景器布局 · 正文/条款 | 竖屏采用稳定的“预览区 + 底部操作区”，指导卡不在画面中追着人脸移动： | N-18、UX-15、UX-32 |
| C-0253 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L110 · 5.4 取景器布局 · 流程上下文 | ┌──────────────────────────┐ | N-18、UX-15、UX-32 |
| C-0254 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L111 · 5.4 取景器布局 · 流程上下文 | │ 退出   人物特写｜人带景  提示方式/设置/闪光 │ | N-18、UX-15、UX-32 |
| C-0255 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L112 · 5.4 取景器布局 · 流程上下文 | │                          │ | N-18、UX-15、UX-32 |
| C-0256 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L113 · 5.4 取景器布局 · 流程上下文 | │        相机预览区         │ | N-18、UX-15、UX-32 |
| C-0257 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L114 · 5.4 取景器布局 · 流程上下文 | │   构图线、水平线、对焦点   │ | N-18、UX-15、UX-32 |
| C-0258 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L115 · 5.4 取景器布局 · 流程上下文 | │                          │ | N-18、UX-15、UX-32 |
| C-0259 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L116 · 5.4 取景器布局 · 流程上下文 | ├──────────────────────────┤ | N-18、UX-15、UX-32 |
| C-0260 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L117 · 5.4 取景器布局 · 流程上下文 | │ 1/2  走近一步  跳过/再优化 │ | N-18、UX-15、UX-32 |
| C-0261 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L118 · 5.4 取景器布局 · 流程上下文 | │ 1x  已验收长焦  ●快门  最近照片 │ | N-18、UX-15、UX-32 |
| C-0262 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L119 · 5.4 取景器布局 · 流程上下文 | └──────────────────────────┘ | N-18、UX-15、UX-32 |
| C-0263 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L121 · 5.4 取景器布局 · 正文/条款 | - 竖屏预览使用居中填充并延伸到底部操作区背后，不得因 4:3 / 16:9 预览与长屏比例不同而在操作区上方留下大块黑色空带；允许为填满屏幕裁掉预览边缘，但分析坐标和成片裁切必须通过同一 Camer… | N-18、UX-15、UX-32 |
| C-0264 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L122 · 5.4 取景器布局 · 正文/条款 | - 底部操作区以两排为主，高度稳定且紧凑，不因提示字数跳动；默认字号下不得占用超过约五分之一的屏幕高度。 | N-18、UX-15、UX-32 |
| C-0265 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L123 · 5.4 取景器布局 · 正文/条款 | - 当前动作最多两行；不能遮挡中央取景区域。 | N-18、UX-15、UX-32 |
| C-0266 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L124 · 5.4 取景器布局 · 正文/条款 | - 横屏时把底部操作区改成右侧操作栏，按钮顺序保持一致。 | N-18、UX-15、UX-32 |
| C-0267 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L125 · 5.4 取景器布局 · 正文/条款 | - 已可靠识别人但没有建议时结束观察；只有满足最低可拍条件才显示“可以拍了”，否则显示“随时可拍”，不显示无限旋转的“AI 思考中”。 | N-18、UX-15、UX-32 |
| C-0268 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L135 · 6.1 取景器组件 · 正文/条款 |  /  意图切换  /  顶部只显示“人物特写 / 人带景”；自动预选但不弹全屏问卷  /  用户手选后本次拍摄会话不再被自动覆盖  /  | FR-04 |
| C-0269 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L136 · 6.1 取景器组件 · 正文/条款 |  /  当前动作卡  /  固定在操作区，一次一个动作，使用高对比底色和粗体显示 1/2 或 2/2  /  不与第二条建议同时出现；最多两行；默认设置下不得只显示低对比“可以拍了”  /  | FR-07 |
| C-0270 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L137 · 6.1 取景器组件 · 正文/条款 |  /  跳过  /  当前动作始终可跳过  /  一次点击进入下一状态，不弹确认  /  | FR-07 |
| C-0271 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L138 · 6.1 取景器组件 · 正文/条款 |  /  可以拍了 / 随时可拍  /  两步结束、两步均跳过或无高置信度建议时显示；单人、最小主体比例、无严重切边、脸部可见、对焦和水平等最低条件成立才使用“可以拍了”，其余使用“随时可拍”  /  … | FR-07 |
| C-0272 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L139 · 6.1 取景器组件 · 正文/条款 |  /  再优化一下  /  只在“可以拍了”后出现，每张照片最多补一条候选；实时姿势变化产生稳定可选候选时，动作卡改为“发现新建议，可再优化”  /  不算必做步骤；补完后入口隐藏；用户仍可直接拍  … | FR-07 |
| C-0273 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L140 · 6.1 取景器组件 · 正文/条款 |  /  提示方式  /  顶部显示“语音+字幕 / 仅语音 / 仅字幕 / 提示关闭”；点按后分别控制当前口令中文语音和被拍者字幕  /  两个开关独立、立即生效并跨启动保持；拍摄者动作卡不受字幕开关… | FR-08 |
| C-0274 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L141 · 6.1 取景器组件 · 正文/条款 |  /  点按对焦  /  点预览区对焦并显示焦点框  /  对焦失败有可见反馈；不会触发快门。点脸命中区域与铺满预览使用相同裁切；只有相机成功回调才确认本轮点脸测光已执行，失败不算完成。暗脸仍存在时保… | N-19 |
| C-0275 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L142 · 6.1 取景器组件 · 正文/条款 |  /  AE/AF 锁定  /  长按发起当前相机支持的 AF/AE 锁定，分别显示请求中、已确认、不支持或失败；只有两项均确认才显示“对焦和曝光已锁定”，可一键解除  /  测光点已更新或自动取消已… | FR-19 |
| C-0276 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L143 · 6.1 取景器组件 · 正文/条款 |  /  EV  /  常驻「调亮」入口可打开曝光滑杆并保持到主动收起；点按对焦仍可短时显示。滑杆上下限和步长取当前相机 ExposureState；向亮侧拖动增加曝光，可重置；用户选择跨连续拍摄保留，… | N-20 |
| C-0277 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L144 · 6.1 取景器组件 · 正文/条款 |  /  快捷焦段  /  只显示小米 14 Pro 经 CameraX/Camera2 能力查询和实拍确认的 1x、长焦等焦段  /  标签与真机实际视角一致；不得把裁切倍率冒充独立光学镜头，也不预设… | FR-18 |
| C-0278 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L145 · 6.1 取景器组件 · 正文/条款 |  /  手动缩放  /  用户可在预览区双指连续放大或缩小，范围以 CameraX 报告能力为准  /  手动缩放不增加虚假光学焦段按钮，口令仍不建议数码猛拉  /  | UX-13 |
| C-0279 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L146 · 6.1 取景器组件 · 正文/条款 |  /  可用模式  /  “普通”始终可用；人像/HDR/夜景只在扩展可用且支持 ImageAnalysis 时出现在模式选择中  /  运行时或绑定失败立即回到普通拍照并说明原因；不得为扩展卸载分析… | UX-23 |
| C-0280 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L147 · 6.1 取景器组件 · 正文/条款 |  /  构图辅助  /  三分网格与水平仪分别开关，默认开启  /  设置立即生效并跨启动保持；关闭后不再绘制对应叠线  /  | UX-19 |
| C-0281 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L148 · 6.1 取景器组件 · 正文/条款 |  /  倒计时  /  关闭、3 秒、10 秒三档；屏幕和音量键共用  /  大号数字可见；倒计时中再次按快门立即取消；不得演变成自动连拍  /  | UX-20 |
| C-0282 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L149 · 6.1 取景器组件 · 正文/条款 |  /  画幅  /  4:3 默认，可选 16:9；Preview、Analysis、Capture 使用同一画幅策略重绑  /  切换失败有恢复路径，不允许只裁 UI 而成片画幅不变  /  | UX-21 |
| C-0283 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L150 · 6.1 取景器组件 · 正文/条款 |  /  拍摄偏好  /  默认“画质优先”，可选“速度优先”  /  前者使用 CameraX 质量优先捕获，后者使用低延迟捕获；这是画质与延迟取舍，不承诺等到合焦才拍；两者都不锁快门或停分析  /  | FR-20 |
| C-0284 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L151 · 6.1 取景器组件 · 正文/条款 |  /  相机设置  /  汇总提示通道、构图辅助、倒计时、画幅、拍摄偏好和能力驱动模式  /  新增设置跨启动保持；“一键恢复默认”不撤回端侧分析同意，也不删除已拍照片  /  | FR-21 |
| C-0285 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L152 · 6.1 取景器组件 · 正文/条款 |  /  闪光  /  顶部显示状态，默认关；P-1 允许关/自动  /  内置口令不要求开启闪光  /  | N-21 |
| C-0286 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L153 · 6.1 取景器组件 · 正文/条款 |  /  快门  /  底部主按钮，除正在捕获和相机不可用外始终可按  /  不受指导、分数或网络状态限制  /  | FR-11 |
| C-0287 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L154 · 6.1 取景器组件 · 正文/条款 |  /  音量键  /  等同屏幕快门  /  不改变系统音量后再补拍  /  | N-22 |
| C-0288 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L155 · 6.1 取景器组件 · 正文/条款 |  /  最近照片  /  保存成功后显示本 App 刚拍照片的缩略图；点击交给系统图片查看器  /  只使用本次写入得到的 URI，不申请读取整本相册  /  | UX-14 |
| C-0289 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L161 · 6.2 交互状态 · 正文/条款 |  /  初始化  /  预览尽快出现；操作区显示“正在准备相机”  /  相机可用后进入观察画面  /  | N-23、N-24、N-25、N-26 |
| C-0290 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L162 · 6.2 交互状态 · 正文/条款 |  /  观察画面  /  快门可按；操作区短暂显示“正在观察画面”  /  1.5 秒内有高置信度候选则进入 1/2；明确未看清单人人物时显示“请露出脸，或靠近一点”，不能误报“可以拍了”；人物已可靠… | N-23、N-24、N-25、N-26 |
| C-0291 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L163 · 6.2 交互状态 · 正文/条款 |  /  指导 1/2  /  只显示 shooter 动作  /  信号改善稳定、用户跳过、发生替换条件或用户拍照  /  | N-23、N-24、N-25、N-26 |
| C-0292 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L164 · 6.2 交互状态 · 正文/条款 |  /  指导 2/2  /  按设置显示 subject 字幕和播放中文语音  /  动作完成/失效、提示通道完成、用户跳过或用户拍照  /  | N-23、N-24、N-25、N-26 |
| C-0293 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L165 · 6.2 交互状态 · 正文/条款 |  /  可以拍了 / 随时可拍  /  突出快门，仅“可以拍了”提供“再优化一下”；继续分析画面，不保留已经完成或失效的旧口令  /  用户拍照、请求一条附加建议，或严重成片问题触发替换  /  | N-23、N-24、N-25、N-26 |
| C-0294 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L166 · 6.2 交互状态 · 正文/条款 |  /  附加建议  /  只显示第三条，不出现 3/3 的必做感  /  语音/提示完成或跳过后重新执行统一结束判定；拍照进入捕获  /  | N-23、N-24、N-25、N-26 |
| C-0295 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L167 · 6.2 交互状态 · 正文/条款 |  /  捕获中  /  快门短暂禁用，显示明确的捕获反馈  /  保存成功或保存失败  /  | N-23、N-24、N-25、N-26 |
| C-0296 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L168 · 6.2 交互状态 · 正文/条款 |  /  倒计时  /  预览和实时指导继续，大号显示剩余秒数，快门文案变为“停”  /  到零进入捕获；再次按屏幕或音量键快门则取消并恢复  /  | N-23、N-24、N-25、N-26 |
| C-0297 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L169 · 6.2 交互状态 · 正文/条款 |  /  保存成功  /  不弹保存确认页；直接发布照片，并显示勾选、缩略图和“已保存到系统相册”  /  1.5 秒后回到新一轮取景；用户也可立即继续  /  | N-23、N-24、N-25、N-26 |
| C-0298 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L170 · 6.2 交互状态 · 正文/条款 |  /  保存失败  /  保留错误条和“重试保存”  /  重试成功，或用户明确放弃后返回取景  /  | N-23、N-24、N-25、N-26 |
| C-0299 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L171 · 6.2 交互状态 · 正文/条款 |  /  相机错误  /  显示原因、重试和系统设置入口  /  恢复成功后回到取景器；不能停在纯黑屏  /  | N-23、N-24、N-25、N-26 |
| C-0300 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L173 · 6.2 交互状态 · 正文/条款 | 状态切换要求： | N-23、N-24、N-25、N-26 |
| C-0301 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L175 · 6.2 交互状态 · 正文/条款 | 统一结束判定适用于两步结束、跳过、超时、通道播放完成、双通道关闭、无候选和附加建议结束；拍照事件优先进入捕获。先处理相机/保存错误；检测到多人时进入多人安全回退；Face/Pose 均不可靠时显示恢复… | N-23、N-24、N-25、N-26 |
| C-0302 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L177 · 6.2 交互状态 · 正文/条款 | 最低条件包括单人、主体比例、边界余量、脸部可见、对焦、水平及严重曝光/运动风险。每项须定义信号来源、分析裁切/旋转坐标、几何度量、有效期、阈值及适用意图；缺失、过期或无法确认按 Unknown 处理。… | N-23、N-24、N-25、N-26 |
| C-0303 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L179 · 6.2 交互状态 · 正文/条款 | - 可机器判断的 shooter 动作，目标信号改善并稳定至少 500ms 后完成。 | N-23、N-24、N-25、N-26 |
| C-0304 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L180 · 6.2 交互状态 · 正文/条款 | - shooter 动作展示 8 秒仍未改善时，不判失败，自动进入下一步；用户仍可随时跳过或拍照。 | N-23、N-24、N-25、N-26 |
| C-0305 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L181 · 6.2 交互状态 · 正文/条款 | - subject 动作不是姿势闯关；中文语音播放完成后 1 秒，或仅字幕展示满 2.5 秒，流程结束并执行统一结束判定，仍按实时信号判断该动作是否有效。 | N-23、N-24、N-25、N-26 |
| C-0306 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L182 · 6.2 交互状态 · 正文/条款 | - 被拍者字幕不因进入“可以拍了”或经过固定时长自动消失；只在动作完成、动作失效、新口令替换、用户跳过/关闭字幕/拍照、切换意图或开始新一轮时清除。 | N-23、N-24、N-25、N-26 |
| C-0307 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L183 · 6.2 交互状态 · 正文/条款 | - 画面信号持续重算；当前口令条件消失或候选改变后，须重新满足候选稳定条件才清除或替换，不能因单帧抖动跳字。 | N-23、N-24、N-25、N-26 |
| C-0308 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L184 · 6.2 交互状态 · 正文/条款 | - 两个被拍者提示通道都关闭时，跳过 subject 输出并执行统一结束判定；不得出现无文字、无语音的空等候。 | N-23、N-24、N-25、N-26 |
| C-0309 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L185 · 6.2 交互状态 · 正文/条款 | - 普通有效动作展示至少 1.5 秒；同一句至少 8 秒不重复外放。相机错误、可靠多人证据或当前动作必要证据过期/失效时，停止无效姿势及其语音，不为凑满最短展示继续误导。普通候选变化仍按稳定门槛处理。 | N-23、N-24、N-25、N-26 |
| C-0310 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L186 · 6.2 交互状态 · 正文/条款 | - 5 秒内不得给出方向相反的动作。 | N-23、N-24、N-25、N-26 |
| C-0311 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L187 · 6.2 交互状态 · 正文/条款 | - 严重过曝、主体切边或镜头遮挡可以替换当前动作，但不能把必做预算增加到三步。 | N-23、N-24、N-25、N-26 |
| C-0312 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L188 · 6.2 交互状态 · 正文/条款 | - 镜头遮挡/脏污提示须以连续多帧极暗且低细节为必要条件，文案使用“可能”并让用户检查；普通暗景、纯色墙或单帧抖动不得直接断言镜头脏。 | N-23、N-24、N-25、N-26 |
| C-0313 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L189 · 6.2 交互状态 · 正文/条款 | - 相机、权限、保存等系统错误不计入两步预算。 | N-23、N-24、N-25、N-26 |
| C-0314 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L193 · 6.3 意图操作 · 正文/条款 | P-1 只允许两个意图： | FR-04、FR-05、FR-15 |
| C-0315 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L197 · 6.3 意图操作 · 正文/条款 |  /  后摄一张脸，脸占比较大  /  人物特写  /  顶部两个选项持续可见，用户可一键切换  /  | FR-04、FR-05、FR-15 |
| C-0316 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L198 · 6.3 意图操作 · 正文/条款 |  /  后摄一张脸，脸较小且有明显环境主体  /  人带景  /  用户选择后锁定到本次拍摄结束  /  | FR-04、FR-05、FR-15 |
| C-0317 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L200 · 6.3 意图操作 · 正文/条款 | 起始脸占比只用于减少点击：人物特写建议区间 12%–25%，人带景建议区间 5%–12%，须用 P-1 样张校准。选择“人带景”后，不能机械切长焦或要求走近到遮住地标。 | FR-04、FR-05、FR-15 |
| C-0318 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L202 · 6.3 意图操作 · 正文/条款 | 完整 P0 才增加“只拍景”和前摄；多人安全回退从 P-1 即生效，不等于合影模式获准： | FR-04、FR-05、FR-15 |
| C-0319 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L204 · 6.3 意图操作 · 正文/条款 | - 前摄一张脸：进入自拍包。 | FR-04、FR-05、FR-15 |
| C-0320 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L205 · 6.3 意图操作 · 正文/条款 | - 后摄无脸：预选只拍景。 | FR-04、FR-05、FR-15 |
| C-0321 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L206 · 6.3 意图操作 · 正文/条款 | - 两张脸以上：不进入单人姿势；只给水平、切边和曝光等安全提示。 | FR-04、FR-05、FR-15 |
| C-0322 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L210 · 6.4 初始参数与用户控制 · 正文/条款 | - 场景包可以在高置信度稳定后自动应用一次安全的起始参数，具体见第 11.4 节。 | FR-10、FR-18、N-20 |
| C-0323 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L211 · 6.4 初始参数与用户控制 · 正文/条款 | - 场景自动改变镜头或模式时，操作区显示两秒可读反馈，例如“已切到长焦”；实际倍率按目标机标定结果显示。EV 只由用户主动调整或主动应用建议，不由场景自动修改；请求中与实际成功分开反馈。 | FR-10、FR-18、N-20 |
| C-0324 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L212 · 6.4 初始参数与用户控制 · 正文/条款 | - 后摄快捷焦段以 CameraX 可选择的独立 CameraInfo 为入口，并同时要求标准 Camera2 焦距元数据和不同的 intrinsicZoomRatio；元数据缺失、候选重复或选择失效… | FR-10、FR-18、N-20 |
| C-0325 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L213 · 6.4 初始参数与用户控制 · 正文/条款 | - 连续缩放上下限来自当前绑定相机的 ZoomState；EV 上下限与步长来自当前绑定相机的 ExposureState，切换焦段后重新发现并夹紧已有值。 | FR-10、FR-18、N-20 |
| C-0326 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L214 · 6.4 初始参数与用户控制 · 正文/条款 | - 用户手动改过某项后，该项在本次拍摄会话中锁定，自动规则不得改回。 | FR-10、FR-18、N-20 |
| C-0327 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L215 · 6.4 初始参数与用户控制 · 正文/条款 | - 自动参数调整不计入两步指导预算。 | FR-10、FR-18、N-20 |
| C-0328 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L216 · 6.4 初始参数与用户控制 · 正文/条款 | - 同一参数不得在 5 秒内来回切换。 | FR-10、FR-18、N-20 |
| C-0329 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L220 · 6.5 中文语音与字幕 · 正文/条款 | 本节的动作卡常驻及提示输出规则适用于指导取景上下文；已批准 P1 独立参数面板是明确例外，打开期间暂停指导视觉/TTS，关闭后恢复，详见 P1 创意需求。P-1 不启用该面板。 | FR-08、N-27 |
| C-0330 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L222 · 6.5 中文语音与字幕 · 正文/条款 | - shooter 动作卡和框线始终可见，不受“被拍者字幕”开关影响；语音开启时 shooter 动作也播报，解决持机者无法持续盯住字幕的问题。 | FR-08、N-27 |
| C-0331 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L223 · 6.5 中文语音与字幕 · 正文/条款 | - 当前口令中文语音与被拍者字幕为两个独立开关，首次安装默认都开；允许“语音+字幕 / 仅语音 / 仅字幕 / 提示关闭”四种组合并跨启动保持。 | FR-08、N-27 |
| C-0332 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L224 · 6.5 中文语音与字幕 · 正文/条款 | - subject 字幕开启时持续显示到动作完成、失效、替换、跳过、拍照或用户关闭字幕；进入“可以拍了”本身不清除仍有效的动作字幕。 | FR-08、N-27 |
| C-0333 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L225 · 6.5 中文语音与字幕 · 正文/条款 | - 关闭中文语音时立即停止当前朗读；若字幕开启则继续字幕流程，若字幕也关闭则立即跳过 subject 输出。 | FR-08、N-27 |
| C-0334 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L226 · 6.5 中文语音与字幕 · 正文/条款 | - 系统静音、勿扰或 TTS 失败且字幕开启时，字幕仍可完成流程，并常驻显示当前语音不可用状态；字幕关闭时显示可读失败状态后跳过该条，不擅自开启字幕。 | FR-08、N-27 |
| C-0335 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L227 · 6.5 中文语音与字幕 · 正文/条款 | - 连接耳机时走耳机，不对外播放。 | FR-08、N-27 |
| C-0336 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L228 · 6.5 中文语音与字幕 · 正文/条款 | - 一次只播一句；shooter / subject 都只播当前稳定口令；实时替换口令时停止已失效的旧句再播放新句，不把新句排在旧句后面。 | FR-08、N-27 |
| C-0337 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L229 · 6.5 中文语音与字幕 · 正文/条款 | - 快门音遵循系统，产品不承诺静音快门。 | FR-08、N-27 |
| C-0338 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L233 · 6.6 保存反馈与恢复 · 正文/条款 | - 按下快门后立即给出触觉或视觉反馈，不等写入结束才回应。 | FR-12、N-35 |
| C-0339 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L234 · 6.6 保存反馈与恢复 · 正文/条款 | - 原片发布并通过适用校验后显示本次缩略图与“已保存到系统相册”，不强制进入照片详情页。P1 配方或副本失败须明确“原片已保存，效果未保存”等部分结果，不把原片成功改报为整张未保存。 | FR-12、N-35 |
| C-0340 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L235 · 6.6 保存反馈与恢复 · 正文/条款 | - 写入失败必须保留待保存结果，优先允许“重试保存”；不能只弹一条会消失的 Toast。凡承诺重试的已捕获照片，源文件须保存在有配额和空间预检的持久私有暂存区，并排除云备份及设备迁移，不能仅依赖可被系… | FR-12、N-35 |
| C-0341 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L236 · 6.6 保存反馈与恢复 · 正文/条款 | - 相机被其他应用占用、权限被撤回、镜头初始化失败时显示可执行恢复入口。 | FR-12、N-35 |
| C-0342 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L237 · 6.6 保存反馈与恢复 · 正文/条款 | - App 不得以黑屏、无限加载或静默失败结束主流程。保存失败不是捕获仍在执行；用户结束失败批次后，相机和存储条件允许时可主动开始新拍。资源不足时给出具体状态，不能以无限“捕获中”代替；不建立无界待保… | FR-12、N-35 |
| C-0343 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L241 · 6.7 P0 代拍简化界面 · 正文/条款 | P-1 不实现。P0 流程： | FR-16 |
| C-0344 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L243 · 6.7 P0 代拍简化界面 · 正文/条款 | 1. 拍摄者在取景器点“交给别人拍”。 | FR-16 |
| C-0345 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L244 · 6.7 P0 代拍简化界面 · 正文/条款 | 2. App 保留当前意图和构图轮廓，进入简化页。 | FR-16 |
| C-0346 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L245 · 6.7 P0 代拍简化界面 · 正文/条款 | 3. 简化页只显示退出、轮廓、一个大快门和音量键提示；屏幕常亮。 | FR-16 |
| C-0347 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L246 · 6.7 P0 代拍简化界面 · 正文/条款 | 4. 不显示构图、EV、评分或多条字幕；语音默认静音或只在开屏播一句。 | FR-16 |
| C-0348 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L247 · 6.7 P0 代拍简化界面 · 正文/条款 | 5. 代拍者拍完后点“还给我”，回到拍摄者取景器。 | FR-16 |
| C-0349 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L249 · 6.7 P0 代拍简化界面 · 正文/条款 | 这条路径是完整性要求，不宣称独有；PoseOverlay 等产品已有近似陌生人模式。 | FR-16 |
| C-0350 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L253 · 6.8 易用性与无障碍 · 正文/条款 | - 所有主要触控目标至少 48dp。 | N-28 |
| C-0351 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L254 · 6.8 易用性与无障碍 · 正文/条款 | - 不能只用红/绿颜色表达完成、错误或闪光状态。 | N-28 |
| C-0352 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L255 · 6.8 易用性与无障碍 · 正文/条款 | - TalkBack 能读出意图、当前动作、中文语音和被拍者字幕开关及状态、闪光、焦段、快门和保存结果。 | N-28 |
| C-0353 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L256 · 6.8 易用性与无障碍 · 正文/条款 | - 字号跟随系统放大时，动作卡可增高但快门不可被挤出屏幕。 | N-28 |
| C-0354 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L257 · 6.8 易用性与无障碍 · 正文/条款 | - 竖屏和横屏都验收；折叠外屏和平板不属于第一版验收范围。 | N-28 |
| C-0355 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L258 · 6.8 易用性与无障碍 · 正文/条款 | - 核心操作不依赖长按、双击或隐藏手势；长按锁 AE/AF 属于增强操作，不是完拍前提。 | N-28 |
| C-0356 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L268 · 8.1 候选、优先级和预算 · 正文/条款 | 端侧流程： | FR-06、FR-07、N-26 |
| C-0357 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L270 · 8.1 候选、优先级和预算 · 流程上下文 | 画面信号 | FR-06、FR-07、N-26 |
| C-0358 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L271 · 8.1 候选、优先级和预算 · 流程上下文 | ↓ | FR-06、FR-07、N-26 |
| C-0359 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L272 · 8.1 候选、优先级和预算 · 流程上下文 | 用户意图硬过滤 | FR-06、FR-07、N-26 |
| C-0360 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L273 · 8.1 候选、优先级和预算 · 流程上下文 | ↓ | FR-06、FR-07、N-26 |
| C-0361 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L274 · 8.1 候选、优先级和预算 · 流程上下文 | 场景规则匹配 | FR-06、FR-07、N-26 |
| C-0362 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L275 · 8.1 候选、优先级和预算 · 流程上下文 | ↓ | FR-06、FR-07、N-26 |
| C-0363 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L276 · 8.1 候选、优先级和预算 · 流程上下文 | 最多三条候选：构图 / 光或曝光 / 姿势 | FR-06、FR-07、N-26 |
| C-0364 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L277 · 8.1 候选、优先级和预算 · 流程上下文 | ↓ | FR-06、FR-07、N-26 |
| C-0365 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L278 · 8.1 候选、优先级和预算 · 流程上下文 | 节奏器一次呈现一条 | FR-06、FR-07、N-26 |
| C-0366 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L279 · 8.1 候选、优先级和预算 · 流程上下文 | ↓ | FR-06、FR-07、N-26 |
| C-0367 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L280 · 8.1 候选、优先级和预算 · 流程上下文 | 最多两条后执行统一结束判定（见第 6.2 节） | FR-06、FR-07、N-26 |
| C-0368 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L282 · 8.1 候选、优先级和预算 · 正文/条款 | 该流程在取景期间持续运行，不是首次命中后冻结：每约 300ms 送入最新信号；已经显示的口令与最新候选不再一致时，重新经过稳定门槛后完成、清除或替换。实时变化只刷新当前动作槽，不增加两步预算；进入“可… | FR-06、FR-07、N-26 |
| C-0369 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L284 · 8.1 候选、优先级和预算 · 正文/条款 | Face 与 Pose 结果不得互相作为唯一开关：检测到可靠上半身且未检测到多张脸时，可以输出保守的单人姿势口令；Face 与 Pose 都未看清时输出“请露出脸，或靠近一点”这一检测恢复动作。两张脸… | FR-06、FR-07、N-26 |
| C-0370 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L286 · 8.1 候选、优先级和预算 · 正文/条款 | 优先级： | FR-06、FR-07、N-26 |
| C-0371 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L288 · 8.1 候选、优先级和预算 · 正文/条款 | 1. 相机不可用、保存失败等系统阻断。 | FR-06、FR-07、N-26 |
| C-0372 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L289 · 8.1 候选、优先级和预算 · 正文/条款 | 2. 镜头遮挡、主体切边、严重过曝等无法正常成片的问题。 | FR-06、FR-07、N-26 |
| C-0373 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L290 · 8.1 候选、优先级和预算 · 正文/条款 | 3. 最重要的 shooter 机位、构图或曝光动作。 | FR-06、FR-07、N-26 |
| C-0374 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L291 · 8.1 候选、优先级和预算 · 正文/条款 | 4. 一条 subject 动作。 | FR-06、FR-07、N-26 |
| C-0375 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L292 · 8.1 候选、优先级和预算 · 正文/条款 | 5. 用户点“再优化一下”后的可选候选。 | FR-06、FR-07、N-26 |
| C-0376 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L294 · 8.1 候选、优先级和预算 · 正文/条款 | 如果没有可靠的 subject 候选，可以只完成一个 shooter 动作后执行统一结束判定。“最多两步”不是必须凑满两步。 | FR-06、FR-07、N-26 |
| C-0377 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L300 · 8.2 触发与防抖 · 正文/条款 |  /  候选触发  /  条件连续稳定 ≥ 600ms，或连续三次分析成立  /  | N-24、N-25 |
| C-0378 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L301 · 8.2 触发与防抖 · 正文/条款 |  /  动作完成  /  可判断信号改善并稳定 ≥ 500ms  /  | N-24、N-25 |
| C-0379 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L302 · 8.2 触发与防抖 · 正文/条款 |  /  当前口令失效/替换  /  最新候选连续稳定 ≥ 600ms 或连续三次分析成立后，清除已完成口令或替换为同一动作槽的新口令  /  | N-24、N-25 |
| C-0380 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L303 · 8.2 触发与防抖 · 正文/条款 |  /  最短展示  /  1.5 秒  /  | N-24、N-25 |
| C-0381 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L304 · 8.2 触发与防抖 · 正文/条款 |  /  已识别人但无可靠建议  /  1.5 秒内结束观察；最低条件成立进入“可以拍了”，否则进入“随时可拍”  /  | N-24、N-25 |
| C-0382 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L305 · 8.2 触发与防抖 · 正文/条款 |  /  TTS 重复间隔  /  同一句至少 8 秒不重复  /  | N-24、N-25 |
| C-0383 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L306 · 8.2 触发与防抖 · 正文/条款 |  /  方向冲突  /  5 秒内不得给出相反动作  /  | N-24、N-25 |
| C-0384 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L307 · 8.2 触发与防抖 · 正文/条款 |  /  用户手动控制  /  改 EV、焦段或意图后，相关自动建议至少静默 3 秒并尊重会话锁定  /  | N-24、N-25 |
| C-0385 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L308 · 8.2 触发与防抖 · 正文/条款 |  /  低置信度  /  不猜姿势和场景；只保留水平、切边等确定性反馈  /  | N-24、N-25 |
| C-0386 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L310 · 8.2 触发与防抖 · 正文/条款 | 提示性能分开记录：画面变化到可见提示包含采样、防抖及排队；稳定候选到可见提示才适用 ≤300ms p95 门槛，详见需求总纲第 3.3 节。不通过缩短防抖凑性能指标。连续三次仅计同一相机会话内、捕获时… | N-24、N-25 |
| C-0387 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L316 · 8.3 信号到提示 · 正文/条款 |  /  有脸且脸暗、背景亮  /  点一下脸  /  用旋转对齐后的脸框中心亮度与框外背景采样比较；样本不足保持未知；不把“开闪光”作为首选  /  | SG-01 |
| C-0388 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L317 · 8.3 信号到提示 · 正文/条款 |  /  人物特写且脸占比 &lt; 12%  /  走近一步；小米 14 Pro 的已验收长焦可用时可建议切长焦  /  未通过真机标定的焦段不显示、不建议  /  | SG-02 |
| C-0389 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L318 · 8.3 信号到提示 · 正文/条款 |  /  人带景且脸占比 &lt; 5%  /  略走近一点，地标要留全  /  不自动切长焦到丢失地标  /  | SG-03 |
| C-0390 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L319 · 8.3 信号到提示 · 正文/条款 |  /  人脸贴近画面边缘  /  后退半步，人物别贴画面边缘  /  只依据可见边界，不声称看到了画面外的身体  /  | SG-04 |
| C-0391 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L320 · 8.3 信号到提示 · 正文/条款 |  /  人物特写且脸明显落在画面下半部  /  把脸放到上方三分线  /  只用稳定的脸框位置触发；不显示审美分  /  | SG-05 |
| C-0392 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L321 · 8.3 信号到提示 · 正文/条款 |  /  人带景且人物仍在画面正中  /  人物放在左或右三分线，景物留另一侧  /  只在用户已选择“人带景”时触发，不强迫所有人像离开中心  /  | SG-06 |
| C-0393 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L322 · 8.3 信号到提示 · 正文/条款 |  /  水平倾斜 &gt; 3°  /  沿网格放平手机，地平线别歪  /  5 秒内不反向提示  /  | SG-07 |
| C-0394 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L323 · 8.3 信号到提示 · 正文/条款 |  /  一张脸且左右转头超过约 18°  /  脸转回镜头一点  /  先恢复正脸再判断眼睛；不对侧脸乱猜  /  | SG-08 |
| C-0395 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L324 · 8.3 信号到提示 · 正文/条款 |  /  一张正脸且任一眼睛持续低睁眼概率  /  眼睛睁开一点，看镜头  /  连续三帧或 600ms 才提示，瞬时眨眼不触发  /  | SG-09 |
| C-0396 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L325 · 8.3 信号到提示 · 正文/条款 |  /  单帧低微笑概率  /  不生成自动口令  /  低微笑不等于紧张、不开心或需要纠正；以后只有用户明确选择“微笑”意图且有独立验收时再立项  /  | SG-10 |
| C-0397 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L326 · 8.3 信号到提示 · 正文/条款 |  /  一张脸、肩线过正  /  身体侧一点，脸转回镜头  /  只给 subject  /  | SG-11 |
| C-0398 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L327 · 8.3 信号到提示 · 正文/条款 |  /  单帧 2D 髋部/脚踝看似居中  /  不生成“重心换到后腿”口令  /  单目 2D 姿态不能可靠判断前后脚承重；保留为人工拍摄灵感，不作自动完成条件  /  | SG-12 |
| C-0399 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L328 · 8.3 信号到提示 · 正文/条款 |  /  头明显后仰  /  下巴微收，头略往前  /  低置信度不猜  /  | SG-13 |
| C-0400 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L329 · 8.3 信号到提示 · 正文/条款 |  /  场景很暗且手持  /  靠稳手机  /  P0 可建议兼容的系统夜景；不建议数码猛拉  /  | SG-14 |
| C-0401 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L330 · 8.3 信号到提示 · 正文/条款 |  /  Face 与 Pose 都未看清单人人物  /  请露出脸，或靠近一点  /  属于检测恢复动作；不得显示“可以拍了”；两张脸以上不使用该句冒充单人判断  /  | SG-15 |
| C-0402 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L334 · 8.4 文案规则 · 正文/条款 | - 一句只含一个主要动作，优先不超过 16 个汉字。 | N-29 |
| C-0403 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L335 · 8.4 文案规则 · 正文/条款 | - 使用手机能执行的词：点脸、拉曝光、切长焦、走近、放平、挪到窗边；倍率文案使用小米 14 Pro 真机标定值。 | N-29 |
| C-0404 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L336 · 8.4 文案规则 · 正文/条款 | - 不对新手说光圈、ISO、焦距倒数、开尔文或视觉权重。 | N-29 |
| C-0405 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L337 · 8.4 文案规则 · 正文/条款 | - 不评价外貌，不使用“胖、脸大、不上镜”等文案。 | N-29 |
| C-0406 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L338 · 8.4 文案规则 · 正文/条款 | - 默认不解释摄影原理；P0 用户点“为什么 / 再讲细”后再展开。 | N-29 |
| C-0407 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L339 · 8.4 文案规则 · 正文/条款 | - 每条口令带听众，缺少或非法听众的云端回包必须丢弃。 | N-29 |
| C-0408 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L343 · 8.5 P0 “再讲细” · 正文/条款 | - 默认关闭，用户主动点击才联网。 | FR-17 |
| C-0409 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L344 · 8.5 P0 “再讲细” · 正文/条款 | - 首次上传前单独说明：当前预览帧可能含人脸，会离开设备，用完不落库。 | FR-17 |
| C-0410 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L345 · 8.5 P0 “再讲细” · 正文/条款 | - 拒绝、撤回或失败时显示“讲解暂时不可用”，内置指导继续。 | FR-17 |
| C-0411 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L346 · 8.5 P0 “再讲细” · 正文/条款 | - 只上传当前压缩帧和必要信号，不连续上传预览。 | FR-17 |
| C-0412 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L347 · 8.5 P0 “再讲细” · 正文/条款 | - 返回最多三条候选和一句原因，客户端重新做优先级、术语、听众和两步预算校验。 | FR-17 |
| C-0413 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L348 · 8.5 P0 “再讲细” · 正文/条款 | - 供应商可替换，主路径不绑定一家模型。 | FR-17 |
| C-0414 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L356 · 11. 场景与口令库 · 正文/条款 | 场景库是规则和文案，不是滤镜库，也不是为每个题材训练一套模型。用户意图是硬过滤条件；每轮最多产生构图、光/曝光、姿势三条候选，再由第 8 节节奏器决定最多两个必做动作。表中的窗户、地标、椅子、天气等场… | N-30 |
| C-0415 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L362 · 11.1 P-1 三个场景包 · 正文/条款 |  /  室内窗边  /  一张脸，室内，一侧明显更亮；人物特写  /  兼容时人像，否则 Photo；闪光关；人物特写可一次切到小米 14 Pro 的已验收长焦  /  靠窗半步；点一下脸；窗过白时曝… | SC-01 |
| C-0416 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L363 · 11.1 P-1 三个场景包 · 正文/条款 |  /  室外人带景  /  一张脸 + 明显地标或大环境；人带景  /  Photo；兼容时 HDR；1x；闪光关  /  人物放在左或右三分线、景物留另一侧；沿网格放平；略走近但地标留全  /  身… | SC-02 |
| C-0417 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L364 · 11.1 P-1 三个场景包 · 正文/条款 |  /  逆光人像  /  一张脸明显暗于背景；人物特写或人带景  /  Photo；兼容时 HDR；按意图保持 1x/已验收长焦；闪光关  /  点一下脸；向侧边走一步；天空过白时略降曝光  /  略… | SC-03 |
| C-0418 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L366 · 11.1 P-1 三个场景包 · 正文/条款 | P-1 静态对照卡： | SC-01、SC-02、SC-03、SC-04、SC-05、SC-06 |
| C-0419 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L370 · 11.1 P-1 三个场景包 · 正文/条款 |  /  室内窗边  /  靠近窗边，让柔光照到脸  /  身体转向窗户  /  | SC-04 |
| C-0420 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L371 · 11.1 P-1 三个场景包 · 正文/条款 |  /  室外人带景  /  人物放在左或右三分线，景物留另一侧  /  身体侧一点  /  | SC-05 |
| C-0421 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L372 · 11.1 P-1 三个场景包 · 正文/条款 |  /  逆光人像  /  点一下脸，别让脸全黑  /  下巴微收  /  | SC-06 |
| C-0422 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L374 · 11.1 P-1 三个场景包 · 正文/条款 | 静态卡不消费当前信号；实验使用需求总纲第 3.2 节固定配置。两组共享相机管线、起始参数、按钮、跳过/超时及保存反馈；静态卡不做信号改善自动完成，结束显示“随时可拍”。后台分析负载和动态机制差异在实验… | SC-01、SC-02、SC-03、SC-04、SC-05、SC-06 |
| C-0423 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L378 · 11.2 P0 与 P1 场景 · 正文/条款 | P0 在 P-1 过线后启用： | FR-15、N-33 |
| C-0424 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L382 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  室外树荫 / 软光  /  避开叶子斑驳光，脸朝开阔天空  /  挪半步，躲开头顶花斑  /  | SC-07 |
| C-0425 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L383 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  黄金时段  /  保留天空和发丝轮廓，不把脸拉白  /  略侧身，让头发有一圈暖光  /  | SC-08 |
| C-0426 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L384 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  阴天人像  /  使用均匀软光，避免系统过度提亮  /  靠近浅色墙或开阔天空  /  | SC-09 |
| C-0427 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L385 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  室内暖光  /  避免两种色温同时打脸  /  转向更亮的那一侧  /  | SC-10 |
| C-0428 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L386 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  自拍  /  镜头略高于眼，不用广角贴脸  /  头略偏向自己好看的一侧  /  | SC-11 |
| C-0429 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L387 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  旅行风景  /  无脸时放平地平线、保留天空  /  放平手机  /  | SC-12 |
| C-0430 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L388 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  建筑立面  /  保持竖线垂直，避免仰拍汇聚  /  手机放平一点  /  | SC-13 |
| C-0431 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L389 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  街拍引导线  /  等人物走进线条，不做网红姿势库  /  顺着路走，看镜头或看前方  /  | SC-14 |
| C-0432 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L390 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  通用兜底  /  只给水平、切边、主体等确定提示  /  结束时按适用意图与可靠证据判定，不直接确认“可以拍了”  /  | SC-15 |
| C-0433 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L392 · 11.2 P0 与 P1 场景 · 正文/条款 | P1 规则可以预写但默认不参与匹配： | FR-15、N-33 |
| C-0434 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L396 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  美食  /  靠窗侧光、关闪光、减少桌面杂物；45° 或俯拍按形状选择  /  | SC-16 |
| C-0435 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L397 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  夜景  /  兼容时系统夜景、靠稳、关闪光、点亮部防过曝  /  | SC-17 |
| C-0436 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L398 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  蓝调时刻  /  保留蓝天与城市灯，不自动拉成白昼  /  | SC-18 |
| C-0437 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L399 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  雨后倒影  /  蹲低贴近水面，保高光，不踩碎倒影  /  | SC-19 |
| C-0438 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L400 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  海边人像  /  放平海平线，按逆光规则保脸和天空  /  | SC-20 |
| C-0439 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L401 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  咖啡馆窗边  /  复用窗边人像，避免桌面杂物占满前景  /  | SC-21 |
| C-0440 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L402 · 11.2 P0 与 P1 场景 · 正文/条款 |  /  宠物  /  点眼睛、自然光、关闪光；不识别品种  /  | SC-22 |
| C-0441 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L404 · 11.2 P0 与 P1 场景 · 正文/条款 | 不按“森系、电影感、黑金”等风格建立场景包，也不把视觉模型的数百个标签直接当业务场景。 | FR-15、N-33 |
| C-0442 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L408 · 11.3 回归样张 · 正文/条款 | 每个启用场景至少四类真机样张： | N-30 |
| C-0443 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L410 · 11.3 回归样张 · 正文/条款 | 1. 应该进入该场景。 | N-30 |
| C-0444 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L411 · 11.3 回归样张 · 正文/条款 | 2. 不应该进入该场景。 | N-30 |
| C-0445 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L412 · 11.3 回归样张 · 正文/条款 | 3. 必须出现的提示。 | N-30 |
| C-0446 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L413 · 11.3 回归样张 · 正文/条款 | 4. 禁止出现的提示。 | N-30 |
| C-0447 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L415 · 11.3 回归样张 · 正文/条款 | 额外要求： | N-30 |
| C-0448 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L417 · 11.3 回归样张 · 正文/条款 | - 每个 P-1 场景至少覆盖小米 14 Pro 真机、竖屏和横屏；每次记录系统与构建版本。 | N-30 |
| C-0449 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L418 · 11.3 回归样张 · 正文/条款 | - 两张脸以上不得进入单人姿势匹配，只给水平、切边和曝光等安全回退。 | N-30 |
| C-0450 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L419 · 11.3 回归样张 · 正文/条款 | - 无脸、遮挡或骨骼不完整时不猜姿势，不显示匹配分。 | N-30 |
| C-0451 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L420 · 11.3 回归样张 · 正文/条款 | - 场景新增或阈值变化必须重跑对应样张；不能只靠开发者现场观察。 | N-30 |
| C-0452 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L424 · 11.4 场景初始参数 · 正文/条款 | 冲突优先级： | FR-10、FR-18 |
| C-0453 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L426 · 11.4 场景初始参数 · 正文/条款 | **用户明确意图 &gt; 相机方向和人数等硬信号 &gt; 更具体的光位/构图包 &gt; 通用兜底。** | FR-10、FR-18 |
| C-0454 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L428 · 11.4 场景初始参数 · 正文/条款 | 场景高置信度稳定后，可在一次拍摄会话中自动应用一次起始参数： | FR-10、FR-18 |
| C-0455 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L432 · 11.4 场景初始参数 · 正文/条款 |  /  系统模式  /  兼容且能与分析同时工作时使用 Photo / 人像 / HDR / 夜景；否则标准 Photo  /  用户可切回 Photo  /  | FR-10、FR-18 |
| C-0456 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L433 · 11.4 场景初始参数 · 正文/条款 |  /  镜头  /  人物特写可用小米 14 Pro 经真机确认的长焦；人带景默认 1x  /  标签使用标定倍率；用户手改后会话锁定  /  | FR-10、FR-18 |
| C-0457 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L434 · 11.4 场景初始参数 · 正文/条款 |  /  对焦  /  一张脸时优先脸；无脸时点中景或主体  /  用户可重新点按  /  | FR-10、FR-18 |
| C-0458 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L435 · 11.4 场景初始参数 · 正文/条款 |  /  EV  /  默认 0 EV；场景识别不自动修改 EV，按有效画面证据提供用户主动应用的建议  /  用户拖动后会话锁定  /  | FR-10、FR-18 |
| C-0459 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L436 · 11.4 场景初始参数 · 正文/条款 |  /  闪光  /  默认关  /  状态可见；用户可选自动  /  | FR-10、FR-18 |
| C-0460 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L438 · 11.4 场景初始参数 · 正文/条款 | 自动调整必须可见、可覆盖、不可抖动。不得自动套 LUT、美颜、FACE_RETOUCH、数码变焦或自动快门。第 3.2 节对照实验是参数策略的明确例外：静态卡与动态教练均使用每场景固定初始参数，关闭实… | FR-10、FR-18 |
| C-0461 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L442 · 11.5 短口令核 · 正文/条款 | 第一版短口令数量由两个意图、三个启用场景及安全回退的覆盖推导，不以历史 12–16 或 8–12 区间作为交付门槛。每条口令须有稳定 ID、听众、适用范围、输入证据、触发/退出、超时/失效处理及对应回… | N-29 |
| C-0462 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L446 · 11.5 短口令核 · 正文/条款 |  /  subject  /  下巴微收，头略往前  /  头明显后仰  /  | CU-01 |
| C-0463 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L447 · 11.5 短口令核 · 正文/条款 |  /  subject  /  脸转回镜头一点  /  左右转头超过可靠分类范围  /  | CU-02 |
| C-0464 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L448 · 11.5 短口令核 · 正文/条款 |  /  subject  /  眼睛睁开一点，看镜头  /  正脸下持续检测到至少一只眼睛低睁眼概率  /  | CU-03 |
| C-0465 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L449 · 11.5 短口令核 · 正文/条款 |  /  subject  /  身体侧一点，脸转回镜头  /  可靠肩髋二维关系满足经校准的正对镜头代理条件；不推断“僵硬”或情绪  /  | CU-04 |
| C-0466 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L450 · 11.5 短口令核 · 正文/条款 |  /  subject  /  肩放松  /  肩部明显耸起  /  | CU-05 |
| C-0467 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L451 · 11.5 短口令核 · 正文/条款 |  /  subject  /  手自然放在身侧  /  可靠双腕持续位于躯干内部或形成遮挡代理；不推断“僵直”，不假设衣服有口袋  /  | CU-06 |
| C-0468 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L452 · 11.5 短口令核 · 正文/条款 |  /  shooter  /  点一下脸，再重新构图  /  焦点或测光不在脸上  /  | CU-07 |
| C-0469 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L453 · 11.5 短口令核 · 正文/条款 |  /  shooter  /  走近一步，减少无关空白  /  人物特写且脸占比过小  /  | CU-08 |
| C-0470 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L454 · 11.5 短口令核 · 正文/条款 |  /  shooter  /  略走近，让人物更清楚，地标留全  /  人带景且脸占比过小  /  | CU-09 |
| C-0471 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L455 · 11.5 短口令核 · 正文/条款 |  /  shooter  /  后退半步，人物别贴画面边缘  /  人脸贴近画面边缘  /  | CU-10 |
| C-0472 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L456 · 11.5 短口令核 · 正文/条款 |  /  shooter  /  把脸放到上方三分线  /  人物特写且脸明显落在下半部  /  | CU-11 |
| C-0473 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L457 · 11.5 短口令核 · 正文/条款 |  /  shooter  /  人物放在左或右三分线，景物留另一侧  /  人带景且人物仍在正中  /  | CU-12 |
| C-0474 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L458 · 11.5 短口令核 · 正文/条款 |  /  shooter  /  沿网格放平手机，地平线别歪  /  倾斜超过 3°  /  | CU-13 |
| C-0475 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L459 · 11.5 短口令核 · 正文/条款 |  /  shooter  /  请露出脸，或靠近一点  /  Face 与 Pose 都未看清单人人物  /  | CU-14 |
| C-0476 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L461 · 11.5 短口令核 · 正文/条款 | 规则： | N-29 |
| C-0477 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L463 · 11.5 短口令核 · 正文/条款 | - P-1 首先验证前四条 subject 口令，不要求大姿势库。 | N-29 |
| C-0478 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L464 · 11.5 短口令核 · 正文/条款 | - 场景特有句与核口令冲突时，场景特有句优先。 | N-29 |
| C-0479 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L465 · 11.5 短口令核 · 正文/条款 | - “换一句”只在当前场景可用句中轮换，不进入姿势商城。 | N-29 |
| C-0480 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L466 · 11.5 短口令核 · 正文/条款 | - 所有基础口令免费。 | N-29 |
| C-0481 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L467 · 11.5 短口令核 · 正文/条款 | - 不评价外貌，不给姿势分，不等待 100% 匹配。 | N-29 |
| C-0482 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L468 · 11.5 短口令核 · 正文/条款 | - 人脸细节只使用端侧 Face 检测提供的正脸角度与睁眼概率；分类缺失、侧脸、多人或置信度不足时不猜。微笑概率不转成“表情需要纠正”。它不是身份识别，也不识别年龄、性别、颜值或复杂情绪。 | N-29 |
| C-0483 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L472 · 11.6 P1 第二阶段姿势与摄影技巧库 · 正文/条款 | 本节属于小米 14 Pro 的 ApprovedSeparate P1 第二阶段，不进入 P-1 三场景对照实验，也不把 P-1 按场景覆盖推导的短口令核扩成必做姿势商城。用户选择某类姿势后，指导仍一… | FR-32、FR-33 |
| C-0484 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L476 · 11.6.1 姿势证据与状态 · 正文/条款 | 每条姿势必须同时定义： | FR-32、N-24、N-26 |
| C-0485 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L478 · 11.6.1 姿势证据与状态 · 正文/条款 | - 适用类型：特写、半身、全身、坐姿、走动或单人互动。 | FR-32、N-24、N-26 |
| C-0486 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L479 · 11.6.1 姿势证据与状态 · 正文/条款 | - 听众、短口令正文和不超过两行的显示文本。 | FR-32、N-24、N-26 |
| C-0487 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L480 · 11.6.1 姿势证据与状态 · 正文/条款 | - 进入证据：所需 Face/Pose 关键点、画面边距、时序稳定性和最低可靠度。 | FR-32、N-24、N-26 |
| C-0488 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L481 · 11.6.1 姿势证据与状态 · 正文/条款 | - 退出证据：动作改善后能在画面中直接观察的条件。 | FR-32、N-24、N-26 |
| C-0489 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L482 · 11.6.1 姿势证据与状态 · 正文/条款 | - 完成方式：可观察退出、定时灵感卡、只提示 shooter 或禁止自动完成。 | FR-32、N-24、N-26 |
| C-0490 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L483 · 11.6.1 姿势证据与状态 · 正文/条款 | - 超时、冷却组和低置信度回退；失效口令不得停留或继续播报。 | FR-32、N-24、N-26 |
| C-0491 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L485 · 11.6.1 姿势证据与状态 · 正文/条款 | 统一状态为： | FR-32、N-24、N-26 |
| C-0492 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L487 · 11.6.1 姿势证据与状态 · 流程上下文 | Disabled | FR-32、N-24、N-26 |
| C-0493 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L488 · 11.6.1 姿势证据与状态 · 流程上下文 | → Acquiring | FR-32、N-24、N-26 |
| C-0494 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L489 · 11.6.1 姿势证据与状态 · 流程上下文 | → Eligible | FR-32、N-24、N-26 |
| C-0495 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L490 · 11.6.1 姿势证据与状态 · 流程上下文 | → CueActive | FR-32、N-24、N-26 |
| C-0496 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L491 · 11.6.1 姿势证据与状态 · 流程上下文 | → Satisfied | FR-32、N-24、N-26 |
| C-0497 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L492 · 11.6.1 姿势证据与状态 · 流程上下文 | → Cooldown | FR-32、N-24、N-26 |
| C-0498 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L494 · 11.6.1 姿势证据与状态 · 正文/条款 | 任一阶段证据丢失后进入 LowConfidence；检测到两张脸以上进入 MultiPersonSuppressed。两者都只允许人物入框、切边、水平和严重曝光等安全反馈，不输出单人姿势。进入、退出、… | FR-32、N-24、N-26 |
| C-0499 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L500 · 11.6.2 六类姿势 · 正文/条款 |  /  特写  /  脸转回一点  /  单脸可靠且左右转头持续超过校准范围  /  转头回到正脸安全区并稳定  /  可直接实现；阈值需目标机微调  /  | PO-01 |
| C-0500 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L501 · 11.6.2 六类姿势 · 正文/条款 |  /  特写  /  眼睛睁开，看镜头  /  正脸、双眼分类可用且持续低睁眼概率  /  双眼睁开概率恢复并稳定  /  可直接实现；瞬时眨眼不触发  /  | PO-02 |
| C-0501 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L502 · 11.6.2 六类姿势 · 正文/条款 |  /  特写/半身  /  肩放松一点  /  双肩和耳部可靠，归一化肩位持续偏高  /  肩位回到目标机校准区间  /  需小米样张校准  /  | PO-03 |
| C-0502 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L503 · 11.6.2 六类姿势 · 正文/条款 |  /  半身  /  一只手放到身体外侧  /  双腕可靠且持续位于躯干内部或挡住身体  /  至少一只手进入安全外侧且未贴边  /  需姿态、衣着与遮挡样张校准  /  | PO-04 |
| C-0503 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L504 · 11.6.2 六类姿势 · 正文/条款 |  /  半身  /  身体侧一点  /  肩线持续接近正对镜头且脸部方向可用  /  肩/髋二维不对称回到校准区间  /  需校准；不得把 Pose Z 当真实深度  /  | PO-05 |
| C-0504 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L505 · 11.6.2 六类姿势 · 正文/条款 |  /  全身  /  脚下留一点空  /  双踝可靠且靠近画面底边  /  双踝与底边余量恢复  /  可直接实现；边距阈值需校准  /  | PO-06 |
| C-0505 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L506 · 11.6.2 六类姿势 · 正文/条款 |  /  全身  /  手肘和膝盖别贴边  /  腕、肘、膝或踝进入裁切危险区  /  可靠关节离开危险区  /  可直接实现  /  | PO-07 |
| C-0506 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L507 · 11.6.2 六类姿势 · 正文/条款 |  /  全身  /  一只脚向前半步  /  用户选择该灵感且全身稳定可见  /  定时结束或用户跳过  /  不做自动完成；单目二维不足以证明前后脚  /  | PO-08 |
| C-0507 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L508 · 11.6.2 六类姿势 · 正文/条款 |  /  坐姿  /  身体坐直，肩放松  /  髋、膝、踝形成持续坐姿候选且肩髋可见  /  躯干轴和肩位进入校准区间  /  需小米样张和机位校准  /  | PO-09 |
| C-0508 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L509 · 11.6.2 六类姿势 · 正文/条款 |  /  坐姿  /  坐到椅子前半边  /  当前没有可靠椅子或座面检测  /  定时结束或用户跳过  /  只做手动灵感卡，不自动触发  /  | PO-10 |
| C-0509 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L510 · 11.6.2 六类姿势 · 正文/条款 |  /  走动  /  慢慢走一步  /  单人全身、髋与双踝连续可见  /  出现稳定交替步态和位移  /  需时序、模糊和连拍真机校准  /  | PO-11 |
| C-0510 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L511 · 11.6.2 六类姿势 · 正文/条款 |  /  单人互动  /  靠近环境或道具，自然做一个动作  /  用户主动选择互动灵感  /  定时结束或用户跳过  /  不识别具体道具；不扩成双人合影  /  | PO-12 |
| C-0511 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L513 · 11.6.2 六类姿势 · 正文/条款 | “互动”只指一名 subject 与环境、道具或拍摄者指令的互动灵感。两张脸以上仍属于合影后期范围，不能因本节出现互动类型而提前多人 Pose。 | FR-32 |
| C-0512 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L519 · 11.6.3 摄影技巧 · 正文/条款 |  /  构图  /  人脸/身体框、三分线、头顶和脚下余量  /  人往右站一点；脚下留一点空  /  不显示审美分  /  | TE-01 |
| C-0513 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L520 · 11.6.3 摄影技巧 · 正文/条款 |  /  关节裁切  /  可靠腕、肘、膝、踝到边缘的距离  /  别从膝盖处截断  /  关键点不可靠时不猜  /  | TE-02 |
| C-0514 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L521 · 11.6.3 摄影技巧 · 正文/条款 |  /  用光  /  旋转对齐的人脸核心与背景亮度、高光比例  /  让人物转向亮的一边；曝光降一点  /  不把位置当亮度，不默认开闪光  /  | TE-03 |
| C-0515 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L522 · 11.6.3 摄影技巧 · 正文/条款 |  /  焦段与距离  /  当前公开相机能力、已验收焦段、脸/身体占比  /  退后一点，再切到已验收长焦  /  不硬编码 2x，不把数码裁切叫光学镜头  /  | TE-04 |
| C-0516 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L523 · 11.6.3 摄影技巧 · 正文/条款 |  /  背景空间  /  人脸方向、主体附近边缘密度和局部对比  /  面朝的方向多留一点空间；换个更干净的背景  /  背景杂乱阈值需目标机样张  /  | TE-05 |
| C-0517 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L524 · 11.6.3 摄影技巧 · 正文/条款 |  /  机位  /  设备姿态和稳定的人体二维几何  /  手机稍微放低一点  /  不承诺精确相机高度或真实透视深度  /  | TE-06 |
| C-0518 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L525 · 11.6.3 摄影技巧 · 正文/条款 |  /  夜景  /  平均亮度、手持稳定性、公开模式能力  /  扶稳手机；先用普通模式  /  不承诺获得系统相机私有夜景质量  /  | TE-07 |
| C-0519 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L526 · 11.6.3 摄影技巧 · 正文/条款 |  /  运动  /  关节时序速度、清晰度和连拍状态  /  等动作停一下再拍；明确开启三张连拍  /  模糊阈值和热量需真机校准  /  | TE-08 |
| C-0520 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L527 · 11.6.3 摄影技巧 · 正文/条款 |  /  多人安全回退  /  多个人脸框、切边、水平和严重曝光  /  两个人再靠近一点；放平手机  /  不分配单人 Pose，不做表情比较  /  | TE-09 |
| C-0521 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L529 · 11.6.3 摄影技巧 · 正文/条款 | 摄影技巧只输出当前取景器能执行、公开 API 能确认或画面可直接观察的动作。背景类别、机位高度、焦段品质、动作自然程度及“更上镜”等无法可靠观测的结论不得伪装成自动判断。 | FR-33 |
| C-0522 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L533 · 11.6.4 表情边界 · 正文/条款 | - 可直接使用的只有转头、侧倾、眼睛睁开和脸部是否足够可见等中性动作。 | FR-35 |
| C-0523 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L534 · 11.6.4 表情边界 · 正文/条款 | - 微笑只在以后增加用户明确选择的“笑脸照”意图、完成独立多帧验收后才能作为动作存在；即使启用，也只能描述“笑容动作”，不能推断开心。 | FR-35 |
| C-0524 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L535 · 11.6.4 表情边界 · 正文/条款 | - 不输出自然、紧张、尴尬、疲惫、悲伤、愤怒、上镜程度或其他心理/审美标签。 | FR-35 |
| C-0525 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L536 · 11.6.4 表情边界 · 正文/条款 | - 不比较多人表情，不保存 tracking ID，不创建人脸 embedding、身份或长期画像。 | FR-35 |
| C-0526 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L540 · 11.6.5 低置信度回退顺序 · 正文/条款 | 1. 人物是否进入画面。 | FR-05、FR-32 |
| C-0527 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L541 · 11.6.5 低置信度回退顺序 · 正文/条款 | 2. 头顶、脚部或可靠关节是否贴边。 | FR-05、FR-32 |
| C-0528 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L542 · 11.6.5 低置信度回退顺序 · 正文/条款 | 3. 手机是否水平。 | FR-05、FR-32 |
| C-0529 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L543 · 11.6.5 低置信度回退顺序 · 正文/条款 | 4. 人脸是否明显过暗或高光严重溢出。 | FR-05、FR-32 |
| C-0530 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L544 · 11.6.5 低置信度回退顺序 · 正文/条款 | 5. 以上都无可靠证据时保持安静并允许拍照，不用随机姿势填满两步预算。 | FR-05、FR-32 |
| C-0531 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L546 · 11.6.5 低置信度回退顺序 · 正文/条款 | 本节对应 FR-32、FR-33、FR-35 和 UX-39～UX-41；所有自动完成阈值、坐姿/走动准确率、真实帧坐标、多人回退和长期热量在小米 14 Pro 验收前保持 NotRun。 | FR-05、FR-32 |
| C-0532 | [interaction-guidance-and-scenarios.md](../requirements/interaction-guidance-and-scenarios.md) L550 · 11.6.5 低置信度回退顺序 · 正文/条款 | 2026-09-08 曝光与技巧修复：相机曝光 Future 成功后才确认应用结果；新请求及重绑使旧结果失效。窗边/逆光不再自动压暗人物。高光提示需要接近饱和像素比例的证据，不能以顶部平均亮度冒充天空… | FR-05、FR-32 |

## docs/requirements/p1-creative.md

| 单元 | 原文位置 | 摘录（非权威副本） | 逐项审核ID |
| --- | --- | --- | --- |
| C-0533 | [p1-creative.md](../requirements/p1-creative.md) L3 · P1 创意功能需求 · 正文/条款 | - 状态：当前规范附件 | N-44 |
| C-0534 | [p1-creative.md](../requirements/p1-creative.md) L4 · P1 创意功能需求 · 正文/条款 | - 权威入口：产品需求总纲 | N-44 |
| C-0535 | [p1-creative.md](../requirements/p1-creative.md) L5 · P1 创意功能需求 · 正文/条款 | - 原始位置：重构前第 12 节 | N-44 |
| C-0536 | [p1-creative.md](../requirements/p1-creative.md) L6 · P1 创意功能需求 · 正文/条款 | - 迁移方式：Moved；候选、已批准能力、非目标和变更规则完整保留 | N-44 |
| C-0537 | [p1-creative.md](../requirements/p1-creative.md) L8 · P1 创意功能需求 · 正文/条款 | 本文件只有第 12.2 节标记为“本轮已批准”的创意层进入已批准 P1；另有需求入口第 4.6 节独立批准的P1 自然上镜需求（v1）。其他 P1 候选仍受数据与单独批准门禁约束，不能因共处本文件而视… | N-44 |
| C-0538 | [p1-creative.md](../requirements/p1-creative.md) L16 · 12.1 P1 候选 · 正文/条款 | - 样片复刻：通过系统 Photo Picker 导入一张参考图，只用于构图和身体轮廓对齐。 | N-33 |
| C-0539 | [p1-creative.md](../requirements/p1-creative.md) L17 · 12.1 P1 候选 · 正文/条款 | - 连拍选最佳：用户明确启动后连拍，并用端侧信号辅助选择。 | N-33 |
| C-0540 | [p1-creative.md](../requirements/p1-creative.md) L18 · 12.1 P1 候选 · 正文/条款 | - 拍后一句原因：解释刚才为什么走近、放平或调整姿势。 | N-33 |
| C-0541 | [p1-creative.md](../requirements/p1-creative.md) L19 · 12.1 P1 候选 · 正文/条款 | - 第 11.2 节 P1 场景开关。 | N-33 |
| C-0542 | [p1-creative.md](../requirements/p1-creative.md) L20 · 12.1 P1 候选 · 正文/条款 | - 讲解 API 的提示词和供应商替换能力。 | N-33 |
| C-0543 | [p1-creative.md](../requirements/p1-creative.md) L22 · 12.1 P1 候选 · 正文/条款 | P1 不因“已经写进规则库”自动进入排期，必须有 P0 数据支持。 | N-33 |
| C-0544 | [p1-creative.md](../requirements/p1-creative.md) L26 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | 本能力是在不改变既有 P-1 实验结论和硬门禁的前提下单独实施的 P1 增强层，只支持并只验收小米 14 Pro。它解决“拍摄者知道该调什么、能得到克制而可撤销的风格、能从一次明确的三张连拍中自行选片… | FR-23、FR-24、FR-25、FR-26、FR-27、FR-28、FR-29、FR-30、FR-31 |
| C-0545 | [p1-creative.md](../requirements/p1-creative.md) L28 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | 参数教练： | FR-23 |
| C-0546 | [p1-creative.md](../requirements/p1-creative.md) L30 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 使用现有端侧信号与当前绑定相机实际报告的能力，按“无法正常成片 &gt; 曝光/对焦 &gt; 稳定性 &gt; 机位/构图 &gt; 输出偏好”排序，最多提供 3 张建议卡，仅在用户主动打开的独… | FR-23 |
| C-0547 | [p1-creative.md](../requirements/p1-creative.md) L31 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 可以建议：按 ExposureState 步长量化后的精确 EV、点按对焦/测光、AE/AF 锁定或解除、人像/HDR/夜景/普通模式、经小米 14 Pro 标定的焦段与人物距离、靠稳/3 秒倒计… | FR-23 |
| C-0548 | [p1-creative.md](../requirements/p1-creative.md) L32 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 能映射现有控件的建议提供一键应用，应用后显示实际结果；走近、放平、靠稳等物理动作必须同时给出当前原因和明确动作，不能伪装成已自动完成。 | FR-23 |
| C-0549 | [p1-creative.md](../requirements/p1-creative.md) L33 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 不开放或伪造 ISO、快门速度、光圈、白平衡、开尔文值或完整专业模式；当前能力不支持的 EV、扩展模式、焦段或锁定动作不得显示假入口。 | FR-23 |
| C-0550 | [p1-creative.md](../requirements/p1-creative.md) L35 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | 稳定风格： | FR-24 |
| C-0551 | [p1-creative.md](../requirements/p1-creative.md) L39 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  原图  /  CameraX 原始成片  /  始终可切回，不做颜色处理  /  | ST-01 |
| C-0552 | [p1-creative.md](../requirements/p1-creative.md) L40 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  自然人像  /  轻微柔和、克制暖肤色  /  不磨皮、不瘦脸、不识别人  /  | ST-02 |
| C-0553 | [p1-creative.md](../requirements/p1-creative.md) L41 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  柔光人像  /  降低高光反差并轻微褪色  /  只做全图矩阵，不修改面部结构  /  | ST-03 |
| C-0554 | [p1-creative.md](../requirements/p1-creative.md) L42 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  清冷人像  /  轻微冷调、压低饱和但保持肤色可辨  /  不做人脸分区或身份规则  /  | ST-04 |
| C-0555 | [p1-creative.md](../requirements/p1-creative.md) L43 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  清透旅行  /  适度提亮、增加通透和饱和  /  不把天空或肤色推到明显失真  /  | ST-05 |
| C-0556 | [p1-creative.md](../requirements/p1-creative.md) L44 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  森林清新  /  轻微冷绿、柔和对比  /  不识别植物种类，不使用 LUT  /  | ST-06 |
| C-0557 | [p1-creative.md](../requirements/p1-creative.md) L45 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  日落暖金  /  暖金高光、克制饱和  /  不伪造太阳、天空或局部光效  /  | ST-07 |
| C-0558 | [p1-creative.md](../requirements/p1-creative.md) L46 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  美食暖色  /  轻暖、适度饱和与对比  /  不识别菜品或局部重绘  /  | ST-08 |
| C-0559 | [p1-creative.md](../requirements/p1-creative.md) L47 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  都市冷调  /  冷色、较低饱和、清晰反差  /  不建立品牌化“电影”预设  /  | ST-09 |
| C-0560 | [p1-creative.md](../requirements/p1-creative.md) L48 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  夜色霓虹  /  保留暗部、提高色彩与反差  /  不把夜景自动拉成白昼  /  | ST-10 |
| C-0561 | [p1-creative.md](../requirements/p1-creative.md) L49 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  纪实黑白  /  降为黑白、略增对比  /  不改变内容，不生成颗粒或替换细节  /  | ST-11 |
| C-0562 | [p1-creative.md](../requirements/p1-creative.md) L50 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 |  /  高反差黑白  /  黑白并显著拉开明暗  /  保持可逆预览，不裁掉原片动态范围数据  /  | ST-12 |
| C-0563 | [p1-creative.md](../requirements/p1-creative.md) L52 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 预览、导出和编辑必须共享同一套可单测的确定性 4×5 颜色矩阵及强度混合规则；因屏幕色彩管理、CameraX 预览链和 JPEG 编码差异，UI 必须说明“预览为近似效果，导出可能有细微差异”，不… | FR-24 |
| C-0564 | [p1-creative.md](../requirements/p1-creative.md) L53 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 小米广色域差异不由 App 猜测或重解释：原片主 JPEG 的压缩图像字节保持不变；所有派生 JPEG 明确标为兼容 SDR，不宣称 Ultra HDR 已在小米 14 Pro 验收。 | FR-24 |
| C-0565 | [p1-creative.md](../requirements/p1-creative.md) L54 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 十二种风格只做用户主动选择的全图矩阵，不自动套用，也不把“脸框区域颜色变化较小”宣称为肤色保护；局部肤色保护只有在建立脸颊采样、肤色范围和真机样张验收后才能另行立项。 | FR-24 |
| C-0566 | [p1-creative.md](../requirements/p1-creative.md) L55 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 十二种全局风格不使用 CameraEffect、小米私有 API、LUT、FACE_RETOUCH 或重美颜，不能为风格卸载 ImageAnalysis。另行批准的P1 自然上镜需求（v1）是独立… | FR-24 |
| C-0567 | [p1-creative.md](../requirements/p1-creative.md) L57 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | 三张连拍选优： | FR-25 |
| C-0568 | [p1-creative.md](../requirements/p1-creative.md) L59 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 默认关闭；用户在拍摄前明确开启后，一次快门固定顺序捕获三张。捕获期间再次按屏幕或音量键快门不创建第二批。 | FR-25 |
| C-0569 | [p1-creative.md](../requirements/p1-creative.md) L60 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 三张原片全部保存。端侧用确定性的清晰度与曝光合理性进行排序，结果页用“更清晰 / 曝光更稳”等理由标出一张推荐；不显示美学分，不自动删除，用户可选任意一张。 | FR-25 |
| C-0570 | [p1-creative.md](../requirements/p1-creative.md) L61 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 任一张捕获或保存失败时暂停该批后续捕获，保留已成功原片并分别显示已捕获/已保存数量。已有源文件的保存失败提供“重试保存”，只恢复该照片失败的保存阶段，绝不触发相机。无源文件的捕获失败不能称为保存重… | FR-25 |
| C-0571 | [p1-creative.md](../requirements/p1-creative.md) L63 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | 轻量拍后编辑： | FR-26 |
| C-0572 | [p1-creative.md](../requirements/p1-creative.md) L65 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 以用户所选原片为源，支持曝光、对比度、饱和度、色温、色调、褪色和风格强度；每次修改进入不可变历史，支持撤销、重做和一键重置到进入编辑时的状态。 | FR-26 |
| C-0573 | [p1-creative.md](../requirements/p1-creative.md) L66 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 编辑页提供按住或切换“对比原图”，只改变预览，不写入原片或清空历史。 | FR-26 |
| C-0574 | [p1-creative.md](../requirements/p1-creative.md) L67 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 唯一保存动作是“另存副本”；禁止覆盖或删除原片。编辑副本的分辨率若受内存上限降采样，保存前必须在界面说明。 | FR-26 |
| C-0575 | [p1-creative.md](../requirements/p1-creative.md) L68 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 解码按边界读取并设置明确像素/内存上限；中间 Bitmap 及时释放。解码、颜色处理或编码失败时回到原片预览，MediaStore 失败显示可恢复错误。 | FR-26 |
| C-0576 | [p1-creative.md](../requirements/p1-creative.md) L70 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | 非破坏保存与拍后操作： | FR-28、FR-29、FR-30、N-35、N-36 |
| C-0577 | [p1-creative.md](../requirements/p1-creative.md) L72 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 每次单拍或连拍用户请求创建不可复用的 batchId；每个单张捕获请求创建独立不可复用的 captureId，以 batchId + 序号 关联批次。捕获失败后再次拍摄使用新 captureId，… | FR-28、FR-29、FR-30、N-35、N-36 |
| C-0578 | [p1-creative.md](../requirements/p1-creative.md) L73 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 默认“原片优先”：先发布未覆盖的原始 JPEG，再把所选风格和七项编辑参数作为 App 私有配方保存；默认不生成效果 JPEG。只有用户明确点“另存副本”时生成兼容 SDR 效果 JPEG；另提供… | FR-28、FR-29、FR-30、N-35、N-36 |
| C-0579 | [p1-creative.md](../requirements/p1-creative.md) L74 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 保存过程显示空间预检、原片发布、配方保存、可选效果生成/发布等分阶段状态。发生部分成功时保留已成功项，只重试失败阶段；原片发布成功后不得因配方或效果失败而删除或重写。 | FR-28、FR-29、FR-30、N-35、N-36 |
| C-0580 | [p1-creative.md](../requirements/p1-creative.md) L75 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 发布写入 DATE_TAKEN。派生 JPEG 只复制方向、尺寸、时间和相机厂商/型号等安全 EXIF；不复制定位、缩略图、MakerNote 或可能包含厂商私有数据的未知标签。 | FR-28、FR-29、FR-30、N-35、N-36 |
| C-0581 | [p1-creative.md](../requirements/p1-creative.md) L76 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - App 私有恢复记录在进入每个不可逆阶段前持久化。恢复键使用 captureId + 资产角色/derivativeId + 阶段，普通 JPEG 与 Motion Photo 是同一主资产的互斥… | FR-28、FR-29、FR-30、N-35、N-36 |
| C-0582 | [p1-creative.md](../requirements/p1-creative.md) L77 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 保存前按原片、临时峰值与可选副本估算可用空间；不足时在捕获/发布前给可见错误。效果副本提供“完整质量”和“省空间兼容”两档，两者均为兼容 SDR 且不得修改原片。 | FR-28、FR-29、FR-30、N-35、N-36 |
| C-0583 | [p1-creative.md](../requirements/p1-creative.md) L78 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 拍后可对本 App 本次得到的 URI 执行打开、系统分享、收藏和移到系统回收站；不扫描或读取整本相册，不新增定位权限。平台不支持收藏/回收站请求时给出可见说明，不伪装成功。 | FR-28、FR-29、FR-30、N-35、N-36 |
| C-0584 | [p1-creative.md](../requirements/p1-creative.md) L79 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 拍照成功后无需用户确认，原片或 Motion Photo 完成适用发布校验后立即反馈；全屏选片/编辑页不得自动弹出或阻塞下一次快门。配方/效果失败与原片成功分别显示，不能把原片已存说成整张未存。用… | FR-28、FR-29、FR-30、N-35、N-36 |
| C-0585 | [p1-creative.md](../requirements/p1-creative.md) L81 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | 无声 Live（Android Motion Photo）： | FR-31、N-37、N-38 |
| C-0586 | [p1-creative.md](../requirements/p1-creative.md) L83 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 这是 Android 官方 Motion Photo Format 1.0，不命名或冒充 Apple Live Photo。Live 为显式开关，默认无声；绝不新增 RECORD_AUDIO 权限… | FR-31、N-37、N-38 |
| C-0587 | [p1-creative.md](../requirements/p1-creative.md) L84 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 能力允许时绑定 Preview + ImageAnalysis + ImageCapture + VideoCapture，继续实时指导和手动快门。绑定前用 CameraX SessionConf… | FR-31、N-37、N-38 |
| C-0588 | [p1-creative.md](../requirements/p1-creative.md) L85 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 开关启用后维护有严格大小和时长上限的循环临时无声 MP4。缓存充分时，按快门以 CameraX 原始 JPEG 为高分辨率封面，保留快门前后各约 1.5 秒并裁剪为约 3 秒。刚开启、缓存轮换或重… | FR-31、N-37、N-38 |
| C-0589 | [p1-creative.md](../requirements/p1-creative.md) L86 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - Motion Photo 封面显式使用普通 SDR JPEG，不请求 Ultra HDR。主文件按照官方 v1 和小米 14 Pro 原生样本：向原始 JPEG 写入 GCamera:Motion… | FR-31、N-37、N-38 |
| C-0590 | [p1-creative.md](../requirements/p1-creative.md) L87 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - 打包、XMP、裁剪或发布任一阶段失败时，清除不完整容器和 pending 行，并可靠发布原始普通 JPEG；恢复记录保证进程重启不重复发布。所有成功、失败、取消和恢复路径都清理超时/超量临时视频。 | FR-31、N-37、N-38 |
| C-0591 | [p1-creative.md](../requirements/p1-creative.md) L88 · 12.2 小米 14 Pro P1 创意层（本轮已批准） · 正文/条款 | - Live 主文件保持原始封面颜色；风格和编辑只生成可选静态兼容 SDR 派生副本，避免封面与视频颜色不一致。未在小米 14 Pro 相册验证前不得宣称系统可播放、Ultra HDR 保留或 Liv… | FR-31、N-37、N-38 |
| C-0592 | [p1-creative.md](../requirements/p1-creative.md) L92 · 12.2.1 P1 第二阶段强化 · 正文/条款 | 本节与第 11.6 节六类姿势和摄影技巧一起构成 2026-09-01 登记的第二阶段 ApprovedSeparate P1。实施顺序为“正确性门禁 → 功能交付 → 校准后开放”；这些名称表示实施… | FR-31、N-37、N-38 |
| C-0593 | [p1-creative.md](../requirements/p1-creative.md) L96 · 12.2.1 P1 第二阶段强化 · 正文/条款 |  /  正确性门禁  /  分析/预览坐标一致、低置信度与多人安全回退、MediaStore 发布前后校验、Live 单文件普通 JPEG 回退  /  未完成不得扩大姿势、风格或 Live 宣称  … | FR-34、FR-35、FR-36、FR-37 |
| C-0594 | [p1-creative.md](../requirements/p1-creative.md) L97 · 12.2.1 P1 第二阶段强化 · 正文/条款 |  /  功能交付  /  六类姿势与摄影技巧、场景化风格推荐、强度/最近/收藏、人像保守强度、资产校验和热降级  /  与 P-1 数据隔离；全部小米 14 Pro 专项保持独立验收  /  | FR-34、FR-35、FR-36、FR-37 |
| C-0595 | [p1-creative.md](../requirements/p1-creative.md) L98 · 12.2.1 P1 第二阶段强化 · 正文/条款 |  /  校准后开放  /  坐姿/走动自动完成、局部肤色处理、复杂表情研究、Ultra HDR Motion Photo  /  缺目标机、样张或相册证据时不得开启或对外承诺  /  | FR-34、FR-35、FR-36、FR-37 |
| C-0596 | [p1-creative.md](../requirements/p1-creative.md) L100 · 12.2.1 P1 第二阶段强化 · 正文/条款 | 场景化风格推荐： | FR-34 |
| C-0597 | [p1-creative.md](../requirements/p1-creative.md) L102 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 原图始终排第一；系统最多给出三个推荐风格，每个只显示一条基于场景、亮度、脸数、高光风险或用户偏好的可解释原因。 | FR-34 |
| C-0598 | [p1-creative.md](../requirements/p1-creative.md) L103 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 推荐只改变候选排序，不自动套用、不自动保存派生图，也不影响普通快门。用户主动选择后才写入当前配方。 | FR-34 |
| C-0599 | [p1-creative.md](../requirements/p1-creative.md) L104 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 每个风格增加场景标签、人像风险、暗光风险、高光风险、建议初始强度和经目标机校准的安全范围；现有统一 1.0 强度不能继续作为所有风格的产品默认值。 | FR-34 |
| C-0600 | [p1-creative.md](../requirements/p1-creative.md) L105 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 最近使用最多保存五个风格 ID；收藏只保存用户明确选择的风格 ID 集合。最近、收藏和推荐均不关联照片 URI、人脸、身份或长期人物画像。 | FR-34 |
| C-0601 | [p1-creative.md](../requirements/p1-creative.md) L106 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 选择器按“推荐 / 最近 / 收藏 / 全部”组织；原图对比、撤销、重做和重置继续复用现有非破坏编辑行为。 | FR-34 |
| C-0602 | [p1-creative.md](../requirements/p1-creative.md) L108 · 12.2.1 P1 第二阶段强化 · 正文/条款 | 人像保守强度： | FR-35 |
| C-0603 | [p1-creative.md](../requirements/p1-creative.md) L110 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 检测到单张可靠人脸时，可以比较变换前后脸部核心区域的亮度、色相、色度和剪裁比例；超过校准风险区间时只降低全图风格强度或回到原图。 | FR-35 |
| C-0604 | [p1-creative.md](../requirements/p1-creative.md) L111 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 该能力不得改变面部结构、磨皮、瘦脸、分区生成内容或推断真实肤色；脸部采样不足、侧脸、多人或强混合光时保持 Unknown。 | FR-35 |
| C-0605 | [p1-creative.md](../requirements/p1-creative.md) L112 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 在覆盖不同光照、肤色、屏幕色彩管理和 HyperOS 相册观感的受控样张通过前，UI 使用“人像保守强度”而不是“肤色保护”。局部颜色蒙版和脸缘融合属于“校准后开放”范围；第 4.6 节自然上镜仅… | FR-35 |
| C-0606 | [p1-creative.md](../requirements/p1-creative.md) L114 · 12.2.1 P1 第二阶段强化 · 正文/条款 | 保存完整性： | FR-36 |
| C-0607 | [p1-creative.md](../requirements/p1-creative.md) L116 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 保存阶段细化为 CaptureTemp → SpaceCheck → MotionPack（可选）→ MediaStoreInsertPending → OriginalCopy → Verify… | FR-36 |
| C-0608 | [p1-creative.md](../requirements/p1-creative.md) L117 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - VerifyPending 至少检查非零长度、JPEG 头尾、可读尺寸和方向/EXIF；失败时删除 pending 行并保留可恢复源文件，不能向 UI 报成功。 | FR-36 |
| C-0609 | [p1-creative.md](../requirements/p1-creative.md) L118 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - VerifyPublished 重新打开最终 URI，核对 MIME、显示名、相册相对路径、非零长度、尺寸和方向。Motion Photo 还须验证只有一个 Primary、一个 MotionPh… | FR-36 |
| C-0610 | [p1-creative.md](../requirements/p1-creative.md) L119 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - journal 增加 schemaVersion、captureId、输入临时文件、输出 URI、长度和阶段重试信息。恢复只跳过已验证成功阶段；原片成功、配方或副本失败时继续保留原片。 | FR-36 |
| C-0611 | [p1-creative.md](../requirements/p1-creative.md) L120 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 原始 CameraX JPEG 仍按字节发布，不经 Bitmap 重编码；派生图继续使用安全 EXIF 白名单。横竖屏及 0/90/180/270 度方向必须纳入目标机验收。 | FR-36 |
| C-0612 | [p1-creative.md](../requirements/p1-creative.md) L122 · 12.2.1 P1 第二阶段强化 · 正文/条款 | Motion Photo 兼容边界： | FR-31 |
| C-0613 | [p1-creative.md](../requirements/p1-creative.md) L124 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - P1 继续只承诺无音轨、标准 Photo、普通 SDR JPEG、单一 MP.JPG 和 HD→SD→普通 JPEG 的降级链。 | FR-31 |
| C-0614 | [p1-creative.md](../requirements/p1-creative.md) L125 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - Android Motion Photo 规范允许带 GainMap 的 Ultra HDR 主图，但当前实现拒绝不能安全合并的 GainMap/XMP 并发布未改写普通 JPEG；这属于保守版本… | FR-31 |
| C-0615 | [p1-creative.md](../requirements/p1-creative.md) L126 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 只有完成 GainMap 目录项、XMP 合并、编码器输出和 HyperOS 相册识别/播放专项后，才可在“校准后开放”环节评估 Ultra HDR Motion Photo。 | FR-31 |
| C-0616 | [p1-creative.md](../requirements/p1-creative.md) L128 · 12.2.1 P1 第二阶段强化 · 正文/条款 | 性能、功耗与热： | FR-37 |
| C-0617 | [p1-creative.md](../requirements/p1-creative.md) L130 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 分析继续使用 latest-only 和有上限的亮度/关键点数据；Face/Pose、风格预览、导出、连拍和 Live 不得共用会积压帧的无界队列。 | FR-37 |
| C-0618 | [p1-creative.md](../requirements/p1-creative.md) L131 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - Normal 状态允许标准分析和用户显式开启的创意功能；Light/Moderate 降低 Pose 与背景分析频率、暂停不必要的实时风格预览并优先使用 SD Live。 | FR-37 |
| C-0619 | [p1-creative.md](../requirements/p1-creative.md) L132 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - Severe 状态只保留 Face、构图、水平和曝光等基础分析，完成当前保存后禁止新增长连拍或 Live；Critical 暂停非必要 ML/创意处理，相机仍可用时优先保留普通预览、快门及原片保存… | FR-37 |
| C-0620 | [p1-creative.md](../requirements/p1-creative.md) L133 · 12.2.1 P1 第二阶段强化 · 正文/条款 | - 热状态不支持、返回未知或无真机证据时，不把“未报告过热”写成通过。小米 14 Pro 连续取景、连拍、导出和 Live 的内存峰值、耗时、温升与降级恢复全部按目标机矩阵验收。 | FR-37 |
| C-0621 | [p1-creative.md](../requirements/p1-creative.md) L135 · 12.2.1 P1 第二阶段强化 · 正文/条款 | 本节对应 FR-34、FR-36、FR-37 和 UX-42～UX-45。代码路径存在、JVM 测试通过、模拟器可运行均不能替代真实颜色、EXIF、HyperOS 相册、Motion Photo 播放… | FR-37 |
| C-0622 | [p1-creative.md](../requirements/p1-creative.md) L139 · 12.3 第一版明确不做 · 正文/条款 | - 生成换姿势、换脸或用生成图替换原片。 | N-43 |
| C-0623 | [p1-creative.md](../requirements/p1-creative.md) L140 · 12.3 第一版明确不做 · 正文/条款 | - P-1/P0 不做重度美颜、自研滤镜、LUT、FACE_RETOUCH 或风格预设；独立批准的 P1 例外仅为第 12.2 节十二种参数化风格和需求入口第 4.6 节自然上镜 v1，两者均默认不生… | N-43 |
| C-0624 | [p1-creative.md](../requirements/p1-creative.md) L141 · 12.3 第一版明确不做 · 正文/条款 | - RAW、直方图、单反/微单教学和全品牌镜头数据库。 | N-43 |
| C-0625 | [p1-creative.md](../requirements/p1-creative.md) L142 · 12.3 第一版明确不做 · 正文/条款 | - 模板信息流、摄影网课、评分水印、对齐百分比、等级、挑战和签到。 | N-43 |
| C-0626 | [p1-creative.md](../requirements/p1-creative.md) L143 · 12.3 第一版明确不做 · 正文/条款 | - 按男/女或网红风维护数百姿势。 | N-43 |
| C-0627 | [p1-creative.md](../requirements/p1-creative.md) L144 · 12.3 第一版明确不做 · 正文/条款 | - 达标自动拍、后台连续拍或用户无法立即停止的自动连拍。 | N-43 |
| C-0628 | [p1-creative.md](../requirements/p1-creative.md) L145 · 12.3 第一版明确不做 · 正文/条款 | - 同步开发 iOS。 | N-43 |
| C-0629 | [p1-creative.md](../requirements/p1-creative.md) L146 · 12.3 第一版明确不做 · 正文/条款 | - 首版菜单放不可用的“合影 / 即将推出”。 | N-43 |
| C-0630 | [p1-creative.md](../requirements/p1-creative.md) L147 · 12.3 第一版明确不做 · 正文/条款 | - 第一版不申请麦克风、定位或读取整本相册；无声 Motion Photo 不构成麦克风权限例外。 | N-43 |
| C-0631 | [p1-creative.md](../requirements/p1-creative.md) L148 · 12.3 第一版明确不做 · 正文/条款 | - 折叠外屏和平板作为发布阻断验收机型。 | N-43 |
| C-0632 | [p1-creative.md](../requirements/p1-creative.md) L152 · 12.4 决策变更规则 · 正文/条款 | - 增加 P-1 页面、权限、必做步骤、测试场景或网络依赖，必须重新评估第 3.2 节实验是否仍可比。 | N-44 |
| C-0633 | [p1-creative.md](../requirements/p1-creative.md) L153 · 12.4 决策变更规则 · 正文/条款 | - 将 P1 或后期能力提前，必须写明它解决的已验证问题和对应指标。 | N-44 |
| C-0634 | [p1-creative.md](../requirements/p1-creative.md) L154 · 12.4 决策变更规则 · 正文/条款 | - 把任何其他 Android 机型加入 P-1、P0 或 P1 都属于范围扩展，须单独立项并新增对应真机回归，不能用模拟器或“小米 14 Pro 已通过”替代。 | N-44 |
| C-0635 | [p1-creative.md](../requirements/p1-creative.md) L155 · 12.4 决策变更规则 · 正文/条款 | - 市场出现相同功能不会自动触发跟进；只根据用户完成效率、成片偏好、复用和可靠性调整。 | N-44 |
| C-0636 | [p1-creative.md](../requirements/p1-creative.md) L156 · 12.4 决策变更规则 · 正文/条款 | - 实现遇到限制时，先回到本文修改产品取舍，再更新架构，不能由代码静默改变需求。 | N-44 |
| C-0637 | [p1-creative.md](../requirements/p1-creative.md) L160 · 12.4 决策变更规则 · 正文/条款 | 2026-09-12 保存合同补充：承诺可重试的原片/副本源和日志须使用受配额管理的持久私有暂存，排除云备份与设备迁移，不仅存可回收 cache。发布成功或用户明确放弃前不主动清除必要源；源丢失须明确… | N-44 |

## docs/requirements/p1-beauty.md

| 单元 | 原文位置 | 摘录（非权威副本） | 逐项审核ID |
| --- | --- | --- | --- |
| C-0638 | [p1-beauty.md](../requirements/p1-beauty.md) L3 · P1 自然上镜需求（v1） · 正文/条款 | Scope：ApprovedSeparate；2026-09-03 用户批准执行。最高入口：产品需求总纲。不是对全部局部颜色处理或 P0 的批准。 | FR-38、N-40 |
| C-0639 | [p1-beauty.md](../requirements/p1-beauty.md) L5 · P1 自然上镜需求（v1） · 正文/条款 | - 当前后摄单人标准 Photo，默认 OFF。创意菜单主动选择 NATURAL / SOFT，重置回 OFF。开启前说明复用本机关键点、不上传、原片优先、效果需另存及预览近似。与 Live、扩展模式… | FR-38、N-40 |
| C-0640 | [p1-beauty.md](../requirements/p1-beauty.md) L6 · P1 自然上镜需求（v1） · 正文/条款 | - 仅平滑脸颊、额头、下巴纹理，软边融合并排除眼眉、鼻部、嘴部。不做肤色分类、瘦脸、大眼、美白、妆容、情绪/身份/外貌推断、LUT、私有 API 或生成重绘。Face 快照仅内存，不保存坐标、帧或身份… | FR-38、N-40 |
| C-0641 | [p1-beauty.md](../requirements/p1-beauty.md) L7 · P1 自然上镜需求（v1） · 正文/条款 | - 缺关键点、多人、侧脸（初始  / yaw /  &gt; 35°）、时间戳/变换无效时退到零；150ms 后衰减，300ms 完全退出。NATURAL 融合 0.18 / 细节保留 0.70；SO… | FR-38、N-40 |
| C-0642 | [p1-beauty.md](../requirements/p1-beauty.md) L8 · P1 自然上镜需求（v1） · 正文/条款 | - 热策略暂停非必要预览时也暂停美颜并说明；效果错误可见回退标准预览。关闭时不绑定效果。原图发布先于成片检测/美颜，捕获固定预设与引擎版本，重试与另存用对应快照。 | FR-38、N-40 |
| C-0643 | [p1-beauty.md](../requirements/p1-beauty.md) L9 · P1 自然上镜需求（v1） · 正文/条款 | - 默认只原片和私有配方，明确另存或 ORIGINAL_AND_EFFECT 才生成独立 SDR JPEG。在有界解码后的拍摄图重新检测，异常报可恢复失败；无有效单人时不应用美颜并给说明。旧配方/日志… | FR-38、N-40 |
| C-0644 | [p1-beauty.md](../requirements/p1-beauty.md) L10 · P1 自然上镜需求（v1） · 正文/条款 | - 保持原有风格/七项编辑。拍后原图对比仍为真原图；拍后未渲染美颜的预览明确提示“美颜将在另存时应用”。P-1 对照实验关闭美颜。遮挡、胡须、眼镜、发丝、肤色、光照及镜头/画幅需专项校准，不声称准确肤… | FR-38、N-40 |
| C-0645 | [p1-beauty.md](../requirements/p1-beauty.md) L12 · P1 自然上镜需求（v1） · 正文/条款 | FR/UX 唯一定义见功能需求定义与追踪矩阵和分阶段验收规范。真实效果、性能与目标机验收均为 NotRun。 | FR-38、N-40 |
| C-0646 | [p1-beauty.md](../requirements/p1-beauty.md) L14 · P1 自然上镜需求（v1） · 正文/条款 | 入口与反馈修复（2026-09-03）：取景器入口明确显示“美颜·风格”，启用后显示“美颜·自然/柔和”，不再以“原图”隐藏美颜入口。菜单内持续显示不兼容原因，并提供用户显式点击的“关闭 Live”和… | FR-38、N-40 |
| C-0647 | [p1-beauty.md](../requirements/p1-beauty.md) L16 · P1 自然上镜需求（v1） · 正文/条款 | 反馈修复（2026-09-05）：关闭时入口明确显示「美颜·关闭」，颜色风格不代表开启美颜；启用后的实际处理/等待/暂停状态在取景器持续可见，不只藏在菜单中。仍保留默认 OFF 和独立选择，不因选择自… | FR-38、N-40 |

## docs/requirements/deferred-scope.md

| 单元 | 原文位置 | 摘录（非权威副本） | 逐项审核ID |
| --- | --- | --- | --- |
| C-0648 | [deferred-scope.md](../requirements/deferred-scope.md) L3 · 后期功能范围 · 正文/条款 | - 状态：当前规范附件 | N-44 |
| C-0649 | [deferred-scope.md](../requirements/deferred-scope.md) L4 · 后期功能范围 · 正文/条款 | - 权威入口：产品需求总纲 | N-44 |
| C-0650 | [deferred-scope.md](../requirements/deferred-scope.md) L5 · 后期功能范围 · 正文/条款 | - 原始位置：重构前第 13、14 节 | N-44 |
| C-0651 | [deferred-scope.md](../requirements/deferred-scope.md) L6 · 后期功能范围 · 正文/条款 | - 迁移方式：Moved；保留方向，不创建第一版入口 | N-44 |
| C-0652 | [deferred-scope.md](../requirements/deferred-scope.md) L8 · 后期功能范围 · 正文/条款 | 本附件全部属于“后期”；其中 P0 代拍简化页仍以交互规格第 6.7 节为准，不等于被拍者主用户或互拍入口已经获批。 | N-44 |
| C-0653 | [deferred-scope.md](../requirements/deferred-scope.md) L14 · 13. 合影：后期保留，第一版不做 · 正文/条款 | 第一版界面和验收不出现合影模式。拍到两张脸以上时，只做安全回退：提示是否有人被切掉、画面是否倾斜和曝光是否严重异常，不进入单人姿势。 | N-41 |
| C-0654 | [deferred-scope.md](../requirements/deferred-scope.md) L16 · 13. 合影：后期保留，第一版不做 · 正文/条款 | 后期按顺序评估： | N-41 |
| C-0655 | [deferred-scope.md](../requirements/deferred-scope.md) L18 · 13. 合影：后期保留，第一版不做 · 正文/条款 | 1. 数脸和切边：提示靠近、高个往后、镜头抬高、看镜头；用户明确开启后连拍，用端侧睁眼信号辅助选片，不合成新脸。 | N-41 |
| C-0656 | [deferred-scope.md](../requirements/deferred-scope.md) L19 · 13. 合影：后期保留，第一版不做 · 正文/条款 | 2. 二人和三至四人半透明模板：允许拖动缩放，不做多人匹配分。 | N-41 |
| C-0657 | [deferred-scope.md](../requirements/deferred-scope.md) L20 · 13. 合影：后期保留，第一版不做 · 正文/条款 | 3. 多人实时骨骼：只有真机通过遮挡、远距离、小孩和高低排测试后才立项。 | N-41 |
| C-0658 | [deferred-scope.md](../requirements/deferred-scope.md) L22 · 13. 合影：后期保留，第一版不做 · 正文/条款 | 现在只预留场景枚举 group、检测到的脸数量和可复用的儿童同意模块，不在第一版渲染入口。 | N-41 |
| C-0659 | [deferred-scope.md](../requirements/deferred-scope.md) L30 · 14. 角色切换：后期保留，第一版不做入口 · 正文/条款 |  /  拍摄者主用户  /  拍摄者  /  构图、曝光、焦段和当前动作  /  P-1 / P0  /  | N-42 |
| C-0660 | [deferred-scope.md](../requirements/deferred-scope.md) L31 · 14. 角色切换：后期保留，第一版不做入口 · 正文/条款 |  /  代拍简化页  /  路人或朋友  /  轮廓、大快门、音量键提示  /  P0  /  | N-42 |
| C-0661 | [deferred-scope.md](../requirements/deferred-scope.md) L32 · 14. 角色切换：后期保留，第一版不做入口 · 正文/条款 |  /  被拍者主用户  /  被拍者自己或支架  /  自己对齐轮廓和姿势  /  后期  /  | N-42 |
| C-0662 | [deferred-scope.md](../requirements/deferred-scope.md) L33 · 14. 角色切换：后期保留，第一版不做入口 · 正文/条款 |  /  互拍对调  /  两人换手  /  在拍摄者与代拍状态间切换  /  后期  /  | N-42 |
| C-0663 | [deferred-scope.md](../requirements/deferred-scope.md) L35 · 14. 角色切换：后期保留，第一版不做入口 · 正文/条款 | 第一版不做“我是被拍者”、互拍向导或角色设置页。现在只要求每条口令带听众字段，为后续切换保留数据结构。 | N-42 |

## docs/acceptance/acceptance-plan.md

| 单元 | 原文位置 | 摘录（非权威副本） | 逐项审核ID |
| --- | --- | --- | --- |
| C-0664 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L3 · 分阶段验收规范 · 正文/条款 | - 状态：当前规范附件；UX 权威定义唯一位置 | N-44 |
| C-0665 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L4 · 分阶段验收规范 · 正文/条款 | - 权威入口：产品需求总纲 | N-44 |
| C-0666 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L5 · 分阶段验收规范 · 正文/条款 | - 原始位置：重构前第 9 节 | N-44 |
| C-0667 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L6 · 分阶段验收规范 · 正文/条款 | - 迁移方式：Moved + Consolidated；UX 文字不改，按真实阶段重新分表 | N-44 |
| C-0668 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L8 · 分阶段验收规范 · 正文/条款 | 本文件是 UX-01～UX-49 的唯一当前权威定义。P-1 验收区不包含已批准 P1 创意层、第二阶段和自然上镜用例；历史快照仅供迁移核对。UX-06 与 UX-31 的低置信度边界已按 需求决策与… | N-44 |
| C-0669 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L14 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-01 首次同意  /  新安装后打开  /  先看到端侧分析说明，再出现系统相机权限  /  | UX-01 |
| C-0670 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L15 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-02 拒绝权限  /  拒绝 CAMERA  /  不黑屏；显示原因和系统设置入口  /  | UX-02 |
| C-0671 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L16 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-03 意图锁定  /  选择“人带景”后让脸变大或变小  /  本次会话不自动改回人物特写；同一相机会话连续拍摄/重绑仍保持手选意图，新照片只重置两步预算  /  | UX-03 |
| C-0672 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L17 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-04 两步指导  /  正常完成、跳过、超时或关闭两种提示通道，分别构造最低条件成立、不足和人物丢失  /  最多 1/2、2/2；所有结束分支执行交互第 6.2 节统一判定；条件成立才… | UX-04 |
| C-0673 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L18 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-05 提前快门  /  在 1/2 或 2/2 时按快门  /  立即捕获，不弹拦截提示  /  | UX-05 |
| C-0674 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L19 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-06 无建议  /  可靠单人但无建议、无 subject 候选、附加建议结束；分别切换必要信号有效/缺失/过期  /  1.5 秒内结束无建议观察；统一结束判定，不循环加载，不因缺候选… | UX-06 |
| C-0675 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L20 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-07 提示方式  /  依次选择语音+字幕、仅语音、仅字幕、提示关闭并重启 App  /  四种组合立即生效；设置跨启动保持；拍摄者动作卡始终可见  /  | UX-07 |
| C-0676 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L21 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-08 目标机焦段  /  在小米 14 Pro 切换所有已验收快捷焦段  /  标签、预览视角和成片一致；未验收倍率不显示，不建议数码猛拉  /  | UX-08 |
| C-0677 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L22 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-09 保存成功  /  连续拍照  /  每张都给可见成功反馈，系统相册可见无水印原图；完成适用发布校验才报成功；配方/效果失败仍明确原片已保存  /  | UX-09 |
| C-0678 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L23 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-10 保存失败  /  模拟写入失败  /  保留错误与“重试保存”，不能静默丢图；补缓存清理/低空间/杀进程；有持久源才重试，无源明确不可恢复且不再拍  /  | UX-10 |
| C-0679 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L24 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-11 飞行模式  /  断网后完整拍摄  /  意图、指导、快门和保存全部可用；补新安装首次启动前断网、无中文离线音色；语音按设置降级但主路径仍须完成  /  | UX-11 |
| C-0680 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L25 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-12 旋转  /  竖屏与横屏分别完成一张  /  控件不重叠，快门与跳过始终可达  /  | UX-12 |
| C-0681 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L26 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-13 缩放  /  在小米 14 Pro 上双指放大再缩小  /  预览连续响应，1x 重置可用，不出现未经验收的快捷焦段按钮  /  | UX-13 |
| C-0682 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L27 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-14 最近照片  /  保存成功后点击缩略图  /  打开系统图片查看器；无可用查看器时给出可见提示  /  | UX-14 |
| C-0683 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L28 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-15 竖屏紧凑布局  /  在小米 14 Pro 竖屏完成一张  /  操作区保持两排且默认字号下不超过约四分之一取景器高度  /  | UX-15 |
| C-0684 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L29 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-16 字幕生命周期  /  仅字幕模式完成、保持、跳过和更换被拍者动作  /  字幕不按 2.5 秒消失；动作完成/失效、替换、跳过、拍照或关闭字幕时正确清除  /  | UX-16 |
| C-0685 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L30 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-17 实时口令  /  连续改变远近、水平和单人姿势，并保持每次变化超过 600ms  /  旧口令完成或失效后不残留；新口令稳定后更新；单帧抖动不跳字；同句和反向口令满足限流；计数帧捕… | UX-17 |
| C-0686 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L31 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-18 AE/AF 锁定  /  长按人物后改变构图，再点“解除”  /  锁定成功/失败和解除均有文字状态；解除后恢复自动；重绑不会虚报仍锁定；AF和AE分别检查支持、请求、确认、失败；… | UX-18 |
| C-0687 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L32 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-19 构图辅助  /  分别关闭网格和水平仪并重启  /  对应叠线立即消失，另一项不受影响；重启后保持  /  | UX-19 |
| C-0688 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L33 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-20 倒计时  /  选择 3 秒/10 秒，用屏幕与音量键触发并中途取消  /  数字逐秒可见，到零只拍一张；再次按快门取消；设置跨启动保持  /  | UX-20 |
| C-0689 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L34 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-21 画幅  /  在 4:3 与 16:9 间切换并各拍一张  /  三用例按同一画幅策略重绑，预览不黑屏，成片画幅匹配选择  /  | UX-21 |
| C-0690 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L35 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-22 拍摄偏好  /  画质优先、速度优先各连续拍摄  /  分别使用质量优先和低延迟捕获；实时分析与保存均不中断；画质优先不表示等待合焦才可按快门  /  | UX-22 |
| C-0691 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L36 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-23 能力模式  /  枚举并逐一切换界面列出的模式  /  只列出可与分析共存的模式；任一绑定失败回到普通拍照并可继续拍  /  | UX-23 |
| C-0692 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L37 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-24 设置恢复  /  修改全部新增设置、重启，再一键恢复默认  /  重启保持；一键恢复网格/水平仪开、倒计时关、4:3、画质优先、自动模式和语音+字幕，不清除相机说明同意；新照片只重… | UX-24 |
| C-0693 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L38 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-25 镜头检查  /  连续遮挡镜头、解除遮挡，再拍普通暗景  /  连续稳定后才显示“可能被挡住或弄脏”；解除后清除；普通暗景不误报  /  | UX-25 |
| C-0694 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L39 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-31 未识别人物  /  分别构造Face漏检但新鲜Pose可靠、启动即实际遮脸、跟踪中遮脸使Pose失效、Face/Pose都无效及恢复露脸  /  新鲜可靠Pose存在时可更新保守姿… | UX-31 |
| C-0695 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L40 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-32 长屏满幅预览  /  在小米 14 Pro 竖屏切换 4:3 / 16:9  /  预览延伸到紧凑底部操作区背后，操作区上方没有大块黑色空带；叠线、点按对焦和分析坐标与可见裁切一致… | UX-32 |
| C-0696 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L41 · 1. P-1 端到端用例 · 正文/条款 |  /  UX-38 保守感知边界  /  构造真实脸框明暗差、仅改变脸框高度、低微笑概率、单帧髋/脚几何和两张脸画面  /  只有真实脸框内外亮度差触发逆光；位置、低微笑和单帧重心不产生纠正；多人只出… | UX-38 |
| C-0697 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L45 · 2. 已批准 P1 创意层用例 · 正文/条款 | 这些用例不进入 P-1 对照实验，不改变 P-1 Go/No-Go，也不能用来扩大完整 P0。 | N-46 |
| C-0698 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L49 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-26 十二种风格  /  依次切换十二种风格、切回原图并预览/另存  /  预览立即可撤回；预览/编辑/导出矩阵确定一致；默认不生成效果 JPEG，明确另存后才得到兼容 SDR 副本  … | UX-26 |
| C-0699 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L50 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-27 明确三张连拍  /  开关三张连拍、捕获中重按快门、逐张制造捕获/保存失败，再重试保存、继续余下拍摄或结束本批  /  关闭只拍一张；开启最多三个有效序号且防重入；保留已捕获原片并… | UX-27 |
| C-0700 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L51 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-28 轻量编辑  /  调整曝光、对比度、饱和度、色温、色调、褪色和强度，依次对比原图、撤销、重做、重置并另存  /  历史结果确定；对比不改历史；另存得到兼容 SDR MediaSto… | UX-28 |
| C-0701 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L52 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-29 创意失败回退  /  模拟超出解码上限、解码失败、效果导出失败和 MediaStore 失败  /  不崩溃、不占满内存；有原片时回退原片；保存失败给可见错误且不把失败说成成功；可… | UX-29 |
| C-0702 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L53 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-30 非目标设备降级  /  在非目标设备或效果能力不足环境执行非阻断健壮性检查  /  只用标准能力探测；有能力时尝试普通拍摄与原片保存，无能力或失败时可见说明；创意能力标为未验证/不… | UX-30 |
| C-0703 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L54 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-33 参数建议与一键应用  /  主动打开参数面板，构造候选并逐条应用，期间拍照、关闭并重开面板  /  面板最多三项，取景器不同时显示第二组主要动作；打开面板暂停指导卡/subject… | UX-33 |
| C-0704 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L55 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-34 分阶段保存  /  模拟原片/配方/多个另存副本失败，跨单张/批次重试及进程重启，并提供身份不明确的旧日志  /  按单张 captureId、资产身份及阶段恢复；不同照片和副本不… | UX-34 |
| C-0705 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L56 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-35 拍后操作  /  对刚保存 URI 依次打开、分享、收藏和移到回收站  /  只作用于本 App 已知 URI；平台确认流程可见；不申请读相册或定位；不支持时不假成功  /  | UX-35 |
| C-0706 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L57 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-36 Live 正常路径  /  开启 Live 后保持预览，按一次手动快门  /  会话预检通过后按 HD→SD 尝试四用例并继续指导；发布一个以 MVIMG_ 开头且以 MP.JPG… | UX-36 |
| C-0707 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L58 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-37 Live 失败回退  /  分别模拟会话预检、HD/SD 绑定、编码器、空间、裁剪、普通 XMP、混合元数据的 Motion XMP、GainMap、发布失败和进程恢复  /  开… | UX-37 |
| C-0708 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L59 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-39 六类姿势  /  依次选择特写、半身、全身、坐姿、走动和单人互动，并完成、保持、失效、超时和跳过  /  每类只显示适用短口令；有可观察证据的动作在稳定改善后退出；灵感卡不伪装自动… | UX-39 |
| C-0709 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L60 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-40 低置信度、多人和表情边界  /  构造关键点缺失、遮挡、两张脸、瞬时眨眼、低微笑、侧脸和恢复  /  只按安全顺序给入框、切边、水平或严重曝光；多人不进入单人 Pose；不输出情绪… | UX-40 |
| C-0710 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L61 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-41 摄影技巧  /  构造头脚/关节贴边、逆光、错误距离、未标定焦段、杂乱背景、暗景和运动  /  只显示画面可观察或当前公开能力可执行的动作；未标定焦段、精确机位高度、真实深度和系统… | UX-41 |
| C-0711 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L62 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-42 风格发现与偏好  /  在不同场景打开推荐，依次选择风格、修改强度、切回原图、加入/取消收藏并重启  /  原图始终第一；推荐最多三个且给出一条原因，不自动套用或生成副本；最近最多… | UX-42 |
| C-0712 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L63 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-43 人像保守强度  /  在日光、暖光、逆光、暗光、侧脸、多人和采样不足时切换十二种风格及高强度  /  单张可靠人脸时过度颜色/亮度变化会降低全图强度或回原图；未知条件不声称保护；无… | UX-43 |
| C-0713 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L64 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-44 资产完整性与恢复  /  分别破坏临时 JPEG、pending 文件、EXIF、方向、MediaStore 行和 Motion Photo 尾部，并在各阶段杀进程  /  验证失… | UX-44 |
| C-0714 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L65 · 2. 已批准 P1 创意层用例 · 正文/条款 |  /  UX-45 热与长时间运行  /  连续取景 20 分钟、Live 10 分钟、三张连拍 20 组和风格导出 20 次，并模拟 Light/Moderate/Severe/Critical  … | UX-45 |
| C-0715 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L71 · 2.1 自然上镜 v1（ApprovedSeparate） · 正文/条款 |  /  UX-46 主动选择与模式边界  /  首次启动、旧设置、普通/自动/扩展模式、Live 开关、重启和重置  /  默认 OFF；菜单先说明本机处理、原片优先、另存及预览近似，再由用户选自然/… | UX-46 |
| C-0716 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L72 · 2.1 自然上镜 v1（ApprovedSeparate） · 正文/条款 |  /  UX-47 预览几何与降级  /  单人正脸、侧脸、多人、进出画面、快速转头、遮挡、四方向、两画幅、切镜头、热暂停和 GPU 失败  /  局部纹理变化只在保守几何蒙版内，眼鼻嘴和背景不被整图… | UX-47 |
| C-0717 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L73 · 2.1 自然上镜 v1（ApprovedSeparate） · 正文/条款 |  /  UX-48 非破坏成片与恢复  /  各档搭配原图/风格、默认保存/另存/自动双保存、拍摄后换设置、处理中断与重试、无有效单人  /  原片先发布且字节不被美颜覆盖；效果副本重新检测拍摄图；捕… | UX-48 |
| C-0718 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L74 · 2.1 自然上镜 v1（ApprovedSeparate） · 正文/条款 |  /  UX-49 质量、稳定性与热  /  受控人物样张 OFF/自然/柔和盲评；各档连续取景 20 分钟、导出 20 次、切换/后台恢复 20 次；温度分级  /  不改变脸型或生成内容；保留可辨… | UX-49 |
| C-0719 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L76 · 2.1 自然上镜 v1（ApprovedSeparate） · 正文/条款 | 执行细节与证据模板见自然上镜专项验收。模拟器基础渲染/状态测试与小米 14 Pro 专项分开登记。 | FR-38、N-40 |
| C-0720 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L80 · 2.2 本轮修订的验收证据要求（2026-09-05） · 正文/条款 | 上述修改是目标验收合同，UX-04/06/27/30/33/34 的新增分支未执行，保持 NotRun。历史测试结果仅证明原覆盖，不自动继承为修订后通过。UX-15 的分母和比例仍受 CP-01 控制… | N-44、N-46 |
| C-0721 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L82 · 2.2 本轮修订的验收证据要求（2026-09-05） · 正文/条款 | P-1 实验按需求总纲第 3.2、3.3.1 节执行：采样前冻结配置与待决计算规则；用含超时、主动退出、失败、缺评、失访和重复恢复的示例记录复算指标；研究侧核对会话关联、导出责任和保留/删除期限。冻结… | N-44、N-46 |
| C-0722 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L84 · 2.2 本轮修订的验收证据要求（2026-09-05） · 正文/条款 | 口令覆盖验收按意图 × 启用场景 × 听众及安全回退检查，每条稳定 ID 对应触发、退出、失效和未知输入样例；数量由覆盖推导，保留历史样张，不按数量配额删减行为。 | N-44、N-46 |
| C-0723 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L88 · 3. 完整 P0 验收缺口 · 正文/条款 | FR-15、FR-16、FR-17 目前只有需求级验收摘要和 P0 门禁，没有独立 UX 编号。为避免伪造追踪，本轮不新增或挪用 UX ID；P-1 全部门槛通过、完整 P0 开发获准前，必须补充专属… | FR-15、FR-16、FR-17 |
| C-0724 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L92 · 9.2 目标机真机矩阵 · 正文/条款 | - P-1、P0 和 P1 只验收小米 14 Pro；其他 Android 机型、折叠屏和平板不进入测试矩阵，也不作兼容承诺。 | N-45 |
| C-0725 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L93 · 9.2 目标机真机矩阵 · 正文/条款 | - 每次验收记录目标机的地区版本、Android 版本、HyperOS 版本、Build fingerprint 和 App 版本；未记录或未实测的系统版本不视为已覆盖。 | N-45 |
| C-0726 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L94 · 9.2 目标机真机矩阵 · 正文/条款 | - 在至少一台小米 14 Pro 上验证冷启动、连续取景 10 分钟、连续拍摄 100 张、前后台切换、权限撤回、相机被占和存储失败；有第二台同型号设备时复跑核心可靠性用例，但不扩展机型范围。 | N-45 |
| C-0727 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L95 · 9.2 目标机真机矩阵 · 正文/条款 | - 其他机型上的问题不阻断第一版；代码仍保留标准 CameraX 能力探测与安全回退，避免无必要地写死厂商私有接口。 | N-45 |
| C-0728 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L97 · 9.2 目标机真机矩阵 · 正文/条款 | 以下为完整专项的未完成状态。2026-08-31 基线轮次未连接目标机；2026-09-03 已有局部真机结果及失败，不能再概括为从未连接。历史局部证据不覆盖本表完整合同或2026-09-12新增分支… | N-45 |
| C-0729 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L101 · 9.2 目标机真机矩阵 · 正文/条款 |  /  后摄快捷焦段按钮与预览/成片视角、EXIF 标定  /  NotRun  /  基线轮次无设备；后续局部证据未覆盖本项完整专项  /  | N-45 |
| C-0730 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L102 · 9.2 目标机真机矩阵 · 正文/条款 |  /  CameraX 报告的连续变焦、EV 范围与各物理后摄切换  /  NotRun  /  无目标机 Camera2 能力报告  /  | N-45 |
| C-0731 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L103 · 9.2 目标机真机矩阵 · 正文/条款 |  /  人像/HDR/夜景扩展与实时 ImageAnalysis 三用例同绑  /  NotRun  /  Extensions 支持取决于目标机系统版本  /  | N-45 |
| C-0732 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L104 · 9.2 目标机真机矩阵 · 正文/条款 |  /  AE/AF 长按锁定、解除和重绑恢复  /  NotRun  /  需要真实 3A 状态与触控预览  /  | N-45 |
| C-0733 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L105 · 9.2 目标机真机矩阵 · 正文/条款 |  /  4:3/16:9 成片、3/10 秒倒计时、音量键与 100 张连续保存  /  NotRun  /  需要目标机相机、相册和硬件按键  /  | N-45 |
| C-0734 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L106 · 9.2 目标机真机矩阵 · 正文/条款 |  /  遮挡/脏污提示在暗景、纯色墙和真实污渍下的误报率  /  NotRun  /  需要目标机样张与阈值校准  /  | N-45 |
| C-0735 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L107 · 9.2 目标机真机矩阵 · 正文/条款 |  /  十二种风格和七项编辑的预览/导出颜色、广色域、屏幕色彩管理与 HyperOS 相册观感  /  NotRun  /  缺覆盖本项的目标机与受控色卡完整证据  /  | N-45 |
| C-0736 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L108 · 9.2 目标机真机矩阵 · 正文/条款 |  /  六类姿势的触发/退出、坐姿/走动时序、关节裁切、衣着/遮挡和多人安全回退  /  NotRun  /  需要目标机真实帧、不同体型/衣着、竖横屏和稳定时序样张  /  | N-45 |
| C-0737 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L109 · 9.2 目标机真机矩阵 · 正文/条款 |  /  构图、用光、焦段/距离、背景、夜景和运动摄影技巧的触发精度  /  NotRun  /  需要目标机视场角、已验收焦段、真实亮度与运动模糊样张  /  | N-45 |
| C-0738 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L110 · 9.2 目标机真机矩阵 · 正文/条款 |  /  场景化风格推荐、最近/收藏和人像保守强度  /  NotRun  /  需要受控色卡、不同光照/肤色、屏幕色彩管理和人工盲评  /  | N-45 |
| C-0739 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L111 · 9.2 目标机真机矩阵 · 正文/条款 |  /  固定三张连拍的间隔、成功率、热量与真实清晰度推荐有效性  /  NotRun  /  需要目标机相机吞吐、真实手抖和人物样张  /  | N-45 |
| C-0740 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L112 · 9.2 目标机真机矩阵 · 正文/条款 |  /  轻编辑副本在 HyperOS MediaStore/系统相册中的可见性和 EXIF 行为  /  NotRun  /  需要目标机系统相册验证  /  | N-45 |
| C-0741 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L113 · 9.2 目标机真机矩阵 · 正文/条款 |  /  pending/发布后资产校验、0/90/180/270 度方向、EXIF 与连续 100 张完整性  /  NotRun  /  需要目标机 MediaStore、相册、进程终止和真实捕获文… | N-45 |
| C-0742 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L114 · 9.2 目标机真机矩阵 · 正文/条款 |  /  分阶段保存、收藏/回收站确认、完整质量/省空间副本和异常恢复  /  NotRun  /  需要目标机存储压力、HyperOS 相册和进程终止验证  /  | N-45 |
| C-0743 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L115 · 9.2 目标机真机矩阵 · 正文/条款 |  /  四用例 Live 绑定、无声编码、约 3 秒裁剪、Motion Photo 播放与普通 JPEG 回退  /  NotRun  /  需要小米 14 Pro CameraX/编码器/Hyper… | N-45 |
| C-0744 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L116 · 9.2 目标机真机矩阵 · 正文/条款 |  /  Motion Photo 是否保留广色域或 Ultra HDR  /  NotRun  /  主 JPEG 字节保持不变，但目标机捕获格式、XMP 重写与 HyperOS 显示均未验收；不得宣… | N-45 |
| C-0745 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L117 · 9.2 目标机真机矩阵 · 正文/条款 |  /  连续取景 20 分钟、Live 10 分钟、连拍/导出负载、内存峰值、热状态与恢复  /  NotRun  /  需要小米 14 Pro 热状态、环境温度、电量和性能记录  /  | N-45 |
| C-0746 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L118 · 9.2 目标机真机矩阵 · 正文/条款 |  /  自然上镜的肤质、遮挡、动态蒙版、画幅/镜头、原片与另存、长时帧率/内存/发热  /  NotRun  /  模拟器合成图与短时三用例测试不能替代目标机真实人物、HyperOS 相册及性能证据 … | N-45 |
| C-0747 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L122 · 9.3 质量门槛 · 正文/条款 | 本轮自动化结果（2026-08-31）：coach:test Pass、androidApp:testDebugUnitTest Pass、ExplainApi .NET 3/3 Pass。该结果不改… | N-46 |
| C-0748 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L124 · 9.3 质量门槛 · 正文/条款 | - 本地自动化门禁依次运行 .\gradlew.bat :coach:test 与 .\gradlew.bat :androidApp:testDebugUnitTest；前者覆盖口令安全边界与多人回… | N-46 |
| C-0749 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L125 · 9.3 质量门槛 · 正文/条款 | - 第 3.3 节产品指标全部保留原始数据。 | N-46 |
| C-0750 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L126 · 9.3 质量门槛 · 正文/条款 | - 同机同景与系统相机比较，教练照片不能稳定出现明显更暗、更噪或更糊。 | N-46 |
| C-0751 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L127 · 9.3 质量门槛 · 正文/条款 | - 扩展模式无法与分析同时工作时，保留分析并回退标准拍照，界面不可黑屏。 | N-46 |
| C-0752 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L128 · 9.3 质量门槛 · 正文/条款 | - 五秒内出现方向相反提示、重复 TTS、两个动作同屏、快门被指导锁住，均为 P-1 阻断问题。 | N-46 |
| C-0753 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L129 · 9.3 质量门槛 · 正文/条款 | - 未完成第 3.2 节三组对照实验，不能以内部主观样片代替 Go 决策。 | N-46 |
| C-0754 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L130 · 9.3 质量门槛 · 正文/条款 | - P1 姿势验收样本中，可观察口令触发精度目标为 ≥90%，改善后的退出响应 p95 ≤1 秒；低置信度和多人样本不得出现单人姿势。样本不足或未记录设备身份时保持 NotRun。 | N-46 |
| C-0755 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L131 · 9.3 质量门槛 · 正文/条款 | - P1 风格推荐必须稳定、可解释且原图第一；真实颜色、人像保守强度和推荐偏好未经受控样张与人工盲评不得标为 Pass。 | N-46 |
| C-0756 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L132 · 9.3 质量门槛 · 正文/条款 | - P1 资产完整性矩阵要求普通单拍 100/100 可打开、方向正确、无零字节、无重复和无 pending 残留；该有限样本门禁不替代第 3.3 节长期保存可靠性指标。 | N-46 |
| C-0757 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L133 · 9.3 质量门槛 · 正文/条款 | - 热降级须在进入 Severe 后 10 秒内降低非必要负载；不得把系统未报告热状态或单次短测当成长期热量通过。 | N-46 |
| C-0758 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L139 · 4. 2026-09-12 逐项审核后的验收增量 · 正文/条款 | 本轮逐一审核UX-01～UX-49；矩阵见逐项审核。新增分支均NotRun，历史记录不继承为新合同通过。UX-15仍ConflictPending，FR-15～17仍缺专属P0 UX，不挪用现有编号。 | N-44、N-46 |
| C-0759 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L141 · 4. 2026-09-12 逐项审核后的验收增量 · 正文/条款 | 通用证据包须记录设备/系统/构建、前置条件、所测分支、操作与预期、实际结果、证据路径及日期。权限用例补安装包备份/迁移排除检查；语音用例补无离线音色和迟到回调；保存用例补持久源/缓存清理与各阶段中断；… | N-44、N-46 |
| C-0760 | [acceptance-plan.md](../acceptance/acceptance-plan.md) L143 · 4. 2026-09-12 逐项审核后的验收增量 · 正文/条款 | 场景、短口令、姿势、技巧和十二风格按逐项矩阵中的每一行采样，不以一类中任选一个正例代表整个类别；支持条件、反例、Unknown/失效都要覆盖。数值/样本量未冻结的专项不能作发布通过结论。 | N-44、N-46 |

## docs/traceability/requirements-matrix.md

| 单元 | 原文位置 | 摘录（非权威副本） | 逐项审核ID |
| --- | --- | --- | --- |
| C-0761 | [requirements-matrix.md](../traceability/requirements-matrix.md) L3 · 功能需求定义与追踪矩阵 · 正文/条款 | 2026-09-08 曝光与技巧局部修复见报告；自动化不替代目标机成片、技巧或美颜验收。 | N-44、N-45 |
| C-0762 | [requirements-matrix.md](../traceability/requirements-matrix.md) L5 · 功能需求定义与追踪矩阵 · 正文/条款 | 2026-09-05 点脸/调亮/美颜反馈专项：验证记录。仅局部自动化与安装证据，未改变下表完整目标机/产品验收状态。 | N-44、N-45 |
| C-0763 | [requirements-matrix.md](../traceability/requirements-matrix.md) L7 · 功能需求定义与追踪矩阵 · 正文/条款 | - 状态：当前规范附件；FR 权威定义唯一位置 | N-44、N-45 |
| C-0764 | [requirements-matrix.md](../traceability/requirements-matrix.md) L8 · 功能需求定义与追踪矩阵 · 正文/条款 | - 权威入口：产品需求总纲 | N-44、N-45 |
| C-0765 | [requirements-matrix.md](../traceability/requirements-matrix.md) L9 · 功能需求定义与追踪矩阵 · 正文/条款 | - UX 权威定义：分阶段验收规范 | N-44、N-45 |
| C-0766 | [requirements-matrix.md](../traceability/requirements-matrix.md) L10 · 功能需求定义与追踪矩阵 · 正文/条款 | - 自动化结果登记：2026-09-01 coach JVM Pass、Android JVM Pass；2026-08-31 ExplainApi .NET 3/3 Pass；详见验证报告 | N-44、N-45 |
| C-0767 | [requirements-matrix.md](../traceability/requirements-matrix.md) L11 · 功能需求定义与追踪矩阵 · 正文/条款 | - 真机结论：没有小米 14 Pro 证据的项目一律保持 NotRun | N-44、N-45 |
| C-0768 | [requirements-matrix.md](../traceability/requirements-matrix.md) L13 · 功能需求定义与追踪矩阵 · 正文/条款 | 2026-09-03 联合真机补充：见Ready、Live/保存与自然上镜回归。目标机核心仪器 14/14 通过，但美颜相机重绑失败、UI 集未完成，实际快门操作被系统输入权限拒绝。下表的完整 Dev… | N-44、N-45 |
| C-0769 | [requirements-matrix.md](../traceability/requirements-matrix.md) L17 · 2026-09-05 需求内容修订 · 正文/条款 | OBJ-GUIDANCE 对应 FR-07/08、UX-04/06/31；OBJ-EXPERIMENT 对应 FR-14 及需求总纲 3.2/3.3.1；OBJ-CAPTURE 对应 FR-25/28… | N-44、N-45 |
| C-0770 | [requirements-matrix.md](../traceability/requirements-matrix.md) L19 · 2026-09-05 需求内容修订 · 正文/条款 | 本轮仅修订文档；所有上述合同新增部分的实现符合性 Unknown、JVM/.NET/Instrumented/Device/Acceptance 新增验证 NotRun。表内旧日期 Pass 保留为历… | N-44、N-45 |
| C-0771 | [requirements-matrix.md](../traceability/requirements-matrix.md) L23 · 状态词汇 · 正文/条款 | 2026-09-03 后续修复见 Ready、Live 与美颜入口修复记录：补充 Ready 实时原因、美颜可见入口/实际状态和录制会话隔离；JVM 198/198、新 APK 构建/lint 通过。… | N-44、N-45 |
| C-0772 | [requirements-matrix.md](../traceability/requirements-matrix.md) L27 · 状态词汇 · 正文/条款 |  /  Scope  /  InScope / GateLocked / ApprovedSeparate / Deferred  /  分别表示当前 P-1、需 Go 后的 P0、单独批准但不进入 … | N-44、N-45 |
| C-0773 | [requirements-matrix.md](../traceability/requirements-matrix.md) L28 · 状态词汇 · 正文/条款 |  /  Delivery  /  路径存在；完整性 Unknown / Unknown / 可能有占位  /  只描述仓库可见事实；本轮未做业务代码完整性审计，不把“有文件”写成“已交付”  /  | N-44、N-45 |
| C-0774 | [requirements-matrix.md](../traceability/requirements-matrix.md) L29 · 状态词汇 · 正文/条款 |  /  Verification  /  JVM/.NET Pass 或待门禁；Instrumented/Device/数据审计 NotRun；Acceptance gap  /  自动化、仪器、真机… | N-44、N-45 |
| C-0775 | [requirements-matrix.md](../traceability/requirements-matrix.md) L35 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-01  /  P-1  /  系统权限前展示端侧分析说明  /  不同意不申请权限；同意后才进系统弹窗  /  UX-01  /  CameraConsentScreen.kt、Main… | FR-01 |
| C-0776 | [requirements-matrix.md](../traceability/requirements-matrix.md) L36 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-02  /  P-1  /  只申请相机权限  /  飞行模式可拍；不申请麦克风、定位、读整本相册；App 私有研究/暂存/配方/恢复文件排除云备份与设备迁移，公开相册由用户管理  / … | FR-02 |
| C-0777 | [requirements-matrix.md](../traceability/requirements-matrix.md) L37 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-03  /  P-1  /  后摄预览、分析和捕获同时工作  /  无黑屏；分析不能阻塞预览和快门  /  UX-09、UX-12、UX-21、UX-23、UX-32、UX-36  / … | FR-03 |
| C-0778 | [requirements-matrix.md](../traceability/requirements-matrix.md) L38 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-04  /  P-1  /  两个意图轻量切换  /  用户手选后本次会话不被覆盖；一张照片只重置预算，相机会话内保持手选意图；退出/进程结束或主动重置才结束对应锁定  /  UX-03… | FR-04 |
| C-0779 | [requirements-matrix.md](../traceability/requirements-matrix.md) L39 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-05  /  P-1  /  端侧获得人脸、单人姿势、水平、亮度和脸占比信号  /  脸部明暗必须来自脸框内外真实亮度采样；未知不猜；不识别身份、不保存人脸模板；Face漏检与实际遮脸分… | FR-05 |
| C-0780 | [requirements-matrix.md](../traceability/requirements-matrix.md) L40 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-06  /  P-1  /  每轮最多生成三条候选  /  构图、光/曝光、姿势各最多一条  /  UX-04、UX-17  /  CoachEngine.kt、CueSelector.… | FR-06 |
| C-0781 | [requirements-matrix.md](../traceability/requirements-matrix.md) L41 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-07  /  P-1  /  默认最多两个必做动作，结束统一判定画面状态  /  完成/跳过/超时/通道关闭/无候选/附加结束均执行统一判定；人物丢失恢复，多人安全回退，必要信号 Unk… | FR-07 |
| C-0782 | [requirements-matrix.md](../traceability/requirements-matrix.md) L42 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-08  /  P-1  /  提示带 shooter / subject 听众，并提供中文语音和被拍者字幕独立开关  /  当前 shooter / subject 口令均可外放；四种提… | FR-08 |
| C-0783 | [requirements-matrix.md](../traceability/requirements-matrix.md) L43 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-09  /  P-1  /  提供第 6.1 节相机控件  /  每条可执行口令在取景器都有对应操作；AF/AE分别确认；画质/速度优先只表示捕获质量与延迟取舍  /  UX-08、UX… | FR-09 |
| C-0784 | [requirements-matrix.md](../traceability/requirements-matrix.md) L44 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-10  /  P-1  /  场景初始参数可自动应用一次  /  改动可见；用户手改后锁定；不得抖动；EV仅用户主动应用；对照实验关闭实时自动初值  /  UX-08、UX-17、UX-… | FR-10 |
| C-0785 | [requirements-matrix.md](../traceability/requirements-matrix.md) L45 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-11  /  P-1  /  手动快门始终优先  /  指导未完成也可拍；自动拍不存在；保存失败不得无限冒充捕获中；结束失败批次后按实际相机/空间条件允许新拍  /  UX-05、UX-… | FR-11 |
| C-0786 | [requirements-matrix.md](../traceability/requirements-matrix.md) L46 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-12  /  P-1  /  写入系统相册可见目录，无水印  /  成功可见；失败可重试；不读整本相册；可重试源持久私有暂存且有界；无源明确不可恢复，保存重试不再次拍照  /  UX-0… | FR-12 |
| C-0787 | [requirements-matrix.md](../traceability/requirements-matrix.md) L47 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-13  /  P-1  /  主路径离线  /  飞行模式完成意图、指导、快门、保存；包括新安装首次断网；语音不可用降级与视觉主路径分开验收  /  UX-11  /  androidA… | FR-13 |
| C-0788 | [requirements-matrix.md](../traceability/requirements-matrix.md) L48 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-14  /  P-1  /  最小研究事件与研究侧记录共同支持可复算指标  /  按需求总纲 3.3.1/10.2 明确起止、超时、退出、保存及匿名关联；不记录帧、坐标、身份；待决公式冻… | FR-14 |
| C-0789 | [requirements-matrix.md](../traceability/requirements-matrix.md) L49 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-15  /  P0  /  前摄、只拍景和更多 P0 场景  /  按第 4.3、11.2 节验收  /  无专属 UX：完整 P0 尚未获准，须在 Go 后补 P0 用例  /  sc… | FR-15 |
| C-0790 | [requirements-matrix.md](../traceability/requirements-matrix.md) L50 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-16  /  P0  /  代拍简化界面  /  未经解释可用大快门或音量键拍三张  /  无专属 UX：完整 P0 尚未获准，须补代拍验收  /  androidApp 目标模块；未找… | FR-16 |
| C-0791 | [requirements-matrix.md](../traceability/requirements-matrix.md) L51 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-17  /  P0  /  可选“再讲细”  /  单独同意、单帧上传、失败不影响主路径  /  无专属 UX：完整 P0 尚未获准，须补讲解客户端验收  /  ExplainApi/、… | FR-17 |
| C-0792 | [requirements-matrix.md](../traceability/requirements-matrix.md) L52 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-18  /  P-1  /  运行时发现后摄焦段、缩放、曝光和扩展模式能力  /  不硬编码 2x、传感器 ID 或厂商私有接口；失效时回到默认后摄/普通 Photo  /  UX-08… | FR-18 |
| C-0793 | [requirements-matrix.md](../traceability/requirements-matrix.md) L53 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-19  /  P-1  /  AE/AF 锁定、网格、水平仪  /  锁定状态可见可解除；叠线可独立关闭并持久化；测光区域维持不等于AE锁；AF/AE支持/请求/确认/失败分开，真实回执… | FR-19 |
| C-0794 | [requirements-matrix.md](../traceability/requirements-matrix.md) L54 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-20  /  P-1  /  倒计时、画幅和画质优先/速度优先  /  关闭/3/10 秒、4:3/16:9 与两种拍摄偏好全部接入真实 CameraX 行为；画质优先不承诺合焦快门门禁… | FR-20 |
| C-0795 | [requirements-matrix.md](../traceability/requirements-matrix.md) L55 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-21  /  P-1  /  相机设置持久化和一键恢复默认  /  非法或旧设置安全回退；重置不清除同意或照片；重绑后状态可恢复；拍摄轮次、相机会话和持久设置分别恢复，不因新照片暗改手选… | FR-21 |
| C-0796 | [requirements-matrix.md](../traceability/requirements-matrix.md) L56 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-22  /  P-1  /  保守提示镜头可能遮挡或脏污  /  连续多帧才触发；无脸时仍可出现；恢复后自动清除，不把暗景直接判成脏污  /  UX-25  /  LensObstruc… | FR-22 |
| C-0797 | [requirements-matrix.md](../traceability/requirements-matrix.md) L57 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-23  /  P1 创意层  /  通过用户主动打开的独立参数面板提供当前能力可执行的建议  /  最多三项；面板与指导视觉/TTS 互斥，分析与快门继续；关闭按最新信号恢复、不重置预算… | FR-23 |
| C-0798 | [requirements-matrix.md](../traceability/requirements-matrix.md) L58 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-24  /  P1 创意层  /  提供第 12.2 节批准的 12 种稳定风格  /  随时切回原图；预览、编辑和导出共享确定性矩阵；原片图像字节不被覆盖  /  UX-26、UX-2… | FR-24 |
| C-0799 | [requirements-matrix.md](../traceability/requirements-matrix.md) L59 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-25  /  P1 创意层  /  用户明确开启三张连拍，区分捕获继续与保存恢复  /  三张原片保留且选优可解释；失败暂停，保存重试不触发拍摄；仅显式继续捕获未完成序号；结束/重启不自… | FR-25 |
| C-0800 | [requirements-matrix.md](../traceability/requirements-matrix.md) L60 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-26  /  P1 创意层  /  拍后调整曝光、对比度、饱和度、色温、色调、褪色和风格强度  /  支持对比原图、撤销、重做和一键重置；只允许“另存副本”，不得覆盖原片  /  UX-… | FR-26 |
| C-0801 | [requirements-matrix.md](../traceability/requirements-matrix.md) L61 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-27  /  P1 创意层  /  图像处理有内存上限、失败回退和可见保存错误  /  解码或调色失败回到未调色原片；MediaStore 错误保留重试或明确失败反馈；可恢复源不得仅依赖… | FR-27 |
| C-0802 | [requirements-matrix.md](../traceability/requirements-matrix.md) L62 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-28  /  P1 创意层  /  批次 batchId、单张 captureId 与派生 derivativeId 明确关联  /  按单张、资产与阶段幂等恢复；新捕获新 ID，保存重… | FR-28 |
| C-0803 | [requirements-matrix.md](../traceability/requirements-matrix.md) L63 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-29  /  P1 创意层  /  默认非破坏保存，并提供可选“原片+效果自动保存”  /  默认只发布原片并在 App 私有目录保存编辑配方；效果 JPEG 只在明确另存时生成；自动双… | FR-29 |
| C-0804 | [requirements-matrix.md](../traceability/requirements-matrix.md) L64 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-30  /  P1 创意层  /  兼容 SDR 副本和本 App URI 拍后操作；非目标设备仅非阻断健壮性检查  /  打开、分享、收藏/回收站服从平台能力，不读整本相册；非目标机失… | FR-30 |
| C-0805 | [requirements-matrix.md](../traceability/requirements-matrix.md) L65 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-31  /  P1 创意层  /  显式无声 Live 开关，生成 Android Motion Photo Format 1.0 单文件 JPEG  /  先做 CameraX 会话能… | FR-31 |
| C-0806 | [requirements-matrix.md](../traceability/requirements-matrix.md) L66 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-32  /  P1 第二阶段  /  提供特写、半身、全身、坐姿、走动和单人互动六类姿势库  /  每条口令定义可观察进入/退出、完成方式、超时、冷却和低置信度回退；一次一个主要动作、最… | FR-32 |
| C-0807 | [requirements-matrix.md](../traceability/requirements-matrix.md) L67 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-33  /  P1 第二阶段  /  提供构图、关节裁切、用光、焦段/距离、背景、夜景、运动和多人安全摄影技巧  /  只使用当前公开能力或画面可观察证据；不硬编码 2x、不伪造精确机位… | FR-33 |
| C-0808 | [requirements-matrix.md](../traceability/requirements-matrix.md) L68 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-34  /  P1 第二阶段  /  在十二种风格上提供场景化推荐、建议强度、最近使用和收藏  /  原图始终第一；最多三个推荐且给出一条原因；不自动套用或生成副本；最近最多五个，偏好不… | FR-34 |
| C-0809 | [requirements-matrix.md](../traceability/requirements-matrix.md) L69 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-35  /  P1 第二阶段  /  面部只输出中性可观察动作，并为人像风格提供保守强度而非未经验证的局部肤色保护  /  不推断情绪、身份、年龄、性别或外貌；可靠单脸可检查变换前后脸部… | FR-35 |
| C-0810 | [requirements-matrix.md](../traceability/requirements-matrix.md) L70 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-36  /  P1 第二阶段  /  MediaStore 保存增加 pending 和发布后资产完整性校验  /  验证非零长度、JPEG/EXIF/方向、尺寸、MIME、路径和最终 … | FR-36 |
| C-0811 | [requirements-matrix.md](../traceability/requirements-matrix.md) L71 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-37  /  P1 第二阶段  /  连续取景、姿势、风格、连拍和 Live 按目标机热状态分级降载  /  Light/Moderate 降低非必要分析与预览负载；Severe 停止新… | FR-37 |
| C-0812 | [requirements-matrix.md](../traceability/requirements-matrix.md) L72 · Requirement → Phase → Acceptance → Component → Evidence → Status · 正文/条款 |  /  FR-38  /  P1 自然上镜  /  提供独立默认 OFF 的 NATURAL/SOFT 局部纹理平滑，限当前后摄单人普通 Photo；不改变面部结构或旧风格语义  /  主动开启、设置… | FR-38 |
| C-0813 | [requirements-matrix.md](../traceability/requirements-matrix.md) L76 · 无 UX 映射说明 · 正文/条款 | - FR-14 是研究事件的数据最小化契约，不是可见交互。强行挂到某个拍照 UX 会掩盖安装包、事件导出和隐私审计，因此明确保留“无合理 UX 映射”。 | N-44、N-45 |
| C-0814 | [requirements-matrix.md](../traceability/requirements-matrix.md) L77 · 无 UX 映射说明 · 正文/条款 | - FR-15～FR-17 属于完整 P0；P-1 未过 Go 门槛，当前 49 个 UX 中没有专属 P0 用例。本轮不伪造编号，后续获准开发完整 P0 前补齐。 | N-44、N-45 |
| C-0815 | [requirements-matrix.md](../traceability/requirements-matrix.md) L78 · 无 UX 映射说明 · 正文/条款 | - 除上述四项外，每个 FR 至少映射一个现有 UX。映射表示验收关系，不代表该 UX 已通过。 | N-44、N-45 |
| C-0816 | [requirements-matrix.md](../traceability/requirements-matrix.md) L82 · 设备证据总状态 · 正文/条款 | 本次文档审核未执行新的真机测试。2026-09-03 已有带设备/构建身份的局部真机记录及失败（见页首链接）；当前完整专项和新增合同仍缺完整证据。快捷焦段、EXIF、Zoom/EV、Extension… | N-44、N-45 |
| C-0817 | [requirements-matrix.md](../traceability/requirements-matrix.md) L86 · 2026-09-12 逐项需求优化 · 正文/条款 | 按用户要求先建立逐项审核再优化文档，覆盖每个FR/UX及未编号条目：逐项矩阵。本轮变更见修订记录。未改变Scope，不改业务实现；受影响行新增合同实现符合性Unknown，JVM/.NET/Instr… | N-44、N-45 |
