# 测试用例设计文档

**最后更新：** 2026-04-25

## 分工说明

| 模块 | 负责人 | 用例编号段 | 测试类 |
|---|---|---|---|
| UserServiceImpl | 印伟辰 | UT-US-001 ~ UT-US-030 | `UserServiceImplTest` |
| OrderServiceImpl | 顾祎炜 | UT-OR-001 ~ UT-OR-048 | `OrderServiceImplTest` |
| VenueServiceImpl | 丘俊 | UT-VN-001 ~ UT-VN-031 | `VenueServiceImplTest` |
| NewsServiceImpl | 丘俊 | UT-NW-001 ~ UT-NW-020 | `NewsServiceImplTest` |
| MessageServiceImpl | 俞楚凡 | UT-MG-001 ~ UT-MG-042 | `MessageServiceImplTest` |
| 集成测试 | 高伟博 | IT-INT-001 ~ IT-INT-029 | `IntegrationFlowTest` |

---

# 一、UserServiceImpl 单元测试

**设计技术总览：** 等价类划分、边界值分析、错误推测

## findByUserID(String)

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:findByUserID(String userID)`

**测试函数**：`UserServiceImplTest:testFindByUserID_found()、testFindByUserID_notFound()、testFindByUserID_emptyString()、testFindByUserID_null()`

**设计技术**：`等价类划分`

### 等价类划分

| 等价类编号 | 输入条件 | 类型 |
|-----------|---------|------|
| EC1 | userID 在数据库中存在 | 有效 |
| EC2 | userID 在数据库中不存在 | 无效 |
| EC3 | userID = `""`（空字符串） | 边界 |
| EC4 | userID = `null` | 无效 |

### 测试用例

| 用例编号 | 对应等价类 | 用例描述 | 输入 | 预期结果 | 测试结果 | 结论 |
|---------|-----------|---------|------|---------|---------|------|
| UT-US-001 | EC1 | userID 存在时返回对应 User | `"user001"` | 返回非 null 的 User，且 userID = `"user001"` | 通过 | 正确 |
| UT-US-002 | EC2 | userID 不存在时返回 null | `"ghost"` | 返回 null | 通过 | 正确 |
| UT-US-003 | EC3 | userID 为空字符串时返回 null | `""` | 返回 null | 通过 | 正确 |
| UT-US-004 | EC4 | userID 为 null 时返回 null | `null` | 返回 null | 通过 | 正确 |

---

## findById(int)

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:findById(int id)`

**测试函数**：`UserServiceImplTest:testFindById_found()、testFindById_notFound()、testFindById_zero()、testFindById_negative()`

**设计技术**：`等价类划分 + 边界值分析`

### 等价类划分

| 等价类编号 | 输入条件 | 类型 |
|-----------|---------|------|
| EC5 | id > 0 且在数据库中存在 | 有效 |
| EC6 | id > 0 但在数据库中不存在 | 无效 |
| EC7 | id = 0（边界值） | 边界 |
| EC8 | id < 0 | 无效 |

### 测试用例

| 用例编号 | 对应等价类 | 用例描述 | 输入 | 预期结果 | 测试结果 | 结论 |
|---------|-----------|---------|------|---------|---------|------|
| UT-US-005 | EC5 | id 存在时返回对应 User | `1` | 返回非 null 的 User，且 id = `1` | 通过 | 正确 |
| UT-US-006 | EC6 | id 不存在时返回 null | `999` | 返回 null | 通过 | 正确 |
| UT-US-007 | EC7 | id = 0（边界值）时返回 null | `0` | 返回 null | 通过 | 正确 |
| UT-US-008 | EC8 | id 为负数时返回 null | `-1` | 返回 null | 通过 | 正确 |

---

## findByUserID(Pageable)

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:findByUserID(Pageable pageable)`

**测试函数**：`UserServiceImplTest:testFindByUserID_pageable_hasUsers()、testFindByUserID_pageable_empty()`

**设计技术**：`等价类划分`

### 等价类划分

| 等价类编号 | 输入条件 | 类型 |
|-----------|---------|------|
| EC9 | 数据库中存在 isadmin=0 的普通用户 | 有效 |
| EC10 | 数据库中不存在 isadmin=0 的普通用户 | 无效 |

### 测试用例

| 用例编号 | 对应等价类 | 用例描述 | 输入 | 预期结果 | 测试结果 | 结论 |
|---------|-----------|---------|------|---------|---------|------|
| UT-US-009 | EC9 | 存在普通用户时返回含数据的分页 | `PageRequest.of(0,10)` | 返回 totalElements=1，且元素 isadmin=0 | 通过 | 正确 |
| UT-US-010 | EC10 | 无普通用户时返回空分页 | `PageRequest.of(0,10)` | 返回 totalElements=0，content 为空 | 通过 | 正确 |

---

## checkLogin

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:checkLogin(String userID, String password)`

**测试函数**：`UserServiceImplTest:testCheckLogin_success()、testCheckLogin_wrongPassword()、testCheckLogin_userNotFound()、testCheckLogin_emptyPassword()、testCheckLogin_nullUserID()、testCheckLogin_nullPassword()`

**设计技术**：`等价类划分`

### 等价类划分

| 等价类编号 | 输入条件 | 类型 |
|-----------|---------|------|
| EC11 | userID 正确 + password 正确 | 有效 |
| EC12 | userID 正确 + password 错误 | 无效 |
| EC13 | userID 不存在 | 无效 |
| EC14 | password = `""`（空字符串） | 边界 |
| EC15 | userID = `null` | 无效 |
| EC16 | password = `null` | 无效 |

### 测试用例

