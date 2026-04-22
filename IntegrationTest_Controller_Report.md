# Controller 层集成测试汇报

## 1. 汇报信息

| 项目 | 内容 |
|---|---|
| 项目名称 | SoftwareTestingDemo |
| 测试类型 | 集成测试（Controller 层功能模块） |
| 负责成员 | 成员E |
| 汇报日期 | 2026-04-21 |
| 测试框架 | Spring Boot Test + MockMvc + JUnit 5 + Mockito |
| 测试脚本 | src/test/java/com/demo/integration/IntegrationFlowTest.java |

## 2. 测试目标

验证 Controller 层核心业务流程在接口入口层的行为正确性，重点检查：
- 正常流程是否返回预期结果
- 登录态相关逻辑是否生效
- 请求参数进入 Controller 后是否触发正确业务调用
- 页面跳转与 JSON 响应是否符合预期

## 3. 覆盖范围

| 模块 | 关键接口/功能 | 用例编号 |
|---|---|---|
| 登录流程 | `/loginCheck.do` | IT-LG-001, IT-LG-002 |
| 订单流程 | `/order_manage`, `/addOrder.do`, `/delOrder.do` | IT-OR-001, IT-OR-002, IT-OR-003 |
| 场馆流程 | `/venuelist/getVenueList`, `/venue` | IT-VN-001, IT-VN-002 |
| 留言流程 | `/sendMessage`, `/message/findUserList`, `/delMessage.do` | IT-MG-001, IT-MG-002, IT-MG-003, IT-MG-004, IT-MG-005 |

## 3.1 模块业务功能与黑盒等价类划分

本节用于对照当前已实现集成测试（通过/失败）与新增黑盒占位测试（`@Disabled`，跳过），说明各模块业务目标与等价类覆盖情况。

### 登录模块（UserController）

**业务功能（概述）**

- 接收用户账号密码登录请求。
- 登录成功返回 `/index` 并写入 Session。
- 登录失败返回 `false`。

**黑盒等价类划分**

- 有效等价类：`userID` 存在且 `password` 正确。
- 无效等价类：`userID` 存在但 `password` 错误。
- 边界/缺口等价类（本轮集成未覆盖）：`userID/password` 为 `null` 或空字符串（当前主要在服务层黑盒缺口中追踪）。

### 订单模块（OrderController + AdminOrderController）

**业务功能（概述）**

- 用户侧：订单管理页访问、创建订单、删除订单。
- 管理侧：审核订单通过/驳回。

**黑盒等价类划分**

- 会话身份等价类：
	- 有效等价类：已登录用户访问受保护功能。
	- 无效等价类：未登录访问/操作应被拒绝。
- 订单参数等价类：
	- 有效等价类：`hours > 0` 且时间格式合法。
	- 无效等价类：`hours <= 0`（新增占位 IT-BB-01，待实现）。
- 订单状态机等价类：
	- 有效等价类：`STATE_NO_AUDIT -> confirm/reject`。
	- 无效等价类：在 `STATE_WAIT/STATE_FINISH/STATE_REJECT` 下继续 `confirm`，或在非法状态下 `reject`（新增占位 IT-BB-02, IT-BB-03，待实现）。

### 场馆模块（VenueController）

**业务功能（概述）**

- 查询场馆分页列表。
- 进入场馆详情页并渲染场馆信息。

**黑盒等价类划分**

- 列表分页等价类：
	- 有效等价类：合法页码（如 `page=1`）返回结构化 JSON。
	- 无效等价类（当前未覆盖）：页码越界/非法值。
- 详情查询等价类：
	- 有效等价类：存在的 `venueID`。
	- 无效等价类（当前未覆盖）：不存在或非法 `venueID`。

### 留言模块（MessageController + AdminMessageController）

**业务功能（概述）**

- 用户侧：提交留言、查询本人留言、删除留言。
- 管理侧：留言审核通过/驳回。

**黑盒等价类划分**

- 会话与越权等价类：
	- 有效等价类：登录用户正常查询本人留言。
	- 无效等价类：未登录提交/删除留言、伪造他人 `userID` 提交留言（已发现缺陷 BUG-003~BUG-005）。
- 留言输入等价类：
	- 有效等价类：`content` 非空。
	- 无效等价类：`content` 为空（新增占位 IT-BB-05，待实现）。
- 留言状态机等价类：
	- 有效等价类：`STATE_NO_AUDIT -> confirm/reject`。
	- 无效等价类：已处理状态（`STATE_PASS/STATE_REJECT`）再次流转（新增占位 IT-BB-04，待实现）。

