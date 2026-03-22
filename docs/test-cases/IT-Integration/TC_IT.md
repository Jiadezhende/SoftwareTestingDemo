# 测试用例设计文档 — 集成测试

## 文档信息

| 字段      | 值 |
|----------|----|
| 测试类型  | 集成测试 |
| 负责成员  |  |
| 用例编号段 | `IT-LG/OR/VN/MG-001 ~ 099` |
| 创建日期  |  |
| 最后更新  |  |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 |      |        | 初稿创建 |

---

## 登录流程

**测试对象**：`src.main.java.com.demo.controller.user.UserController.java:login(String userID, String password, HttpServletRequest)`
**测试脚本**：`src.test.java.com.demo.integration.LoginFlowTest`

| 功能点列表 | 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|-----------|---------|---------|---------|---------|------|
| 普通用户登录 | IT-LG-001 | 合法用户提交正确用户名与密码 | 响应体为 `/index`，Session 写入 `user` | — | 待测 |
