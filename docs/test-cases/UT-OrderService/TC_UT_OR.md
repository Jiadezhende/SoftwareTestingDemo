# 测试用例设计文档 - OrderServiceImpl

## 文档信息

| 字段 | 值 |
|------|----|
| 模块名称 | `OrderServiceImpl` |
| 负责成员 | `成员B` |
| 用例编号段 | `UT-OR-001 ~ UT-OR-048` |
| 创建日期 | `2026-04-02` |
| 最后更新 | `2026-04-23` |

### 变更记录

| 版本 | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-04-02 | 成员B | 初始版本，补充 `OrderServiceImpl` 单元测试用例与 JUnit 对应关系 |
| v1.1 | 2026-04-21 | 成员B | 根据 `BlackBoxSelfCheck` 补充状态机校验相关用例 |
| v1.2 | 2026-04-21 | 成员B | 将已知缺陷改为按需求断言的失败测试，状态机缺口转为正式错误用例 |
| v1.3 | 2026-04-23 | 成员B | 根据自查表补充弱覆盖方法的空结果、非法边界和代表值用例 |
| v1.4 | 2026-04-23 | 成员B | 补充 DAO 异常透传用例，验证当前 Service 层透传设计 |
| v1.5 | 2026-04-23 | 成员B | 根据 OrderService 缺口清单补充负边界、空值和非法 ID 用例 |
| v1.6 | 2026-04-23 | 成员B | 补充 `findDateOrder` 的无效时间区间等价类 |
| v1.7 | 2026-04-23 | 成员B | 补充 `findById`、`findUserOrder`、`findNoAuditOrder` 的非法输入等价类 |
| v1.8 | 2026-04-23 | 成员B | 按等价类组合原则拆分无效代表值，一类一用例 |
| v1.9 | 2026-04-23 | 成员B | 按当前文档顺序重排用例编号，并同步缺陷引用 |

---

## 用例设计说明

- 黑盒测试重点覆盖 `venueName`、`orderID`、`hours` 的有效/无效等价类，以及 `hours=0/1` 的边界值。
- 白盒测试重点覆盖 `confirmOrder`、`finishOrder`、`rejectOrder` 中 `order == null` 与非法前置状态分支，体现语句覆盖和判定覆盖。
- 对无业务判断、仅做 DAO 透传的方法使用代表值加空结果补充用例，兼顾完整性与去冗余。
- 对当前 Service 层不捕获 DAO 异常的方法，补充异常透传用例，验证异常原样向上抛出且后续 DAO 不再执行。

---

## findById

**测试对象**：`OrderServiceImpl.findById(int orderID)`
**测试函数**：`OrderServiceImplTest:testFindById_delegatesToDao`
**测试函数**：`OrderServiceImplTest:testFindById_notFound`
**测试函数**：`OrderServiceImplTest:testFindById_zeroId`
**测试函数**：`OrderServiceImplTest:testFindById_daoException`
**设计技术**：等价类划分

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-001 | 输入存在的订单 ID | 返回对应 `Order` 对象 | 返回 DAO 提供的 `Order` 对象 | 正确 |
| UT-OR-002 | 输入不存在的订单 ID | 返回空结果，不应额外抛异常 | 返回 `null`，并调用 `getOne(orderID)` | 正确 |
| UT-OR-003 | 输入 `orderID<=0` 的代表值 `0` | 应拒绝非法订单 ID，不应调用 DAO | 实际未校验非法 ID，仍继续调用 `getOne` | 错误 |
| UT-OR-004 | DAO 查询订单时抛出数据访问异常 | 原样抛出同一异常对象 | 原样抛出 `DataAccessResourceFailureException` | 正确 |

---

## findDateOrder

**测试对象**：`OrderServiceImpl.findDateOrder(int venueID, LocalDateTime startTime, LocalDateTime startTime2)`
**测试函数**：`OrderServiceImplTest:testFindDateOrder_delegatesToDao`
**测试函数**：`OrderServiceImplTest:testFindDateOrder_noMatch`
**测试函数**：`OrderServiceImplTest:testFindDateOrder_invalidTimeRange`
**测试函数**：`OrderServiceImplTest:testFindDateOrder_daoException`
**设计技术**：等价类划分

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-005 | 输入合法场馆 ID 和合法时间区间 | 返回该时间区间内的订单列表 | 返回 DAO 提供的订单列表 | 正确 |
| UT-OR-006 | 输入合法场馆 ID 和合法时间区间，但无匹配订单 | 返回空列表 | 返回空列表 | 正确 |
| UT-OR-007 | 输入合法场馆 ID，但 `startTime > endTime` | 应拒绝非法时间区间，不应调用 DAO | 实际未校验时间区间，仍继续调用 DAO | 错误 |
| UT-OR-008 | DAO 按场馆和时间区间查询时抛出数据访问异常 | 原样抛出同一异常对象 | 原样抛出 `DataAccessResourceFailureException` | 正确 |

