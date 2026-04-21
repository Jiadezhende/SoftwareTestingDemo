# 黑盒测试自查手册

**用途**：各模块负责人对照速查表，将未实现的校验逻辑以 `@Disabled` 占位写入测试文件。

**CI 语义**：绿 = 正常；红 = 一定坏了；跳过 = 已知缺口待实现。`@Disabled` 数量即当前需求缺口数，不污染红绿信号。

**实现后**：删掉 `@Disabled`，将 `Exception.class` 换成具体异常类，测试应变绿。

---

## 写法示例

### 输入校验类缺陷

```java
@Test
@Disabled("未实现：空密码应被拒绝")
@DisplayName("EC28 - create: password 为空时应抛出异常")
void testCreate_EmptyPassword_ShouldBeRejected() {
    User user = buildUser("u001", "alice", "");
    assertThrows(Exception.class, () -> userService.create(user));
    verify(userDao, never()).save(any());
}
```

### 状态机非法流转类缺陷

```java
@Test
@Disabled("未实现：confirmOrder 应拒绝已通过(STATE_WAIT)的订单")
@DisplayName("confirmOrder: STATE_WAIT 时应抛出异常")
void testConfirmOrder_AlreadyWait_ShouldBeRejected() {
    Order order = buildOrder(1, OrderService.STATE_WAIT);
    when(orderDao.findByOrderID(1)).thenReturn(order);
    assertThrows(Exception.class, () -> orderService.confirmOrder(1));
}
```

---

## 各模块缺口速查表

实现后在"已实现"列打勾，并在 `docs/defects/DefectLog.md` 补充对应条目。

### UserService

| # | 方法 | 未校验的等价类 | 严重程度 | 已实现 |
|---|------|--------------|----------|--------|
| 1 | `create()` | password 为空 | 高 | [ ] |
| 2 | `create()` | userID 为 null | 高 | [ ] |
| 3 | `create()` | userID 重复（服务层未调用 countUserID） | 中 | [ ] |
| 4 | `checkLogin()` | userID 为 null（当前透传给 DAO） | 中 | [ ] |
| 5 | `checkLogin()` | password 为 null（当前透传给 DAO） | 中 | [ ] |

### OrderService

状态机合法路径：`STATE_NO_AUDIT(1)` → confirm → `STATE_WAIT(2)` → finish → `STATE_FINISH(3)`，或 → reject → `STATE_REJECT(4)`。以下操作均缺少前置状态校验：

| # | 方法 | 非法前置状态 | 严重程度 | 已实现 |
|---|------|------------|----------|--------|
| 6 | `confirmOrder()` | STATE_WAIT / STATE_FINISH / STATE_REJECT | 高 | [ ] |
| 7 | `finishOrder()` | STATE_NO_AUDIT / STATE_REJECT | 高 | [ ] |
| 8 | `rejectOrder()` | STATE_WAIT / STATE_FINISH | 高 | [ ] |
| 9 | `submit()` | 场馆不存在（当前为 NPE，非业务异常） | 高 | [ ] |
| 10 | `submit()` | hours ≤ 0 | 中 | [ ] |

### VenueService

| # | 方法 | 未校验的等价类 | 严重程度 | 已实现 |
|---|------|--------------|----------|--------|
| 11 | `create()` | price < 0 | 中 | [ ] |
| 12 | `create()` | venueName 为空 | 中 | [ ] |
| 13 | `create()` | venueName 重复（服务层未调用 countVenueName） | 中 | [ ] |

### MessageService

状态机：`STATE_NO_AUDIT(1)` → confirm → `STATE_PASS(2)` 或 → reject → `STATE_REJECT(3)`。已处理留言不应再次变更状态：

| # | 方法 | 非法前置状态 | 严重程度 | 已实现 |
|---|------|------------|----------|--------|
| 14 | `confirmMessage()` | STATE_PASS / STATE_REJECT | 高 | [ ] |
| 15 | `rejectMessage()` | STATE_PASS / STATE_REJECT | 高 | [ ] |
| 16 | `create()` | content 为空 | 中 | [ ] |

### NewsService

| # | 方法 | 未校验的等价类 | 严重程度 | 已实现 |
|---|------|--------------|----------|--------|
| 17 | `create()` | title 为空 | 低 | [ ] |
| 18 | `create()` | content 为空 | 低 | [ ] |
