# 测试用例设计文档 — 集成测试

## 文档信息

| 字段      | 值 |
|----------|----|
| 测试类型  | 集成测试 |
| 负责成员  | 成员E |
| 用例编号段 | `IT-LG/OR/VN/MG-001 ~ 099` |
| 创建日期  | 2026-04-08 |
| 最后更新  | 2026-04-21 |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-04-08 | 成员E | 初稿创建并完成首轮执行 |
| v1.1 | 2026-04-13 | 成员E | 补充漏洞探测型集成测试用例（待执行） |
| v1.2 | 2026-04-13 | 成员E | 执行漏洞探测型集成测试并记录缺陷 |
| v1.3 | 2026-04-21 | 成员E | 基于黑盒自查手册补充 @Disabled 占位用例（需求缺口追踪） |

---

## 登录流程

**测试对象**：`src.main.java.com.demo.controller.user.UserController.java:login(String userID, String password, HttpServletRequest)`
**测试函数**：`src.test.java.com.demo.integration.IntegrationFlowTest:testLoginSuccessShouldWriteUserSession()、testLoginWrongPasswordShouldReturnFalse()`
**设计技术**：`场景法 + 等价类划分`

| 功能点列表 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-----------|---------|---------|---------|---------|------|
| 普通用户登录 | IT-LG-001 | 合法用户提交正确用户名与密码 | 响应体为 `/index`，Session 写入 `user` | 响应体为 `/index`，Session 成功写入 `user` | 正确 |
| 密码错误 | IT-LG-002 | 用户名存在但密码错误 | 响应体为 `false` | 响应体为 `false` | 正确 |

---

## 订单流程

**测试对象**：`src.main.java.com.demo.controller.user.OrderController.java:order_manage(Model, HttpServletRequest), addOrder(String, String, String, int, HttpServletRequest, HttpServletResponse)`
**测试函数**：`src.test.java.com.demo.integration.IntegrationFlowTest:testOrderManageWithoutLoginShouldThrowLoginException()、testAddOrderWithLoginShouldRedirectAndInvokeSubmit()、testDeleteOrderWithoutLoginShouldStillInvokeDelete_Vulnerability()`
**设计技术**：`场景法`

| 功能点列表 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-----------|---------|---------|---------|---------|------|
| 未登录访问订单管理 | IT-OR-001 | 未登录直接访问 `/order_manage` | 抛出 `LoginException`，请求被拒绝 | 返回 500，抛出 `LoginException` | 正确 |
| 登录后提交订单 | IT-OR-002 | 已登录用户提交合法订单参数 | 跳转到 `order_manage`，并调用提交逻辑 | 响应重定向到 `order_manage`，`orderService.submit` 被调用 1 次 | 正确 |
| 未登录删除订单 | IT-OR-003 | 未登录直接调用 `/delOrder.do` 删除订单 | 请求应被拒绝（登录校验或权限校验失败） | 返回 `true`，`orderService.delOrder(1)` 被调用 | 错误 |

---

## 场馆流程

**测试对象**：`src.main.java.com.demo.controller.user.VenueController.java:venue_list(int), toGymPage(Model, int)`
**测试函数**：`src.test.java.com.demo.integration.IntegrationFlowTest:testVenueListShouldReturnPagedData()、testVenueDetailShouldRenderVenuePage()`
**设计技术**：`场景法`

| 功能点列表 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-----------|---------|---------|---------|---------|------|
| 分页获取场馆列表 | IT-VN-001 | 请求第一页场馆数据 | 返回分页 JSON，`content` 非空且包含场馆名 | 返回分页 JSON，`content[0].venueName=羽毛球馆A` | 正确 |
| 场馆详情页渲染 | IT-VN-002 | 通过 `venueID` 访问场馆详情页 | 返回详情页面并包含对应场馆数据 | 返回 200，页面包含 `羽毛球馆A` | 正确 |

---

## 留言流程