---

## findUserOrder

**测试对象**：`OrderServiceImpl.findUserOrder(String userID, Pageable pageable)`
**测试函数**：`OrderServiceImplTest:testFindUserOrder_delegatesToDao`
**测试函数**：`OrderServiceImplTest:testFindUserOrder_unknownUser`
**测试函数**：`OrderServiceImplTest:testFindUserOrder_nullUserId`
**测试函数**：`OrderServiceImplTest:testFindUserOrder_daoException`
**设计技术**：等价类划分

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-009 | 输入存在的 `userID` 和合法分页参数 | 返回该用户的分页订单结果 | 返回 DAO 提供的分页结果 | 正确 |
| UT-OR-010 | 输入不存在的 `userID` 和合法分页参数 | 返回空分页结果 | 返回空分页结果 | 正确 |
| UT-OR-011 | 输入空用户标识代表值 `userID=null`，分页参数合法 | 应拒绝非法用户标识，不应调用 DAO | 实际未校验非法 `userID`，仍继续调用 DAO | 错误 |
| UT-OR-012 | DAO 查询用户分页订单时抛出数据访问异常 | 原样抛出同一异常对象 | 原样抛出 `DataAccessResourceFailureException` | 正确 |

---

## submit

**测试对象**：`OrderServiceImpl.submit(String venueName, LocalDateTime startTime, int hours, String userID)`
**测试函数**：`OrderServiceImplTest:testSubmit_success`
**测试函数**：`OrderServiceImplTest:testSubmit_zeroHours`
**测试函数**：`OrderServiceImplTest:testSubmit_negativeHours`
**测试函数**：`OrderServiceImplTest:testSubmit_unknownVenue`
**测试函数**：`OrderServiceImplTest:testSubmit_nullStartTime`
**测试函数**：`OrderServiceImplTest:testSubmit_nullUserId`
**测试函数**：`OrderServiceImplTest:testSubmit_venueDaoException`
**设计技术**：等价类划分 + 边界值分析

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-013 | 合法场馆名、合法开始时间、`hours=1` | 创建订单成功，状态为 `STATE_NO_AUDIT`，总价按 `hours * price` 计算 | 成功保存订单，状态和总价计算正确 | 正确 |
| UT-OR-014 | 合法场馆名、合法开始时间、`hours=0` | 非法预约时长应被拒绝，不应保存 | 实际未拦截 `0`，仍继续保存订单 | 错误 |
| UT-OR-015 | 不存在的场馆名、合法开始时间、合法 `hours` | 应提示场馆不存在并拒绝保存 | 实际抛出 `NullPointerException` | 错误 |
| UT-OR-016 | 合法场馆名、`startTime=null`、合法 `hours` | 应拒绝空开始时间，不应保存 | 实际未校验空开始时间，仍继续保存订单 | 错误 |
| UT-OR-017 | 合法场馆名、合法开始时间、空用户标识代表值 `userID=null` | 应拒绝空用户标识，不应保存 | 实际未校验 `userID`，仍继续保存订单 | 错误 |
| UT-OR-018 | 合法场馆名、合法开始时间、`hours=-1` | 非法预约时长应被拒绝，不应保存 | 实际未拦截 `-1`，仍继续保存订单 | 错误 |
| UT-OR-019 | 查询场馆时 DAO 抛出数据访问异常 | 原样抛出同一异常对象，且不执行 `save` | 原样抛出 `DataAccessResourceFailureException`，未调用 `save` | 正确 |

---

## updateOrder