| 用例编号 | 对应等价类 | 用例描述 | 输入 | 预期结果 | 测试结果 | 结论 |
|---------|-----------|---------|------|---------|---------|------|
| UT-US-011 | EC11 | 账号密码均正确时返回 User | `"user001"`, `"123456"` | 返回非 null 的 User，且 userID = `"user001"` | 通过 | 正确 |
| UT-US-012 | EC12 | 密码错误时返回 null | `"user001"`, `"wrong"` | 返回 null | 通过 | 正确 |
| UT-US-013 | EC13 | 账号不存在时返回 null | `"nobody"`, `"123456"` | 返回 null | 通过 | 正确 |
| UT-US-014 | EC14 | 密码为空字符串时返回 null | `"user001"`, `""` | 返回 null | 通过 | 正确 |
| UT-US-015 | EC15 | userID 为 null 时服务层应抛出 LoginException | `null`, `"123456"` | 抛出 `LoginException`，且不调用 `userDao.findByUserIDAndPassword` | 失败（缺陷暴露） | 错误 |
| UT-US-016 | EC16 | password 为 null 时服务层应抛出 LoginException | `"user001"`, `null` | 抛出 `LoginException`，且不调用 `userDao.findByUserIDAndPassword` | 失败（缺陷暴露） | 错误 |

---

## create

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:create(User user)`

**测试函数**：`UserServiceImplTest:testCreate_multipleUsers()、testCreate_firstUser()、testCreate_nullUser()、testCreate_emptyPassword()、testCreate_nullUserID()、testCreate_duplicateUserID()`

**设计技术**：`等价类划分`

### 等价类划分

| 等价类编号 | 输入条件 | 类型 |
|-----------|---------|------|
| EC17 | 有效 User，表中已有其他数据 | 有效 |
| EC18 | 有效 User，为表中首条记录（边界值） | 边界 |
| EC19 | user = `null` | 无效 |

### 测试用例

| 用例编号 | 对应等价类 | 用例描述 | 输入 | 预期结果 | 测试结果 | 结论 |
|---------|-----------|---------|------|---------|---------|------|
| UT-US-017 | EC17 | 表中已有数据时，保存后返回全表总数 | 有效 User，findAll 返回 2 条 | 调用 save 一次，返回 `2` | 通过 | 正确 |
| UT-US-018 | EC18 | 首个用户时，保存后返回总数 1 | 有效 User，findAll 返回 1 条 | 调用 save 一次，返回 `1` | 通过 | 正确 |
| UT-US-019 | EC19 | user 为 null 时抛出 IllegalArgumentException | `null` | 抛出 `IllegalArgumentException` | 通过 | 正确 |
| UT-US-028 | EC28 | 空密码应被拒绝，save 不应被调用 | `password = ""` 的 User | 抛出异常（类型待定） | 跳过（@Disabled） | 待测 |
| UT-US-029 | EC29 | userID 为 null 应被拒绝，save 不应被调用 | `userID = null` 的 User | 抛出异常（类型待定） | 跳过（@Disabled） | 待测 |
| UT-US-030 | EC30 | 重复 userID 应被拒绝，save 不应被调用 | 已存在的 `userID`（countUserID 返回 1） | 抛出异常（类型待定） | 跳过（@Disabled） | 待测 |

---

## delByID

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:delByID(int id)`

**测试函数**：`UserServiceImplTest:testDelByID_success()、testDelByID_notFound()、testDelByID_negativeId()`

**设计技术**：`等价类划分`

### 等价类划分

| 等价类编号 | 输入条件 | 类型 |
|-----------|---------|------|
| EC20 | id 在数据库中存在 | 有效 |
| EC21 | id 在数据库中不存在 | 无效 |
| EC22 | id < 0 | 无效 |

### 测试用例

| 用例编号 | 对应等价类 | 用例描述 | 输入 | 预期结果 | 测试结果 | 结论 |
|---------|-----------|---------|------|---------|---------|------|
| UT-US-020 | EC20 | id 存在时正常删除 | `1` | 调用 deleteById 一次，无异常 | 通过 | 正确 |
| UT-US-021 | EC21 | id 不存在时抛出 EmptyResultDataAccessException | `999` | 抛出 `EmptyResultDataAccessException` | 通过 | 正确 |
| UT-US-022 | EC22 | id 为负数时抛出 EmptyResultDataAccessException | `-1` | 抛出 `EmptyResultDataAccessException` | 通过 | 正确 |

---

## updateUser

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:updateUser(User user)`

**测试函数**：`UserServiceImplTest:testUpdateUser_valid()、testUpdateUser_nullUser()`

**设计技术**：`等价类划分`

### 等价类划分

| 等价类编号 | 输入条件 | 类型 |
|-----------|---------|------|
| EC23 | 有效 User 对象 | 有效 |
| EC24 | user = `null` | 无效 |

### 测试用例

| 用例编号 | 对应等价类 | 用例描述 | 输入 | 预期结果 | 测试结果 | 结论 |
|---------|-----------|---------|------|---------|---------|------|
| UT-US-023 | EC23 | 有效 User 时调用 save 保存 | 修改了 userName 的 User | 调用 save 一次，无异常 | 通过 | 正确 |
| UT-US-024 | EC24 | user 为 null 时抛出 IllegalArgumentException | `null` | 抛出 `IllegalArgumentException` | 通过 | 正确 |

---

## countUserID

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:countUserID(String userID)`

**测试函数**：`UserServiceImplTest:testCountUserID_exists()、testCountUserID_notExists()、testCountUserID_null()`

**设计技术**：`等价类划分`

### 等价类划分

| 等价类编号 | 输入条件 | 类型 |
|-----------|---------|------|
| EC25 | userID 在数据库中已存在 | 有效 |
| EC26 | userID 在数据库中不存在 | 无效 |
| EC27 | userID = `null` | 无效 |

### 测试用例

| 用例编号 | 对应等价类 | 用例描述 | 输入 | 预期结果 | 测试结果 | 结论 |
|---------|-----------|---------|------|---------|---------|------|
| UT-US-025 | EC25 | userID 已存在时返回 1 | `"user001"` | 返回 `1` | 通过 | 正确 |
| UT-US-026 | EC26 | userID 不存在时返回 0 | `"newUser"` | 返回 `0` | 通过 | 正确 |
| UT-US-027 | EC27 | userID 为 null 时返回 0 | `null` | 返回 `0` | 通过 | 正确 |

---

# 二、OrderServiceImpl 单元测试

**设计技术总览：** 等价类划分、边界值分析、状态机测试、ArgumentCaptor 参数验证