**测试对象**：`src.main.java.com.demo.controller.user.MessageController.java:sendMessage(String, String, HttpServletResponse), user_message_list(int, HttpServletRequest)`
**测试函数**：`src.test.java.com.demo.integration.IntegrationFlowTest:testSendMessageShouldRedirectAndPersistDefaultState()、testFindUserMessageWithoutLoginShouldThrowLoginException()、testSendMessageWithoutLoginShouldStillPersist_Vulnerability()、testSendMessageWithForgedUserIdShouldPersistForgedIdentity_Vulnerability()、testDeleteMessageWithoutLoginShouldStillInvokeDelete_Vulnerability()`
**设计技术**：`场景法`

| 功能点列表 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-----------|---------|---------|---------|---------|------|
| 提交留言 | IT-MG-001 | 用户提交留言内容 | 重定向到 `/message_list`，留言默认状态为未审核 | 响应重定向到 `/message_list`，`message.state=1` | 正确 |
| 未登录查询个人留言 | IT-MG-002 | 未登录访问 `/message/findUserList` | 抛出 `LoginException`，请求被拒绝 | 返回 500，抛出 `LoginException` | 正确 |
| 未登录提交留言 | IT-MG-003 | 未登录直接调用 `/sendMessage` 提交留言 | 请求应被拒绝（登录校验失败） | 响应重定向 `/message_list`，留言成功入库（`userID=u9999`） | 错误 |
| 伪造用户提交留言 | IT-MG-004 | 登录用户A在请求参数中提交用户B的 `userID` | 应使用会话用户身份，禁止伪造他人身份提交 | 留言按请求参数保存为 `userID=u2002`，未使用会话用户 | 错误 |
| 未登录删除留言 | IT-MG-005 | 未登录直接调用 `/delMessage.do` 删除留言 | 请求应被拒绝（登录校验或权限校验失败） | 返回 `true`，`messageService.delById(11)` 被调用 | 错误 |

---

## 黑盒自查占位（@Disabled）

> 说明：以下用例为“需求缺口占位”，已写入 `IntegrationFlowTest` 并使用 `@Disabled` 标记。当前 CI 语义为“跳过=已知缺口待实现”，不污染红绿信号。

**测试函数**：`src.test.java.com.demo.integration.IntegrationFlowTest:testAddOrder_NonPositiveHours_ShouldBeRejected()、testPassOrder_IllegalState_ShouldBeRejected()、testRejectOrder_IllegalState_ShouldBeRejected()、testPassOrRejectMessage_IllegalState_ShouldBeRejected()、testSendMessage_EmptyContent_ShouldBeRejected()、testAddNews_EmptyTitleOrContent_ShouldBeRejected()`
**设计技术**：`黑盒等价类划分（需求缺口占位）`

| 功能点列表 | 用例编号 | 用例描述 | 预期结果 | 当前状态 | 结论 |
|-----------|---------|---------|---------|---------|------|
| 订单提交参数校验 | IT-BB-01 | `/addOrder.do` 传入 `hours=0` | 抛出业务异常，且不应调用 `orderService.submit` | 跳过（@Disabled） | 待测 |
| 订单状态机非法流转 | IT-BB-02 | `/passOrder.do` 对非法前置状态订单执行通过 | 抛出业务异常，且不应调用 `orderService.confirmOrder` | 跳过（@Disabled） | 待测 |
| 订单状态机非法流转 | IT-BB-03 | `/rejectOrder.do` 对非法前置状态订单执行驳回 | 抛出业务异常，且不应调用 `orderService.rejectOrder` | 跳过（@Disabled） | 待测 |
| 留言状态机非法流转 | IT-BB-04 | `/passMessage.do` / `/rejectMessage.do` 对已处理留言再次流转 | 抛出业务异常，且不应调用对应 service 方法 | 跳过（@Disabled） | 待测 |
| 留言输入校验 | IT-BB-05 | `/sendMessage` 传入空 `content` | 抛出业务异常，且不应调用 `messageService.create` | 跳过（@Disabled） | 待测 |
| 新闻输入校验 | IT-BB-06 | `/addNews.do` 传入空 `title` 或空 `content` | 抛出业务异常，且不应调用 `newsService.create` | 跳过（@Disabled） | 待测 |