### 新闻模块（AdminNewsController）

**业务功能（概述）**

- 管理端新增新闻并重定向到新闻管理页。

**黑盒等价类划分**

- 新闻输入等价类：
	- 有效等价类：`title` 非空且 `content` 非空。
	- 无效等价类：`title` 为空或 `content` 为空（新增占位 IT-BB-06，待实现）。

### 等价类覆盖结论（集成测试视角）

- 已落地并执行：登录正确/错误、会话鉴权、核心页面渲染、越权漏洞探测。
- 已识别但待实现（`@Disabled` 占位）：订单参数校验、订单状态机非法流转、留言状态机非法流转、留言空内容、新闻空标题/空内容。
- CI 语义保持：通过=行为符合预期；失败=已复现缺陷；跳过=已知需求缺口待实现。

## 4. 用例与结果统计

| 测试场景 | 总用例 | 通过 | 失败 | 阻塞 |
|---|---:|---:|---:|---:|
| 登录流程 | 2 | 2 | 0 | 0 |
| 订单流程 | 3 | 2 | 1 | 0 |
| 场馆流程 | 2 | 2 | 0 | 0 |
| 留言流程 | 5 | 2 | 3 | 0 |
| 合计 | 12 | 8 | 4 | 0 |

## 5. 关键验证点

| 验证点 | 结果 |
|---|---|
| 合法用户登录后写入 Session 并返回 `/index` | 通过 |
| 错误密码登录返回 `false` | 通过 |
| 未登录访问订单管理触发登录异常逻辑 | 通过 |
| 登录后提交订单触发业务提交并重定向 | 通过 |
| 未登录删除订单应被拒绝 | 失败：实际可删除（IT-OR-003，BUG-002） |
| 场馆分页接口返回结构化 JSON 列表 | 通过 |
| 场馆详情页能够渲染目标场馆信息 | 通过 |
| 留言提交后重定向到留言页 | 通过 |
| 未登录查询个人留言触发登录异常逻辑 | 通过 |
| 未登录提交留言应被拒绝 | 失败：实际可提交并入库（IT-MG-003，BUG-003） |
| 已登录用户伪造他人 userID 提交留言应被拒绝 | 失败：实际按请求参数入库（IT-MG-004，BUG-004） |
| 未登录删除留言应被拒绝 | 失败：实际可删除（IT-MG-005，BUG-005） |

## 6. 缺陷发现与风险评估

| 缺陷编号 | 关联用例 | 问题描述 | 严重程度 | 状态 |
|---|---|---|---|---|
| BUG-002 | IT-OR-003 | 未登录可调用 `/delOrder.do` 删除订单，缺少登录与归属校验 | 高 | 新建 |
| BUG-003 | IT-MG-003 | 未登录可调用 `/sendMessage` 提交留言并入库 | 高 | 新建 |
| BUG-004 | IT-MG-004 | 已登录用户可伪造他人 userID 提交留言 | 高 | 新建 |
| BUG-005 | IT-MG-005 | 未登录可调用 `/delMessage.do` 删除留言 | 高 | 新建 |

风险结论：当前 Controller 层在部分写操作接口上存在鉴权缺失与身份校验缺失问题，属于高风险越权漏洞，应优先修复。

## 7. 执行说明

已在本地通过 Maven 执行该测试类并验证通过，示例命令：

```powershell
$env:MAVEN_HOME=Join-Path $env:LOCALAPPDATA 'Apache\maven\apache-maven-3.9.14'
$env:Path="$env:MAVEN_HOME\bin;$env:Path"
Set-Location 'd:\desktop\testpj\SoftwareTestingDemo'
mvn -Dtest=IntegrationFlowTest test
```

本轮执行结果：12 项测试方法执行通过（含漏洞探测型测试）；其中 4 项为漏洞复现用例，按“安全期望应拒绝”口径计为失败，关键漏洞均可稳定复现。

## 8. 结论

Controller 层集成测试已完成并扩展了安全性验证场景。当前覆盖登录、订单、场馆、留言四个核心模块，共 12 条业务用例：8 条通过、4 条失败。失败用例对应 4 条高风险缺陷（BUG-002~BUG-005），均与接口鉴权缺失或身份校验缺失有关，建议纳入最高优先级修复。

## 9. 相关文档

- docs/test-cases/IT-Integration/TC_IT.md
- docs/test-summary/TestSummaryReport.md（第3章）
- docs/defects/DefectLog.md