**用例设计说明：**
- 黑盒测试重点覆盖 `venueName`、`orderID`、`hours` 的有效/无效等价类，以及 `hours=0/1` 的边界值。
- 白盒测试重点覆盖 `confirmOrder`、`finishOrder`、`rejectOrder` 中 `order == null` 与非法前置状态分支，体现语句覆盖和判定覆盖。
- 对无业务判断、仅做 DAO 透传的方法使用代表值加空结果补充用例，兼顾完整性与去冗余。
- 对当前 Service 层不捕获 DAO 异常的方法，补充异常透传用例，验证异常原样向上抛出且后续 DAO 不再执行。

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

# 三、VenueServiceImpl 单元测试

**设计技术总览：** 等价类划分、边界值分析、决策表

## findByVenueID

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findByVenueID(int id)`

**测试函数**：`VenueServiceImplTest:testFindByVenueID_found()、testFindByVenueID_zeroId()、testFindByVenueID_daoException()`

**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-001 | 传入存在的场馆 ID | 调用 `venueDao.getOne(id)` 并返回对应 Venue 对象 | 返回 DAO 模拟的 Venue 对象，字段保持一致 | 正确 |
| UT-VN-009 | 边界值：传入 `id=0` 查询场馆 | 调用 `venueDao.getOne(0)` 并返回对应对象 | 返回 DAO 模拟的 `venueID=0` 场馆对象 | 正确 |
| UT-VN-010 | 异常路径：DAO 按 ID 查询场馆抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

---

## findByVenueName

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findByVenueName(String venueName)`

**测试函数**：`VenueServiceImplTest:testFindByVenueName_found()、testFindByVenueName_emptyName()、testFindByVenueName_daoException()`

**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-002 | 查询存在的场馆名 | 调用 `venueDao.findByVenueName(venueName)` 并返回对应 Venue 对象 | 返回 DAO 模拟的 Venue 对象 | 正确 |
| UT-VN-011 | 边界值：传入空字符串场馆名 | 调用 `venueDao.findByVenueName("")` 并返回 DAO 结果 | 返回 DAO 模拟的空名称场馆对象 | 正确 |
| UT-VN-012 | 异常路径：DAO 按名称查询场馆抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

---

## findAll(Pageable)

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findAll(Pageable pageable)`

**测试函数**：`VenueServiceImplTest:testFindAll_pageable()、testFindAll_pageable_emptyPage()、testFindAll_pageable_daoException()、testFindAll_pageable_nullPageable()`

**设计技术**：`等价类划分 + 边界值分析 + 决策表`

### 等价类划分（Pageable）

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `pageable != null` | 返回 `venueDao.findAll(pageable)` 的结果 |
| EC2 | `pageable == null` | 异常向上透传（由 DAO 或下层抛出） |

### 边界值分析（分页）

| 参数 | 边界值 | 覆盖用例 |
|------|--------|----------|
| `pageSize` | `1` | `UT-VN-013`（空分页） |
| `pageNumber` | `0`（第一页） | `UT-VN-003` / `UT-VN-013` |

### 决策表

| 条件/规则 | R1 | R2 | R3 | R4 |
|---|---|---|---|---|
| `pageable == null` | 否 | 否 | 否 | 是 |
| DAO 返回空分页 | 否 | 是 | — | — |
| DAO 抛异常 | 否 | 否 | 是 | 是 |
| 期望 | 返回分页结果 | 返回空分页 | 异常透传 | 异常透传 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-003 | 传入分页参数查询场馆列表 | 调用 `venueDao.findAll(pageable)` 并返回分页结果 | 返回 DAO 模拟的分页对象，记录数正确 | 正确 |
| UT-VN-013 | 边界值：传入第一页且 DAO 返回空分页 | 调用 `venueDao.findAll(pageable)` 并原样返回空分页对象 | 返回空分页对象，`totalElements=0` | 正确 |
| UT-VN-014 | 异常路径：DAO 分页查询场馆抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-VN-025 | 等价类：`pageable=null` | 调用 `venueDao.findAll(null)`，异常向上透传 | 捕获到 DAO 抛出的 `IllegalArgumentException`，对象一致 | 正确 |

---

## findAll()

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findAll()`

**测试函数**：`VenueServiceImplTest:testFindAll_noParam()、testFindAll_noParam_emptyList()、testFindAll_noParam_daoException()`

**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-004 | 查询全部场馆 | 调用 `venueDao.findAll()` 并返回列表结果 | 返回 DAO 模拟的场馆列表，共 2 条记录 | 正确 |
| UT-VN-015 | 边界值：DAO 返回空场馆列表 | 调用 `venueDao.findAll()` 并原样返回空列表 | 返回空列表，`size=0` | 正确 |
| UT-VN-016 | 异常路径：DAO 查询全部场馆抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

---