**测试对象**：`OrderServiceImpl.updateOrder(int orderID, String venueName, LocalDateTime startTime, int hours, String userID)`
**测试函数**：`OrderServiceImplTest:testUpdateOrder_success`
**测试函数**：`OrderServiceImplTest:testUpdateOrder_missingOrder`
**测试函数**：`OrderServiceImplTest:testUpdateOrder_zeroHours`
**测试函数**：`OrderServiceImplTest:testUpdateOrder_negativeHours`
**测试函数**：`OrderServiceImplTest:testUpdateOrder_unknownVenue`
**测试函数**：`OrderServiceImplTest:testUpdateOrder_daoException`
**设计技术**：等价类划分 + 边界值分析

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-020 | 存在的订单 ID、存在的场馆名、合法开始时间、下边界 `hours=1` | 更新订单成功，状态重置为 `STATE_NO_AUDIT`，总价按 1 小时重新计算 | 订单字段被正确更新并保存，总价等于场馆单价 | 正确 |
| UT-OR-021 | 不存在的订单 ID、存在的场馆名、合法开始时间、合法 `hours` | 应提示订单不存在并拒绝修改 | 实际抛出 `NullPointerException` | 错误 |
| UT-OR-022 | 存在的订单 ID、存在的场馆名、合法开始时间、`hours=0` | 非法修改时长应被拒绝，不应保存 | 实际未拦截 `0`，仍继续更新并保存订单 | 错误 |
| UT-OR-023 | 存在的订单 ID、不存在的场馆名、合法开始时间、合法 `hours` | 应提示场馆不存在并拒绝修改 | 实际抛出 `NullPointerException` | 错误 |
| UT-OR-024 | 存在的订单 ID、存在的场馆名、合法开始时间、`hours=-1` | 非法修改时长应被拒绝，不应保存 | 实际未拦截 `-1`，仍继续更新并保存订单 | 错误 |
| UT-OR-025 | 查询订单时 DAO 抛出数据访问异常 | 原样抛出同一异常对象，且不执行 `save` | 原样抛出 `DataAccessResourceFailureException`，未调用 `save` | 正确 |

---

## delOrder

**测试对象**：`OrderServiceImpl.delOrder(int orderID)`
**测试函数**：`OrderServiceImplTest:testDelOrder_delegatesToDao`
**测试函数**：`OrderServiceImplTest:testDelOrder_notFound`
**测试函数**：`OrderServiceImplTest:testDelOrder_zeroId`
**测试函数**：`OrderServiceImplTest:testDelOrder_daoException`
**设计技术**：等价类划分

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-026 | 输入存在的订单 ID | 调用 DAO 删除该订单 | 成功调用 `deleteById(orderID)` | 正确 |
| UT-OR-027 | 输入不存在的订单 ID | 服务层仍透传删除请求，不额外抛异常 | 成功调用 `deleteById(orderID)` | 正确 |
| UT-OR-028 | 输入 `orderID<=0` 的代表值 `0` | 应拒绝非法订单 ID，不应调用删除 | 实际未校验非法 ID，仍继续调用 `deleteById` | 错误 |
| UT-OR-029 | DAO 删除订单时抛出数据访问异常 | 原样抛出同一异常对象 | 原样抛出 `EmptyResultDataAccessException` | 正确 |

---

## confirmOrder

**测试对象**：`OrderServiceImpl.confirmOrder(int orderID)`
**测试函数**：`OrderServiceImplTest:testConfirmOrder_success`
**测试函数**：`OrderServiceImplTest:testConfirmOrder_notFound`
**测试函数**：`OrderServiceImplTest:testConfirmOrder_illegalState`
**测试函数**：`OrderServiceImplTest:testConfirmOrder_daoException`
**设计技术**：语句覆盖 + 判定覆盖

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-030 | 订单存在 | 将订单状态更新为 `STATE_WAIT` | 成功调用 `updateState(STATE_WAIT, orderID)` | 正确 |
| UT-OR-031 | 订单不存在 | 抛出运行时异常，且不执行状态更新 | 抛出 `RuntimeException`，未调用 `updateState` | 正确 |
| UT-OR-032 | 输入非法前置状态代表值 `STATE_WAIT` 的订单 | 应拒绝非法前置状态并抛出业务异常，不更新订单状态 | 实际未拦截非法状态，直接更新为 `STATE_WAIT` | 错误 |
| UT-OR-033 | 查询订单时 DAO 抛出数据访问异常 | 原样抛出同一异常对象，且不执行 `updateState` | 原样抛出 `DataAccessResourceFailureException`，未调用 `updateState` | 正确 |

---

## finishOrder

