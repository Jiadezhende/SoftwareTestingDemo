# 测试用例设计文档 — UserServiceImpl

## 文档信息

| 字段      | 值 |
|----------|----|
| 模块名称  | `UserServiceImpl` |
| 负责成员  |  |
| 用例编号段 | `UT-US-001 ~ UT-US-099` |
| 创建日期  |  |
| 最后更新  |  |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 |      |        | 初稿创建 |

---

## checkLogin

**测试对象**：`src.main.java.com.demo.service.impl.UserServiceImpl.java:checkLogin(String userID, String password)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| 0 | 用户存在但密码错误 | 不通过 | — | 待测 |