## create

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:create(Venue venue)`

**测试函数**：`VenueServiceImplTest:testCreate_success()、testCreate_zeroId()、testCreate_daoException()、testCreate_nullSavedEntity()、testCreate_negativePrice()、testCreate_emptyVenueName()、testCreate_duplicateVenueName()`

**设计技术**：`等价类划分 + 边界值分析 + 决策表`

### 等价类划分（入参 Venue）

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `venue != null` 且 `price >= 0` 且 `venueName` 非空且未重复 | 调用 `venueDao.save(venue)` 并返回 `save` 结果的 `venueID` |
| EC2 | `venue.price < 0` | 抛出业务异常，且不调用 `venueDao.save(venue)` |
| EC3 | `venue.venueName == ""` | 抛出业务异常，且不调用 `venueDao.save(venue)` |
| EC4 | `venueName` 已存在（需先查询数量） | 抛出业务异常，且不调用 `venueDao.save(venue)` |
| EC5 | `venue == null` | 异常向上透传（由 DAO 或下层抛出） |

### 边界值分析（返回值来源）

| 关注点 | 边界/特殊值 | 覆盖用例 |
|--------|------------|----------|
| `save(venue)` 返回对象 | `null` | `UT-VN-026` |
| 返回 `venueID` | `0` | `UT-VN-017` |
| `price` | `-1` | `UT-VN-029` |
| `venueName` | `""` | `UT-VN-030` |
| `countVenueName(venueName)` | `1`（已存在） | `UT-VN-031` |

### 决策表

| 条件/规则 | R1 | R2 | R3 | R4 | R5 | R6 | R7 |
|---|---|---|---|---|---|---|---|
| `price < 0` | 否 | 否 | 否 | 否 | 是 | 否 | 否 |
| `venueName == ""` | 否 | 否 | 否 | 否 | 否 | 是 | 否 |
| `countVenueName(venueName) > 0` | 否 | 否 | 否 | 否 | 否 | 否 | 是 |
| `venue == null` | 否 | 否 | 否 | 是 | — | — | — |
| DAO 返回 `savedVenue == null` | 否 | 是 | 否 | — | — | — | — |
| DAO 抛异常 | 否 | 否 | 是 | 是 | — | — | — |
| 期望 | 返回 `savedVenue.venueID` | 抛出 `NullPointerException` | 异常透传 | 异常透传 | 抛出业务异常且不保存 | 抛出业务异常且不保存 | 抛出业务异常且不保存 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-005 | 新增合法场馆对象，入参 `venueID=0` | 调用 `venueDao.save(venue)`，返回持久化后对象的 `venueID` | 入参 `venueID=0`，DAO 返回 `venueID=6`，方法最终返回 `6` | 正确 |
| UT-VN-017 | 边界值：新增场馆后持久化对象 `venueID=0` | 调用 `venueDao.save(venue)`，返回持久化后对象的 `venueID=0` | 方法最终返回 `0`，证明 service 未额外加工返回值 | 正确 |
| UT-VN-018 | 异常路径：DAO 保存场馆抛出异常 | 异常向上透传，不返回默认值 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-VN-026 | 边界值：DAO `save` 返回 `null` | 调用后获取 `venueID` 触发空指针异常（BUG-036） | 捕获到 `NullPointerException` | 正确 |
| UT-VN-029 | 缺陷占位：`price < 0` 的场馆不应被创建（BUG-023） | 应抛出业务异常，且不调用 `venueDao.save(venue)` | 跳过（@Disabled） | 待测 |
| UT-VN-030 | 缺陷占位：`venueName` 为空的场馆不应被创建（BUG-024） | 应抛出业务异常，且不调用 `venueDao.save(venue)` | 跳过（@Disabled） | 待测 |
| UT-VN-031 | 缺陷占位：重复 `venueName` 的场馆不应被创建（BUG-025） | 应先校验 `countVenueName`，若已存在则抛出业务异常 | 跳过（@Disabled） | 待测 |

---

## update

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:update(Venue venue)`

**测试函数**：`VenueServiceImplTest:testUpdate_success()、testUpdate_zeroId()、testUpdate_daoException()、testUpdate_nullVenue()`

**设计技术**：`等价类划分 + 边界值分析`

### 等价类划分（入参 Venue）

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `venue != null` | 调用 `venueDao.save(venue)` 并完成更新请求转发 |
| EC2 | `venue == null` | 异常向上透传（由 DAO 或下层抛出） |

### 决策表

| 条件/规则 | R1 | R2 | R3 |
|---|---|---|---|
| `venue == null` | 否 | 否 | 是 |
| DAO 抛异常 | 否 | 是 | 是 |
| 期望 | 正常转发保存 | 异常透传 | 异常透传 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-006 | 更新已有场馆对象 | 调用 `venueDao.save(venue)` 完成更新 | 校验到 `save` 被调用 1 次且参数正确 | 正确 |
| UT-VN-019 | 边界值：更新 `venueID=0` 的场馆对象 | 调用 `venueDao.save(venue)` 完成更新请求转发 | 校验到 `save` 被调用 1 次且参数为 `venueID=0` 对象 | 正确 |
| UT-VN-020 | 异常路径：DAO 更新场馆抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-VN-027 | 等价类：`venue=null` | 调用 `venueDao.save(null)`，异常向上透传 | 捕获到 DAO 抛出的 `IllegalArgumentException`，对象一致 | 正确 |

---

## delById

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:delById(int id)`

**测试函数**：`VenueServiceImplTest:testDelById_success()、testDelById_zeroId()、testDelById_daoException()`

**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-007 | 删除指定场馆 ID | 调用 `venueDao.deleteById(id)` 删除对应记录 | 校验到 `deleteById(8)` 被调用 1 次 | 正确 |
| UT-VN-021 | 边界值：删除 `id=0` 的场馆 | 调用 `venueDao.deleteById(0)` | 校验到 `deleteById(0)` 被正确调用 | 正确 |
| UT-VN-022 | 异常路径：DAO 删除场馆抛出异常 | 异常向上透传，不做吞并或忽略 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

---

## countVenueName

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:countVenueName(String venueName)`

**测试函数**：`VenueServiceImplTest:testCountVenueName_found()、testCountVenueName_emptyName()、testCountVenueName_daoException()、testCountVenueName_nullName()`

**设计技术**：`等价类划分 + 边界值分析`

### 等价类划分（venueName）

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `venueName` 为普通非空字符串 | 返回 `venueDao.countByVenueName(venueName)` |
| EC2 | `venueName == ""` | 返回 `venueDao.countByVenueName("")` |
| EC3 | `venueName == null` | 异常向上透传（由 DAO 或下层抛出） |

### 决策表

| 条件/规则 | R1 | R2 | R3 | R4 |
|---|---|---|---|---|
| `venueName == null` | 否 | 否 | 否 | 是 |
| `venueName == ""` | 否 | 是 | 否 | — |
| DAO 抛异常 | 否 | 否 | 是 | 是 |
| 期望 | 返回计数 | 返回计数 | 异常透传 | 异常透传 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-008 | 统计指定场馆名的数量 | 调用 `venueDao.countByVenueName(venueName)` 并返回数量 | 返回 DAO 模拟数量值 `2` | 正确 |
| UT-VN-023 | 边界值：统计空字符串场馆名数量 | 调用 `venueDao.countByVenueName("")` 并返回数量 | 返回 DAO 模拟数量值 `0` | 正确 |
| UT-VN-024 | 异常路径：DAO 统计场馆名数量抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-VN-028 | 等价类：`venueName=null` | 调用 `venueDao.countByVenueName(null)`，异常向上透传 | 捕获到 DAO 抛出的 `IllegalArgumentException`，对象一致 | 正确 |

---

# 四、NewsServiceImpl 单元测试

**设计技术总览：** 等价类划分、边界值分析、决策表

