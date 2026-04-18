# 测试用例设计文档 — UserServiceImpl

## 文档信息

| 字段      | 值                       |
|----------|-------------------------|
| 模块名称  | `UserServiceImpl`       |
| 负责成员  | 印伟辰                     |
| 用例编号段 | `UT-US-001 ~ UT-US-027` |
| 创建日期  | 2026-04-18              |
| 最后更新  | 2026-04-18              |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-04-18 | | 初稿创建，按等价类划分补全全部用例 |

---

## findByUserID(String)

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:findByUserID(String userID)`
**测试函数**：`src.test.java.com.demo.service.UserServiceTest:testFindByUserID_EC1_found()`

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
**测试函数**：`src.test.java.com.demo.service.UserServiceTest:testFindById_EC5_found()`

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
**测试函数**：`src.test.java.com.demo.service.UserServiceTest:testFindByUserID_pageable_EC9_hasUsers()`

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
**测试函数**：`src.test.java.com.demo.service.UserServiceTest:testCheckLogin_EC11_success()`

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
| UT-US-015 | EC15 | userID 为 null 时返回 null | `null`, `"123456"` | 返回 null | 通过 | 正确 |
| UT-US-016 | EC16 | password 为 null 时返回 null | `"user001"`, `null` | 返回 null | 通过 | 正确 |

---

## create

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:create(User user)`
**测试函数**：`src.test.java.com.demo.service.UserServiceTest:testCreate_EC17_multipleUsers()`

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

---

## delByID

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:delByID(int id)`
**测试函数**：`src.test.java.com.demo.service.UserServiceTest:testDelByID_EC20_success()`

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
**测试函数**：`src.test.java.com.demo.service.UserServiceTest:testUpdateUser_EC23_valid()`

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
**测试函数**：`src.test.java.com.demo.service.UserServiceTest:testCountUserID_EC25_exists()`

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