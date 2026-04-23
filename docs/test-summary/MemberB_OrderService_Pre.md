# 成员B预答辩说明 - OrderServiceImpl

## 1. 负责范围

- 负责模块：`OrderServiceImpl`
- 用例编号段：`UT-OR-001 ~ UT-OR-048`
- 相关代码：
  - `src/main/java/com/demo/service/impl/OrderServiceImpl.java`
  - `src/test/java/com/demo/service/impl/OrderServiceImplTest.java`
  - `docs/test-cases/UT-OrderService/TC_UT_OR.md`
  - `docs/defects/DefectLog.md`

---

## 2. 模块功能概述

`OrderServiceImpl` 负责订单相关的核心业务处理，主要包括：

- 根据订单 ID 查询订单
- 根据场馆和时间区间查询订单
- 根据用户查询分页订单
- 提交新订单 `submit`
- 修改已有订单 `updateOrder`
- 删除订单 `delOrder`
- 审核通过订单 `confirmOrder`
- 完成订单 `finishOrder`
- 拒绝订单 `rejectOrder`
- 查询未审核订单和已审核订单

该模块同时涉及输入合法性校验、金额计算、订单状态流转和 DAO 调用，因此适合同时使用黑盒和白盒测试。

---

## 3. 测试设计思路

### 3.1 黑盒测试

黑盒测试采用：

- 等价类划分
- 边界值分析

执行原则：

- 有效等价类采用最小组合，每个有效代表值至少在一个测试用例中出现
- 无效等价类按代表值逐一拆分，每个无效代表值对应一个独立测试用例
- 无效代表值只与其他输入项的有效代表值组合，不把多个无效值塞进同一个用例

### 3.2 白盒测试

白盒测试重点覆盖：

- `confirmOrder`
- `finishOrder`
- `rejectOrder`

体现的技术包括：

- 语句覆盖
- 判定覆盖

并补充了状态机非法前置状态的分支覆盖。

### 3.3 DAO 异常透传验证

当前 `OrderServiceImpl` 不捕获 DAO 异常，而是直接透传。因此补充了 DAO 异常透传测试，验证：

- Service 向上抛出同一个异常对象
- 目标 DAO 方法确实被调用
- 多步 DAO 场景中，中途失败后后续 DAO 不再执行

---

## 4. 实际测试过程

本次测试分为两个阶段。

### 第一阶段：建立可运行的单元测试框架

使用 JUnit 5 + Mockito 为 `OrderServiceImpl` 建立纯单元测试，不依赖真实数据库。

具体做法：

- 对 `OrderDao` 和 `VenueDao` 使用 `@Mock`
- 对 `OrderServiceImpl` 使用 `@InjectMocks`
- 用 `when(...).thenReturn(...)` 模拟 DAO 返回值
- 用 `verify(...)` 验证 DAO 是否按预期被调用

### 第二阶段：按需求断言缺陷

后续根据业务规则、自查表和等价类组合原则，将已知问题全部改为按需求断言的失败测试。

也就是说：

- 需求要求“应拒绝”的输入
- 当前代码没有拒绝
- 则测试直接失败，并在文档中记为 `错误`

---

## 5. 已执行测试与结果

当前 `OrderServiceImplTest` 共设计并执行 48 条测试。

结果如下：

- 通过：31 条
- 失败：17 条
- 跳过：0 条

这 17 条失败用例中，既包含关键边界值失败，也包含无效等价类失败；归并后共对应 15 类问题。

---

## 6. 主要问题

### 6.1 输入校验问题

1. `submit` 未拦截 `hours<=0`
2. `submit` 对不存在的场馆名未做业务校验
3. `submit` 未校验 `startTime=null`
4. `submit` 未校验 `userID=null/空串`
5. `updateOrder` 对不存在的订单未做业务校验
6. `updateOrder` 未拦截 `hours<=0`
7. `updateOrder` 对不存在的场馆名未做业务校验
8. `delOrder` 未校验 `orderID=0/负数`
9. `findDateOrder` 未校验非法时间区间
10. `findById` 未校验 `orderID=0/负数`
11. `findUserOrder` 未校验 `userID=null/空串`
12. `findNoAuditOrder` 未校验 `pageable=null`

### 6.2 状态机问题

13. `confirmOrder` 未校验 `STATE_WAIT / STATE_FINISH / STATE_REJECT`
14. `finishOrder` 未校验 `STATE_NO_AUDIT / STATE_REJECT`
15. `rejectOrder` 未校验 `STATE_WAIT / STATE_FINISH`

---

## 7. 结果说明

需要说明的是：

- 测试失败不表示测试代码写错
- 而是表示测试代码按需求断言后，真实暴露了被测代码中的缺陷

另外，DAO 异常透传相关测试均通过，说明当前实现确实采用了异常原样向上抛出的设计；如果项目后续要求统一包装为 `ServiceException`，则这一部分测试预期应调整。

---

## 8. 最终质量评估

### 8.1 优点

- 基础查询、删除、正常订单创建与修改路径能够运行
- 正常输入下金额计算逻辑基本正确
- `confirmOrder / finishOrder / rejectOrder` 对“订单不存在”场景已有基础异常处理

### 8.2 主要风险

- 输入合法性校验整体不足
- 空值防护不完整，导致 `NullPointerException`
- 状态机约束不完整
- 服务层异常处理风格不统一

### 8.3 结论

`OrderServiceImpl` 当前质量评价为：

- 常规路径可运行
- 测试覆盖已较完整
- 关键业务约束仍存在明显缺陷

因此该模块不适合作为高质量可发布版本，需要先补齐输入校验和状态流转校验，再重新执行全部测试。

---

## 9. 个人总结

这次测试工作的核心结论有两点：

- 测试通过不等于系统没有缺陷，关键在于断言依据是“当前行为”还是“需求预期”
- 等价类测试不能只写“覆盖过就行”，还要遵守组合原则，尤其是无效代表值必须独立成例

本次工作最终形成了可执行测试代码、测试用例文档和缺陷记录，能够较真实地反映 `OrderServiceImpl` 的实际质量状况。