## findAll

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:findAll(Pageable pageable)`

**测试函数**：`NewsServiceImplTest:testFindAll_success()、testFindAll_emptyPage()、testFindAll_daoException()、testFindAll_nullPageable()`

**设计技术**：`等价类划分 + 边界值分析 + 决策表`

### 等价类划分（Pageable）

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `pageable != null` | 返回 `newsDao.findAll(pageable)` 的结果 |
| EC2 | `pageable == null` | 异常向上透传（由 DAO 或下层抛出） |

### 决策表

| 条件/规则 | R1 | R2 | R3 | R4 |
|---|---|---|---|---|
| `pageable == null` | 否 | 否 | 否 | 是 |
| DAO 返回空分页 | 否 | 是 | — | — |
| DAO 抛异常 | 否 | 否 | 是 | 是 |
| 期望 | 返回分页结果 | 返回空分页 | 异常透传 | 异常透传 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-001 | 传入分页参数查询新闻列表 | 调用 `newsDao.findAll(pageable)` 并返回分页结果 | 返回 DAO 模拟的分页对象，记录数正确 | 正确 |
| UT-NW-006 | 边界值：传入第一页且 DAO 返回空分页 | 调用 `newsDao.findAll(pageable)` 并原样返回空分页对象 | 返回空分页对象，`totalElements=0` | 正确 |
| UT-NW-007 | 异常路径：DAO 分页查询抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-NW-016 | 等价类：`pageable=null` | 调用 `newsDao.findAll(null)`，异常向上透传 | 捕获到 DAO 抛出的 `IllegalArgumentException`，对象一致 | 正确 |

---

## findById

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:findById(int newsID)`

**测试函数**：`NewsServiceImplTest:testFindById_success()、testFindById_zeroId()、testFindById_daoException()`

**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-002 | 查询存在的新闻 ID | 调用 `newsDao.getOne(newsID)` 并返回对应 News 对象 | 返回 DAO 模拟的 News 对象 | 正确 |
| UT-NW-008 | 边界值：查询 `newsID=0` 的新闻 | 调用 `newsDao.getOne(0)` 并返回对应对象 | 返回 DAO 模拟的 `newsID=0` 新闻对象 | 正确 |
| UT-NW-009 | 异常路径：DAO 查询单条新闻抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

---

## create

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:create(News news)`

**测试函数**：`NewsServiceImplTest:testCreate_success()、testCreate_zeroId()、testCreate_daoException()、testCreate_nullSavedEntity()、testCreate_emptyTitle()、testCreate_emptyContent()`

**设计技术**：`等价类划分 + 边界值分析 + 决策表`

### 等价类划分（入参 News）

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `news != null` 且 `title`、`content` 均为非空字符串 | 调用 `newsDao.save(news)` 并返回 `save` 结果的 `newsID` |
| EC2 | `news.title == ""` | 抛出业务异常，且不调用 `newsDao.save(news)` |
| EC3 | `news.content == ""` | 抛出业务异常，且不调用 `newsDao.save(news)` |
| EC4 | `news == null` | 异常向上透传（由 DAO 或下层抛出） |

### 决策表

| 条件/规则 | R1 | R2 | R3 | R4 | R5 | R6 |
|---|---|---|---|---|---|---|
| `title == ""` | 否 | 否 | 否 | 否 | 是 | 否 |
| `content == ""` | 否 | 否 | 否 | 否 | 否 | 是 |
| `news == null` | 否 | 否 | 否 | 是 | — | — |
| DAO 返回 `savedNews == null` | 否 | 是 | 否 | — | — | — |
| DAO 抛异常 | 否 | 否 | 是 | 是 | — | — |
| 期望 | 返回 `savedNews.newsID` | 抛出 `NullPointerException` | 异常透传 | 异常透传 | 抛出业务异常且不保存 | 抛出业务异常且不保存 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-003 | 新增合法新闻对象，入参 `newsID=0` | 调用 `newsDao.save(news)`，返回持久化后对象的 `newsID` | 入参 `newsID=0`，DAO 返回 `newsID=3`，方法最终返回 `3` | 正确 |
| UT-NW-010 | 边界值：新增新闻后持久化对象 `newsID=0` | 调用 `newsDao.save(news)`，返回持久化后对象的 `newsID=0` | 方法最终返回 `0` | 正确 |
| UT-NW-011 | 异常路径：DAO 保存新闻抛出异常 | 异常向上透传，不返回默认值 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-NW-017 | 边界值：DAO `save` 返回 `null`（BUG-037） | 调用后获取 `newsID` 触发空指针异常 | 捕获到 `NullPointerException` | 正确 |
| UT-NW-019 | 缺陷占位：`title` 为空的新闻不应被创建（BUG-021） | 应抛出业务异常，且不调用 `newsDao.save(news)` | 跳过（@Disabled） | 待测 |
| UT-NW-020 | 缺陷占位：`content` 为空的新闻不应被创建（BUG-022） | 应抛出业务异常，且不调用 `newsDao.save(news)` | 跳过（@Disabled） | 待测 |

---

## delById

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:delById(int newsID)`

**测试函数**：`NewsServiceImplTest:testDelById_success()、testDelById_zeroId()、testDelById_daoException()`

**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-004 | 删除指定新闻 ID | 调用 `newsDao.deleteById(newsID)` 删除对应记录 | 校验到 `deleteById(4)` 被调用 1 次 | 正确 |
| UT-NW-012 | 边界值：删除 `newsID=0` 的新闻 | 调用 `newsDao.deleteById(0)` | 校验到 `deleteById(0)` 被正确调用 | 正确 |
| UT-NW-013 | 异常路径：DAO 删除新闻抛出异常 | 异常向上透传，不做吞并或忽略 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

---

