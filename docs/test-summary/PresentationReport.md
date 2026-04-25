# SoftwareTestingDemo 测试汇报

## 项目背景

**被测系统：** 场馆预约系统，支持普通用户与管理员两类角色

**核心业务流程：**

```
用户注册/登录 → 浏览场馆 → 提交预约（待审核）
                              ↓
管理员审核 → 已审核 → 用户完成   /   驳回（失效）
用户发布留言（待审核）→ 管理员通过/拒绝
```

**总体结果：** 201 个测试用例 · 163 通过 · 22 失败 · 16 跳过 · 通过率 **81.1%** · 发现缺陷 **37 条**

**测试分工：**

| 模块 | 负责人 |
|---|---|
| UserServiceImpl | 印伟辰 |
| OrderServiceImpl | 顾祎炜 |
| VenueServiceImpl & NewsServiceImpl | 丘俊 |
| MessageServiceImpl | 俞楚凡 |
| 集成测试 | 高伟博 |

<div style="page-break-after: always;"></div>

## OrderServiceImpl 单元测试

**核心方法：** `submit`（提交预约）、`confirmOrder / finishOrder / rejectOrder`（状态流转）

**测试结果：** 48 个用例 · 31 通过 · 17 失败 · 0 跳过

**设计亮点 1 — `submit` 边界值 + ArgumentCaptor 验证**

```
hours=1（合法）  → 正常保存，total = price × hours = 120      通过
hours=0          → 应抛出异常，拒绝保存                         失败（BUG-002）
venueName 不存在 → 应抛出异常，拒绝保存                         失败（BUG-003）
startTime=null   → 应抛出异常，拒绝保存                         失败（BUG-009）
```

使用 `ArgumentCaptor` 捕获 `orderDao.save()` 的入参，逐字段断言：userID、venueID、total、startTime、orderTime、state 均正确。

**设计亮点 2 — 状态机测试**

订单生命周期：`待审核(1) → 已审核(2) → 已完成(3)` / `→ 失效(4)`

| 状态转换 | 前置状态 | 合法？ | 结论 |
|---|---|---|---|
| confirmOrder | 待审核(1) | 合法 | 通过 |
| confirmOrder | 已审核(2) | 非法 | 失败（BUG-005）|
| finishOrder | 待审核(1) | 非法 | 失败（BUG-006）|
| rejectOrder | 已审核(2) | 非法 | 失败（BUG-007）|

**缺陷总结：** 17 个失败用例集中在输入校验缺失（11 个）和状态机前置约束缺失（3 个）。

<div style="page-break-after: always;"></div>

## MessageServiceImpl 单元测试

**核心方法：** `create`、`update`、`confirmMessage / rejectMessage`、`findByUser` 等（9 个方法全覆盖）

**测试结果：** 42 个用例 · 37 通过 · 0 失败 · 5 跳过（@Disabled，BUG-031~035）

**设计亮点 — 多维边界值分析**

| 参数 | 边界值设计 | 典型用例 |
|---|---|---|
| messageID | 下界 0、有效下界 1、负数 -1 | `findById(0)` 应抛异常 |
| userID 长度 | 下界 1、上界 25、超界 26 | 超长 userID 应被拒绝 |
| state 合法性 | 有效值 1/2/3，非法值 0/4 | `create(state=0)` 应被拒绝 |
| Pageable | page=-1、size=0 | 非法分页参数应被拒绝 |

**白盒覆盖：** 语句覆盖 100% · 条件-判定覆盖 100% · 路径覆盖 100%

**发现缺陷（5 条）：**

| 缺陷 | 问题 | 严重程度 |
|---|---|---|
| BUG-031 | `create(content=null)` 未被拒绝 | 中 |
| BUG-032 | `create(content="")` 未被拒绝 | 中 |
| BUG-033 | `update` 对不存在留言被当作新增处理 | 高 |
| BUG-034/035 | `create(state=0/4)` 非法状态值未被拒绝 | 中 |

<div style="page-break-after: always;"></div>

## 集成测试

**方式：** `@WebMvcTest` 切片，覆盖全量 Controller，`@MockBean` 隔离 Service 层

**测试结果：** 29 个用例 · 23 通过 · 0 失败 · 6 跳过（@Disabled，关联 BUG 待修复）

**覆盖范围：**

| 场景 | 用例数 | 结论 |
|---|---|---|
| 登录流程（正确/错误凭据、Session 写入） | 2 | 全部通过 |
| 订单 & 场馆 & 留言 & 首页渲染 | 10 | 全部通过 |
| 管理员功能（订单/留言/新闻/用户/场馆接口） | 11 | 全部通过 |
| 黑盒自查（@Disabled，hours=0/非法状态/空内容） | 6 | 已标注 BUG，待修复 |

**设计亮点 — 安全漏洞验证（错误推测法）**

主动构造"应被拦截但实际未拦截"的场景，将漏洞用例化：

| 漏洞 | 构造方式 | 缺陷 |
|---|---|---|
| 未登录可删除订单 | POST `/delOrder.do`（无 Session）→ 成功执行 | BUG-017 |
| 未登录可提交留言 | POST `/sendMessage`（无 Session）→ 入库成功 | BUG-018 |
| 可伪造他人 userID | 登录为 u1001，提交 `userID=u2002` 的留言 → 成功入库 | BUG-019 |
| 未登录可删除留言 | POST `/delMessage.do`（无 Session）→ 成功执行 | BUG-020 |

<div style="page-break-after: always;"></div>

## 汇报总结

### 测试计划概览

以黑盒测试为主（等价类划分 + 边界值分析 + 状态机测试），辅以白盒路径覆盖，覆盖 5 个 Service 模块 + 全量 Controller，201 个测试用例，发现 37 条缺陷。

### 测试结果

| 总数 | 通过 | 失败 | 跳过 | 通过率 |
|---:|---:|---:|---:|---:|
| 201 | 163 | 22 | 16 | 81.1% |

缺陷分布：输入校验缺失 14 条 · 状态机约束缺失 5 条 · 鉴权不足 4 条 · 空对象处理不规范 6 条

### 遇到的难点及解决方法

**1. 被测系统过于简陋，缺少统一异常体系**

项目没有自定义异常类，服务层直接透传底层异常（NullPointerException、DAO 异常等）。测试策略上，将底层异常直接暴露视作错误行为；DAO 透传暂时认为是当前预期行为，不额外断言。

**2. 功能缺口与代码 Bug 的区分**

部分被测方法根本没有实现输入校验（如 `price < 0` 不拒绝），这是功能缺口，不是已有代码跑出了错误结果。对此类用例统一使用 `@Disabled`，而不是让测试红着，避免 CI 中混淆真正的回归错误。

**3. 多人协作测试规范不统一**

通过三项机制对齐：统一用例编号规范（`UT-{模块}-{序号}`）分配各人负责段；每人维护各自的测试用例设计文档；接入 GitHub Actions，每次 push 自动运行全量测试，及时暴露冲突。