**测试对象**：`OrderServiceImpl.finishOrder(int orderID)`
**测试函数**：`OrderServiceImplTest:testFinishOrder_success`
**测试函数**：`OrderServiceImplTest:testFinishOrder_notFound`
**测试函数**：`OrderServiceImplTest:testFinishOrder_illegalState`
**测试函数**：`OrderServiceImplTest:testFinishOrder_daoException`
**设计技术**：语句覆盖 + 判定覆盖

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-034 | 订单存在 | 将订单状态更新为 `STATE_FINISH` | 成功调用 `updateState(STATE_FINISH, orderID)` | 正确 |
| UT-OR-035 | 订单不存在 | 抛出运行时异常，且不执行状态更新 | 抛出 `RuntimeException`，未调用 `updateState` | 正确 |
| UT-OR-036 | 输入非法前置状态代表值 `STATE_NO_AUDIT` 的订单 | 应拒绝非法前置状态并抛出业务异常，不更新订单状态 | 实际未拦截非法状态，直接更新为 `STATE_FINISH` | 错误 |
| UT-OR-037 | 查询订单时 DAO 抛出数据访问异常 | 原样抛出同一异常对象，且不执行 `updateState` | 原样抛出 `DataAccessResourceFailureException`，未调用 `updateState` | 正确 |

---

## rejectOrder

**测试对象**：`OrderServiceImpl.rejectOrder(int orderID)`
**测试函数**：`OrderServiceImplTest:testRejectOrder_success`
**测试函数**：`OrderServiceImplTest:testRejectOrder_notFound`
**测试函数**：`OrderServiceImplTest:testRejectOrder_illegalState`
**测试函数**：`OrderServiceImplTest:testRejectOrder_daoException`
**设计技术**：语句覆盖 + 判定覆盖

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-038 | 订单存在 | 将订单状态更新为 `STATE_REJECT` | 成功调用 `updateState(STATE_REJECT, orderID)` | 正确 |
| UT-OR-039 | 订单不存在 | 抛出运行时异常，且不执行状态更新 | 抛出 `RuntimeException`，未调用 `updateState` | 正确 |
| UT-OR-040 | 输入非法前置状态代表值 `STATE_WAIT` 的订单 | 应拒绝非法前置状态并抛出业务异常，不更新订单状态 | 实际未拦截非法状态，直接更新为 `STATE_REJECT` | 错误 |
| UT-OR-041 | 查询订单时 DAO 抛出数据访问异常 | 原样抛出同一异常对象，且不执行 `updateState` | 原样抛出 `DataAccessResourceFailureException`，未调用 `updateState` | 正确 |

---

## findNoAuditOrder

**测试对象**：`OrderServiceImpl.findNoAuditOrder(Pageable pageable)`
**测试函数**：`OrderServiceImplTest:testFindNoAuditOrder_delegatesToDao`
**测试函数**：`OrderServiceImplTest:testFindNoAuditOrder_empty`
**测试函数**：`OrderServiceImplTest:testFindNoAuditOrder_nullPageable`
**测试函数**：`OrderServiceImplTest:testFindNoAuditOrder_daoException`
**设计技术**：等价类划分

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-042 | 输入合法分页参数 | 返回未审核订单分页结果 | 返回 DAO 提供的未审核分页结果 | 正确 |
| UT-OR-043 | 输入合法分页参数，但当前无未审核订单 | 返回空分页结果 | 返回空分页结果 | 正确 |
| UT-OR-044 | 输入 `pageable=null` | 应拒绝空分页参数，不应调用 DAO | 实际未校验空分页参数，仍继续调用 DAO | 错误 |
| UT-OR-045 | DAO 查询未审核订单时抛出数据访问异常 | 原样抛出同一异常对象 | 原样抛出 `DataAccessResourceFailureException` | 正确 |

---

## findAuditOrder

**测试对象**：`OrderServiceImpl.findAuditOrder()`
**测试函数**：`OrderServiceImplTest:testFindAuditOrder_delegatesToDao`
**测试函数**：`OrderServiceImplTest:testFindAuditOrder_empty`
**测试函数**：`OrderServiceImplTest:testFindAuditOrder_daoException`
**设计技术**：等价类划分


| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-OR-046 | 查询已审核订单 | 返回状态为 `STATE_WAIT` 和 `STATE_FINISH` 的订单集合 | 返回 DAO 提供的审核后订单集合 | 正确 |
| UT-OR-047 | 当前无已审核订单 | 返回空列表 | 返回空列表 | 正确 |
| UT-OR-048 | DAO 查询已审核订单时抛出数据访问异常 | 原样抛出同一异常对象 | 原样抛出 `DataAccessResourceFailureException` | 正确 |

---