## update

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:update(News news)`

**测试函数**：`NewsServiceImplTest:testUpdate_success()、testUpdate_zeroId()、testUpdate_daoException()、testUpdate_nullNews()`

**设计技术**：`等价类划分 + 边界值分析`

### 等价类划分（入参 News）

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `news != null` | 调用 `newsDao.save(news)` 并完成更新请求转发 |
| EC2 | `news == null` | 异常向上透传（由 DAO 或下层抛出） |

### 决策表

| 条件/规则 | R1 | R2 | R3 |
|---|---|---|---|
| `news == null` | 否 | 否 | 是 |
| DAO 抛异常 | 否 | 是 | 是 |
| 期望 | 正常转发保存 | 异常透传 | 异常透传 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-005 | 更新已有新闻对象 | 调用 `newsDao.save(news)` 完成更新 | 校验到 `save` 被调用 1 次且参数正确 | 正确 |
| UT-NW-014 | 边界值：更新 `newsID=0` 的新闻对象 | 调用 `newsDao.save(news)` 完成更新请求转发 | 校验到 `save` 被调用 1 次且参数为 `newsID=0` 对象 | 正确 |
| UT-NW-015 | 异常路径：DAO 更新新闻抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-NW-018 | 等价类：`news=null` | 调用 `newsDao.save(null)`，异常向上透传 | 捕获到 DAO 抛出的 `IllegalArgumentException`，对象一致 | 正确 |

---

# 五、MessageServiceImpl 单元测试

**设计技术总览：** 等价类划分、边界值分析、语句/条件-判定/路径覆盖

**白盒覆盖说明：** 所有判定均为单一条件，条件覆盖与判定覆盖等价；每个方法最多 1 个判定、2 条路径，已有用例覆盖全部路径。语句覆盖 100%、条件-判定覆盖 100%、路径覆盖 100%。

## confirmMessage

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:confirmMessage(int messageID)`

**测试函数**：`MessageServiceImplTest:testConfirmMessage_existingId()、testConfirmMessage_nonExistingId()、testConfirmMessage_messageIdZero()、testConfirmMessage_messageIdOne()、testConfirmMessage_messageIdNegative()`

**设计技术**：`等价类划分 + 边界值分析`

**控制流：**
```
S1: Message message = messageDao.findByMessageID(messageID);
S2: if (message == null) → S3: throw RuntimeException("留言不存在")
                         → S4: messageDao.updateState(STATE_PASS, ...)
```

| 路径 | 执行序列 | 覆盖用例 |
|------|---------|---------|
| 路径1 | S1 → S2(false) → S4 | UT-MG-001, UT-MG-029 |
| 路径2 | S1 → S2(true) → S3 | UT-MG-002, UT-MG-028, UT-MG-030 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-001 | 传入数据库中存在的留言 ID(2) | 该留言状态变为 STATE_PASS(2) | DAO 的 updateState(STATE_PASS, 2) 被正确调用 | 正确 |
| UT-MG-002 | 传入数据库中不存在的留言 ID(9999) | 抛出 RuntimeException("留言不存在") | 抛出 RuntimeException("留言不存在") | 正确 |
| UT-MG-028 | confirmMessage 传入 messageID=0（下界-1） | 抛出 RuntimeException("留言不存在") | 抛出 RuntimeException("留言不存在") | 正确 |
| UT-MG-029 | confirmMessage 传入 messageID=1（下界） | 若留言存在则状态变为 STATE_PASS | updateState(STATE_PASS, 1) 被正确调用 | 正确 |
| UT-MG-030 | confirmMessage 传入 messageID=-1（负数） | 抛出 RuntimeException("留言不存在") | 抛出 RuntimeException("留言不存在") | 正确 |

---

## rejectMessage

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:rejectMessage(int messageID)`

**测试函数**：`MessageServiceImplTest:testRejectMessage_existingId()、testRejectMessage_nonExistingId()`

**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-003 | 传入数据库中存在的留言 ID(2) | 该留言状态变为 STATE_REJECT(3) | DAO 的 updateState(STATE_REJECT, 2) 被正确调用 | 正确 |
| UT-MG-004 | 传入数据库中不存在的留言 ID(9999) | 抛出 RuntimeException("留言不存在") | 抛出 RuntimeException("留言不存在") | 正确 |

---

## create

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:create(Message message)`

**测试函数**：`MessageServiceImplTest:testCreate_validMessage()、testCreate_nullMessage()、testCreate_nullUserID()、testCreate_nullContent()、testCreate_emptyContent()、testCreate_userIdLengthMin()、testCreate_userIdLengthMax()、testCreate_userIdLengthOverMax()、testCreate_stateZero()、testCreate_stateMin()、testCreate_stateMax()、testCreate_stateOverMax()`

**设计技术**：`等价类划分 + 边界值分析`

**等价类划分（userID 长度）**

| 等价类 | 边界值 | 覆盖用例 |
|-------|--------|---------|
| 下界（1字符） | 1 | UT-MG-033 |
| 上界（25字符） | 25 | UT-MG-034 |
| 超界（26字符） | 26 | UT-MG-035 |

**等价类划分（state）**

| 等价类 | 值 | 覆盖用例 |
|-------|-----|---------|
| 非法下界 | 0 | UT-MG-036 |
| 有效下界 | 1（待审核） | UT-MG-037 |
| 有效上界 | 3（驳回） | UT-MG-038 |
| 非法上界 | 4 | UT-MG-039 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-005 | 传入所有字段正常的 Message 对象 | 返回新增留言的 messageID（大于 0） | 返回 messageID=2 | 正确 |
| UT-MG-006 | 传入 null | 抛出异常 | 抛出 IllegalArgumentException | 正确 |
| UT-MG-007 | 传入 userID 为 null 的 Message | 抛出异常（数据库 NOT NULL 约束） | 抛出 DataIntegrityViolationException | 正确 |
| UT-MG-008 | 传入 content 为 null 的 Message | 拒绝创建，抛出异常或返回错误提示 | 跳过（@Disabled，BUG-031） | 跳过 |
| UT-MG-009 | 传入 content 为空字符串的 Message | 拒绝创建，抛出异常或返回错误提示 | 跳过（@Disabled，BUG-032） | 跳过 |
| UT-MG-033 | create 传入 userID 长度=1（下界） | 正常创建 | 正常创建，返回 messageID=30 | 正确 |
| UT-MG-034 | create 传入 userID 长度=25（上界） | 正常创建 | 正常创建，返回 messageID=31 | 正确 |
| UT-MG-035 | create 传入 userID 长度=26（上界+1） | 应拒绝创建，抛出数据截断异常 | 抛出 DataIntegrityViolationException | 正确 |
| UT-MG-036 | create 传入 state=0（下界-1，非法状态） | 应拒绝创建，抛出异常或返回错误 | 跳过（@Disabled，BUG-034） | 跳过 |
| UT-MG-037 | create 传入 state=1（下界，待审核） | 正常创建 | 正常创建，返回 messageID=34 | 正确 |
| UT-MG-038 | create 传入 state=3（上界，驳回） | 正常创建 | 正常创建，返回 messageID=35 | 正确 |
| UT-MG-039 | create 传入 state=4（上界+1，非法状态） | 应拒绝创建，抛出异常或返回错误 | 跳过（@Disabled，BUG-035） | 跳过 |

