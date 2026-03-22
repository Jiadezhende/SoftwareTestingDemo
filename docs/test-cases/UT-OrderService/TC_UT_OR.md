# 测试用例设计文档 — OrderServiceImpl

## 文档信息

| 字段      | 值 |
|----------|----|
| 模块名称  | `OrderServiceImpl` |
| 负责成员  |  |
| 用例编号段 | `UT-OR-001 ~ UT-OR-099` |
| 创建日期  |  |
| 最后更新  |  |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 |      |        | 初稿创建 |

---

## confirmOrder

**测试对象**：`src.main.java.com.demo.service.impl.OrderServiceImpl.java:confirmOrder(int orderID)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| 0 | 订单存在时确认 | 状态变为 STATE_WAIT | — | 待测 |
