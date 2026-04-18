# 测试用例设计文档 - OrderServiceImpl

## 文档信息

| 字段 | 值 |
|------|----|
| 模块名称 | `OrderServiceImpl` |
| 负责成员 | `成员B` |
| 用例编号段 | `UT-OR-001 ~ UT-OR-099` |
| 创建日期 | `2026-04-02` |
| 最后更新 | `2026-04-02` |

### 变更记录

| 版本 | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-04-02 | 成员B | 初始版本，补充 OrderServiceImpl 单元测试用例与 JUnit 对应关系 |

---

## 用例设计说明

- 黑盒测试重点覆盖 `venueName`、`orderID`、`hours` 的有效/无效等价类，以及 `hours=0/1` 的边界值。
- 白盒测试重点覆盖 `confirmOrder`、`finishOrder`、`rejectOrder` 中 `order == null` 判定的真/假分支，满足语句覆盖与判定覆盖。
- 对纯 DAO 转发型方法采用代表值用例，避免对无分支逻辑重复堆砌冗余样例。

---

## findById

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:findById(int orderID)`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testFindByIdDelegatesToDao()`
**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-001 | 输入存在的订单 ID | 返回对应 `Order` 对象 | 返回 DAO 提供的 `Order` 对象 | 正确 |

---

## findDateOrder

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:findDateOrder(int venueID, LocalDateTime startTime, LocalDateTime startTime2)`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testFindDateOrderDelegatesToDao()`
**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-002 | 输入合法场馆 ID 和合法时间区间 | 返回该时间区间内的订单列表 | 返回 DAO 提供的订单列表 | 正确 |

---

## findUserOrder

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:findUserOrder(String userID, Pageable pageable)`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testFindUserOrderDelegatesToDao()`
**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-003 | 输入存在的 `userID` 和合法分页参数 | 返回该用户的分页订单结果 | 返回 DAO 提供的分页结果 | 正确 |

---

## submit

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:submit(String venueName, LocalDateTime startTime, int hours, String userID)`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testSubmitCreatesOrderWithCalculatedTotal()`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testSubmitWithZeroHoursStillSavesOrder()`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testSubmitWithUnknownVenueThrowsNullPointerException()`
**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-004 | 合法场馆名、合法开始时间、`hours=1` | 创建订单成功，状态为 `STATE_NO_AUDIT`，总价按 `hours * price` 计算 | 成功保存订单，状态和总价计算正确 | 正确 |
| UT-OR-005 | 合法场馆名、合法开始时间、`hours=0` | 0 小时订单应被拒绝，不应保存 | 实际仍保存订单，`hours=0` 且 `total=0` | 错误 |
| UT-OR-006 | 不存在的场馆名、合法开始时间、合法 `hours` | 应提示场馆不存在并拒绝保存 | 实际抛出 `NullPointerException` | 错误 |

---

## updateOrder

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:updateOrder(int orderID, String venueName, LocalDateTime startTime, int hours, String userID)`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testUpdateOrderUpdatesExistingOrderUsingVenuePrice()`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testUpdateOrderWithMissingOrderThrowsNullPointerException()`
**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-007 | 存在的订单 ID、存在的场馆名、合法开始时间、下边界 `hours=1` | 更新订单成功，状态重置为 `STATE_NO_AUDIT`，总价按 1 小时重新计算 | 订单字段被正确更新并保存，总价等于场馆单价 | 正确 |
| UT-OR-008 | 不存在的订单 ID、存在的场馆名、合法开始时间、合法 `hours` | 应提示订单不存在并拒绝修改 | 实际抛出 `NullPointerException` | 错误 |

---

## delOrder

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:delOrder(int orderID)`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testDeleteOrderDelegatesToDao()`
**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-009 | 输入存在的订单 ID | 调用 DAO 删除该订单 | 成功调用 `deleteById(orderID)` | 正确 |

---

## confirmOrder

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:confirmOrder(int orderID)`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testConfirmOrderUpdatesStateWhenOrderExists()`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testConfirmOrderThrowsWhenOrderMissing()`
**设计技术**：`语句覆盖 + 判定覆盖`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-010 | 订单存在 | 将订单状态更新为 `STATE_WAIT` | 成功调用 `updateState(STATE_WAIT, orderID)` | 正确 |
| UT-OR-011 | 订单不存在 | 抛出运行时异常，且不执行状态更新 | 抛出 `RuntimeException`，未调用 `updateState` | 正确 |

---

## finishOrder

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:finishOrder(int orderID)`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testFinishOrderUpdatesStateWhenOrderExists()`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testFinishOrderThrowsWhenOrderMissing()`
**设计技术**：`语句覆盖 + 判定覆盖`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-012 | 订单存在 | 将订单状态更新为 `STATE_FINISH` | 成功调用 `updateState(STATE_FINISH, orderID)` | 正确 |
| UT-OR-013 | 订单不存在 | 抛出运行时异常，且不执行状态更新 | 抛出 `RuntimeException`，未调用 `updateState` | 正确 |

---

## rejectOrder

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:rejectOrder(int orderID)`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testRejectOrderUpdatesStateWhenOrderExists()`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testRejectOrderThrowsWhenOrderMissing()`
**设计技术**：`语句覆盖 + 判定覆盖`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-014 | 订单存在 | 将订单状态更新为 `STATE_REJECT` | 成功调用 `updateState(STATE_REJECT, orderID)` | 正确 |
| UT-OR-015 | 订单不存在 | 抛出运行时异常，且不执行状态更新 | 抛出 `RuntimeException`，未调用 `updateState` | 正确 |

---

## findNoAuditOrder

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:findNoAuditOrder(Pageable pageable)`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testFindNoAuditOrderDelegatesToDao()`
**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-016 | 输入合法分页参数 | 返回未审核订单分页结果 | 返回 DAO 提供的未审核分页结果 | 正确 |

---

## findAuditOrder

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:findAuditOrder()`
**测试函数**：`src.test.java.com.demo.service.impl.OrderServiceImplTest:testFindAuditOrderDelegatesToDao()`
**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-017 | 查询已审核订单 | 返回状态为 `STATE_WAIT` 和 `STATE_FINISH` 的订单集合 | 返回 DAO 提供的审核后订单集合 | 正确 |