---

## findById

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:findById(int messageID)`

**测试函数**：`MessageServiceImplTest:testFindById_existingId()、testFindById_nonExistingId()、testFindById_messageIdZero()`

**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-010 | 传入数据库中存在的留言 ID(2) | 返回对应的 Message 对象 | 返回 Message 对象，messageID=2，userID="test" | 正确 |
| UT-MG-011 | 传入数据库中不存在的留言 ID(9999) | 抛出 EntityNotFoundException | 抛出 EntityNotFoundException | 正确 |
| UT-MG-031 | findById 传入 messageID=0 | 抛出 EntityNotFoundException | 抛出 EntityNotFoundException | 正确 |

---

## delById

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:delById(int messageID)`

**测试函数**：`MessageServiceImplTest:testDelById_existingId()、testDelById_nonExistingId()、testDelById_messageIdNegative()`

**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-012 | 传入数据库中存在的留言 ID(2) | 删除成功，无异常 | 删除成功，无异常 | 正确 |
| UT-MG-013 | 传入数据库中不存在的留言 ID(9999) | 抛出 EmptyResultDataAccessException | 抛出 EmptyResultDataAccessException | 正确 |
| UT-MG-032 | delById 传入 messageID=-1 | 抛出 EmptyResultDataAccessException | 抛出 EmptyResultDataAccessException | 正确 |

---

## update

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:update(Message message)`

**测试函数**：`MessageServiceImplTest:testUpdate_existingMessage()、testUpdate_nullMessage()、testUpdate_nonExistingMessage()`

**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-014 | 传入已存在的留言，修改 content 为新内容 | 更新成功，content 变为新值 | 更新成功，无异常 | 正确 |
| UT-MG-015 | 传入 null | 抛出异常 | 抛出 IllegalArgumentException | 正确 |
| UT-MG-016 | 传入数据库中不存在的留言（messageID=9999） | 应拒绝更新，抛出异常或返回错误提示 | 跳过（@Disabled，BUG-033） | 跳过 |

---

## findByUser

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:findByUser(String userID, Pageable pageable)`

**测试函数**：`MessageServiceImplTest:testFindByUser_existingUser()、testFindByUser_noMessages()、testFindByUser_nullUserID()、testFindByUser_emptyUserID()、testFindByUser_nullPageable()、testFindByUser_pageableMinBoundary()、testFindByUser_negativePageNumber()、testFindByUser_zeroPageSize()`

**设计技术**：`等价类划分 + 边界值分析`

**Pageable 边界值：**

| 参数 | 边界值 | 覆盖用例 |
|------|--------|---------|
| page=0, size=1（最小合法） | 合法下界 | UT-MG-040 |
| page=-1（非法页码） | 非法下界 | UT-MG-041 |
| size=0（非法每页条数） | 非法下界 | UT-MG-042 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-017 | 传入有留言记录的用户 ID("test") | 返回该用户的留言分页，内容不为空 | 返回包含 1 条留言的分页 | 正确 |
| UT-MG-018 | 传入无留言记录的用户 ID("nobody") | 返回空分页 | 返回空分页 | 正确 |
| UT-MG-019 | 传入 userID 为 null | 抛出异常或返回空分页 | 返回空分页 | 正确 |
| UT-MG-020 | 传入 userID 为空字符串 | 返回空分页 | 返回空分页 | 正确 |
| UT-MG-021 | 传入 pageable 为 null | 抛出异常 | 抛出 IllegalArgumentException | 正确 |
| UT-MG-040 | 传入 page=0, size=1（最小合法分页） | 返回最多 1 条数据的分页 | 返回包含 1 条数据的分页 | 正确 |
| UT-MG-041 | 传入 page=-1（下界-1，负数页码） | 抛出 IllegalArgumentException | 抛出 IllegalArgumentException | 正确 |
| UT-MG-042 | 传入 size=0（下界-1，每页0条） | 抛出 IllegalArgumentException | 抛出 IllegalArgumentException | 正确 |

---

## findWaitState

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:findWaitState(Pageable pageable)`

**测试函数**：`MessageServiceImplTest:testFindWaitState_hasData()、testFindWaitState_noData()、testFindWaitState_nullPageable()`

**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-022 | 数据库中存在待审核留言(state=1) | 返回非空分页，所有留言 state 均为 1 | 返回包含 1 条 state=1 留言的分页 | 正确 |
| UT-MG-023 | 数据库中无待审核留言 | 返回空分页 | 返回空分页 | 正确 |
| UT-MG-024 | 传入 pageable 为 null | 抛出异常 | 抛出 IllegalArgumentException | 正确 |

---

## findPassState

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:findPassState(Pageable pageable)`

**测试函数**：`MessageServiceImplTest:testFindPassState_hasData()、testFindPassState_noData()、testFindPassState_nullPageable()`

**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-025 | 数据库中存在已通过留言(state=2) | 返回非空分页，所有留言 state 均为 2 | 返回包含 1 条 state=2 留言的分页 | 正确 |
| UT-MG-026 | 数据库中无已通过留言 | 返回空分页 | 返回空分页 | 正确 |
| UT-MG-027 | 传入 pageable 为 null | 抛出异常 | 抛出 IllegalArgumentException | 正确 |

---

# 六、集成测试

**测试方式：** `@WebMvcTest` 切片，覆盖全量 Controller，`@MockBean` 隔离 Service 层
**设计技术总览：** 场景法、等价类划分、错误推测（安全漏洞验证）

## 登录流程

**测试对象**：`UserController.java:login(String userID, String password, HttpServletRequest)`

**测试函数**：`IntegrationFlowTest:testLogin_success()、testLogin_wrongPassword()`

**设计技术**：`场景法 + 等价类划分`

