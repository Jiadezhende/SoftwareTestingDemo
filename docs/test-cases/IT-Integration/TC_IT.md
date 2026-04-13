# 测试用例设计文档 — 集成测试

## 文档信息

| 字段      | 值 |
|----------|----|
| 测试类型  | 集成测试 |
| 负责成员  | 成员E |
| 用例编号段 | `IT-LG/OR/VN/MG-001 ~ 099` |
| 创建日期  | 2026-04-08 |
| 最后更新  | 2026-04-08 |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-04-08 | 成员E | 初稿创建并完成首轮执行 |

---

## 登录流程

**测试对象**：`src.main.java.com.demo.controller.user.UserController.java:login(String userID, String password, HttpServletRequest)`
**测试脚本**：`src.test.java.com.demo.integration.IntegrationFlowTest:testLoginSuccessShouldWriteUserSession(), testLoginWrongPasswordShouldReturnFalse()`

| 功能点列表 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-----------|---------|---------|---------|---------|------|
| 普通用户登录 | IT-LG-001 | 合法用户提交正确用户名与密码 | 响应体为 `/index`，Session 写入 `user` | 响应体为 `/index`，Session 成功写入 `user` | 正确 |
| 密码错误 | IT-LG-002 | 用户名存在但密码错误 | 响应体为 `false` | 响应体为 `false` | 正确 |

---

## 订单流程

**测试对象**：`src.main.java.com.demo.controller.user.OrderController.java:order_manage(Model, HttpServletRequest), addOrder(String, String, String, int, HttpServletRequest, HttpServletResponse)`
**测试脚本**：`src.test.java.com.demo.integration.IntegrationFlowTest:testOrderManageWithoutLoginShouldThrowLoginException(), testAddOrderWithLoginShouldRedirectAndInvokeSubmit()`

| 功能点列表 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-----------|---------|---------|---------|---------|------|
| 未登录访问订单管理 | IT-OR-001 | 未登录直接访问 `/order_manage` | 抛出 `LoginException`，请求被拒绝 | 返回 500，抛出 `LoginException` | 正确 |
| 登录后提交订单 | IT-OR-002 | 已登录用户提交合法订单参数 | 跳转到 `order_manage`，并调用提交逻辑 | 响应重定向到 `order_manage`，`orderService.submit` 被调用 1 次 | 正确 |

---

## 场馆流程

**测试对象**：`src.main.java.com.demo.controller.user.VenueController.java:venue_list(int), toGymPage(Model, int)`
**测试脚本**：`src.test.java.com.demo.integration.IntegrationFlowTest:testVenueListShouldReturnPagedData(), testVenueDetailShouldRenderVenuePage()`

| 功能点列表 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-----------|---------|---------|---------|---------|------|
| 分页获取场馆列表 | IT-VN-001 | 请求第一页场馆数据 | 返回分页 JSON，`content` 非空且包含场馆名 | 返回分页 JSON，`content[0].venueName=羽毛球馆A` | 正确 |
| 场馆详情页渲染 | IT-VN-002 | 通过 `venueID` 访问场馆详情页 | 返回详情页面并包含对应场馆数据 | 返回 200，页面包含 `羽毛球馆A` | 正确 |

---

## 留言流程

**测试对象**：`src.main.java.com.demo.controller.user.MessageController.java:sendMessage(String, String, HttpServletResponse), user_message_list(int, HttpServletRequest)`
**测试脚本**：`src.test.java.com.demo.integration.IntegrationFlowTest:testSendMessageShouldRedirectAndPersistDefaultState(), testFindUserMessageWithoutLoginShouldThrowLoginException()`

| 功能点列表 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-----------|---------|---------|---------|---------|------|
| 提交留言 | IT-MG-001 | 用户提交留言内容 | 重定向到 `/message_list`，留言默认状态为未审核 | 响应重定向到 `/message_list`，`message.state=1` | 正确 |
| 未登录查询个人留言 | IT-MG-002 | 未登录访问 `/message/findUserList` | 抛出 `LoginException`，请求被拒绝 | 返回 500，抛出 `LoginException` | 正确 |