| 功能点 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-------|---------|---------|---------|---------|------|
| 普通用户登录 | IT-LG-001 | 合法用户提交正确用户名与密码 | 响应体为 `/index`，Session 写入 `user` | 响应体为 `/index`，Session 成功写入 `user` | 正确 |
| 密码错误 | IT-LG-002 | 用户名存在但密码错误 | 响应体为 `false` | 响应体为 `false` | 正确 |

---

## 订单流程

**测试对象**：`OrderController.java:order_manage(Model, HttpServletRequest), addOrder(String, String, String, int, HttpServletRequest, HttpServletResponse)`

**测试函数**：`IntegrationFlowTest:testOrderManage_noLogin()、testAddOrder_success()、testDelOrder_noLogin_vulnerability()`

**设计技术**：`场景法`

| 功能点 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-------|---------|---------|---------|---------|------|
| 未登录访问订单管理 | IT-OR-001 | 未登录直接访问 `/order_manage` | 抛出 `LoginException`，请求被拒绝 | 返回 500，抛出 `LoginException` | 正确 |
| 登录后提交订单 | IT-OR-002 | 已登录用户提交合法订单参数 | 跳转到 `order_manage`，并调用提交逻辑 | 响应重定向到 `order_manage`，`orderService.submit` 被调用 1 次 | 正确 |
| 未登录删除订单 | IT-OR-003 | 未登录直接调用 `/delOrder.do` 删除订单 | 请求应被拒绝（登录校验或权限校验失败） | 返回 `true`，`orderService.delOrder(1)` 被调用 | 错误 |

---

## 场馆流程

**测试对象**：`VenueController.java:venue_list(int), toGymPage(Model, int)`

**测试函数**：`IntegrationFlowTest:testVenueList_success()、testVenueDetail_success()`

**设计技术**：`场景法`

| 功能点 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-------|---------|---------|---------|---------|------|
| 分页获取场馆列表 | IT-VN-001 | 请求第一页场馆数据 | 返回分页 JSON，`content` 非空且包含场馆名 | 返回分页 JSON，`content[0].venueName=羽毛球馆A` | 正确 |
| 场馆详情页渲染 | IT-VN-002 | 通过 `venueID` 访问场馆详情页 | 返回详情页面并包含对应场馆数据 | 返回 200，页面包含 `羽毛球馆A` | 正确 |

---

## 留言流程

**测试对象**：`MessageController.java:sendMessage(String, String, HttpServletResponse), user_message_list(int, HttpServletRequest)`

**测试函数**：`IntegrationFlowTest:testSendMessage_success()、testFindUserMessage_noLogin()、testSendMessage_noLogin_vulnerability()、testSendMessage_forgedUserId_vulnerability()、testDelMessage_noLogin_vulnerability()`

**设计技术**：`场景法 + 错误推测`

| 功能点 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-------|---------|---------|---------|---------|------|
| 提交留言 | IT-MG-001 | 用户提交留言内容 | 重定向到 `/message_list`，留言默认状态为未审核 | 响应重定向到 `/message_list`，`message.state=1` | 正确 |
| 未登录查询个人留言 | IT-MG-002 | 未登录访问 `/message/findUserList` | 抛出 `LoginException`，请求被拒绝 | 返回 500，抛出 `LoginException` | 正确 |
| 未登录提交留言 | IT-MG-003 | 未登录直接调用 `/sendMessage` 提交留言 | 请求应被拒绝（登录校验失败） | 响应重定向 `/message_list`，留言成功入库（`userID=u9999`） | 错误 |
| 伪造用户提交留言 | IT-MG-004 | 登录用户A在请求参数中提交用户B的 `userID` | 应使用会话用户身份，禁止伪造他人身份提交 | 留言按请求参数保存为 `userID=u2002`，未使用会话用户 | 错误 |
| 未登录删除留言 | IT-MG-005 | 未登录直接调用 `/delMessage.do` 删除留言 | 请求应被拒绝（登录校验或权限校验失败） | 返回 `true`，`messageService.delById(11)` 被调用 | 错误 |

---

## 黑盒自查（缺陷暴露 + @Disabled 占位）

> IT-INT-024~026（原 IT-BB-01~03）当前运行并 failing，暴露已知服务层缺陷（BUG-028/029/030）。IT-INT-027~029（原 IT-BB-04~06）保持 `@Disabled`，待对应功能修复后启用。

**测试函数**：`IntegrationFlowTest:testAddOrder_nonPositiveHours()、testPassOrder_illegalState()、testRejectOrder_illegalState()、testPassOrRejectMessage_illegalState()、testSendMessage_emptyContent()、testAddNews_emptyTitleOrContent()`

**设计技术**：`黑盒等价类划分`

| 功能点 | 用例编号 | 用例描述 | 预期结果 | 当前状态 | 结论 |
|-------|---------|---------|---------|---------|------|
| 订单提交参数校验 | IT-INT-024 | `/addOrder.do` 传入 `hours=0` | 抛出业务异常，且不应调用 `orderService.submit` | 失败（缺陷暴露，BUG-028） | 错误 |
| 订单状态机非法流转 | IT-INT-025 | `/passOrder.do` 对非法前置状态订单执行通过 | 抛出业务异常，且不应调用 `orderService.confirmOrder` | 失败（缺陷暴露，BUG-029） | 错误 |
| 订单状态机非法流转 | IT-INT-026 | `/rejectOrder.do` 对非法前置状态订单执行驳回 | 抛出业务异常，且不应调用 `orderService.rejectOrder` | 失败（缺陷暴露，BUG-030） | 错误 |
| 留言状态机非法流转 | IT-INT-027 | `/passMessage.do` / `/rejectMessage.do` 对已处理留言再次流转 | 抛出业务异常，且不应调用对应 service 方法 | 跳过（@Disabled，message 分支未合并） | 待测 |
| 留言输入校验 | IT-INT-028 | `/sendMessage` 传入空 `content` | 抛出业务异常，且不应调用 `messageService.create` | 跳过（@Disabled，message 分支未合并） | 待测 |
| 新闻输入校验 | IT-INT-029 | `/addNews.do` 传入空 `title` 或空 `content` | 抛出业务异常，且不应调用 `newsService.create` | 跳过（@Disabled，等待服务层实现后启用） | 待测 |
