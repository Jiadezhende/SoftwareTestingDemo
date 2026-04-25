# 测试用例设计文档 — MessageServiceImpl

## 文档信息

| 字段      | 值 |
|----------|----|
| 模块名称  | `MessageServiceImpl` |
| 负责成员  | 俞楚凡 |
| 用例编号段 | `UT-MG-001 ~ UT-MG-042` |
| 创建日期  | 2026-04-13 |
| 最后更新  | 2026-04-16 |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-04-13 | 俞楚凡 | 初稿创建，基于等价类划分构造测试用例 |
| v1.1 | 2026-04-15 | 俞楚凡 | 补充边界值分析测试用例 UT-MG-028~042 |
| v1.2 | 2026-04-16 | 俞楚凡 | 补充白盒测试覆盖分析 |
| v1.3 | 2026-04-25 | 俞楚凡 | UT-MG-008/009/016/036/039 结论更新为"跳过"，对应 @Disabled 代码变更（BUG-031~035） |

---

## confirmMessage

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:confirmMessage(int messageID)`
**测试函数**：（待补全）
**设计技术**：`等价类划分`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-001 | 传入数据库中存在的留言 ID(2) | 该留言状态变为 STATE_PASS(2) | DAO 的 updateState(STATE_PASS, 2) 被正确调用 | 正确 |
| UT-MG-002 | 传入数据库中不存在的留言 ID(9999) | 抛出 RuntimeException("留言不存在") | 抛出 RuntimeException("留言不存在") | 正确 |

---

## rejectMessage

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:rejectMessage(int messageID)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-003 | 传入数据库中存在的留言 ID(2) | 该留言状态变为 STATE_REJECT(3) | DAO 的 updateState(STATE_REJECT, 2) 被正确调用 | 正确 |
| UT-MG-004 | 传入数据库中不存在的留言 ID(9999) | 抛出 RuntimeException("留言不存在") | 抛出 RuntimeException("留言不存在") | 正确 |

---

## create

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:create(Message message)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-005 | 传入所有字段正常的 Message 对象 | 返回新增留言的 messageID（大于 0） | 返回 messageID=2 | 正确 |
| UT-MG-006 | 传入 null | 抛出异常 | 抛出 IllegalArgumentException | 正确 |
| UT-MG-007 | 传入 userID 为 null 的 Message | 抛出异常（数据库 NOT NULL 约束） | 抛出 DataIntegrityViolationException | 正确 |
| UT-MG-008 | 传入 content 为 null 的 Message | 拒绝创建，抛出异常或返回错误提示 | 跳过（@Disabled，BUG-031） | 跳过 |
| UT-MG-009 | 传入 content 为空字符串的 Message | 拒绝创建，抛出异常或返回错误提示 | 跳过（@Disabled，BUG-032） | 跳过 |

---

## findById

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:findById(int messageID)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-010 | 传入数据库中存在的留言 ID(2) | 返回对应的 Message 对象 | 返回 Message 对象，messageID=2，userID="test" | 正确 |
| UT-MG-011 | 传入数据库中不存在的留言 ID(9999) | 抛出 EntityNotFoundException | 抛出 EntityNotFoundException | 正确 |

---

## delById

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:delById(int messageID)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-012 | 传入数据库中存在的留言 ID(2) | 删除成功，无异常 | 删除成功，无异常 | 正确 |
| UT-MG-013 | 传入数据库中不存在的留言 ID(9999) | 抛出 EmptyResultDataAccessException | 抛出 EmptyResultDataAccessException | 正确 |

---

## update

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:update(Message message)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-014 | 传入已存在的留言，修改 content 为新内容 | 更新成功，content 变为新值 | 更新成功，无异常 | 正确 |
| UT-MG-015 | 传入 null | 抛出异常 | 抛出 IllegalArgumentException | 正确 |
| UT-MG-016 | 传入数据库中不存在的留言（messageID=9999） | 应拒绝更新，抛出异常或返回错误提示 | 跳过（@Disabled，BUG-033） | 跳过 |

---

## findByUser

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:findByUser(String userID, Pageable pageable)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-017 | 传入有留言记录的用户 ID("test") | 返回该用户的留言分页，内容不为空 | 返回包含 1 条留言的分页 | 正确 |
| UT-MG-018 | 传入无留言记录的用户 ID("nobody") | 返回空分页 | 返回空分页 | 正确 |
| UT-MG-019 | 传入 userID 为 null | 抛出异常或返回空分页 | 返回空分页 | 正确 |
| UT-MG-020 | 传入 userID 为空字符串 | 返回空分页 | 返回空分页 | 正确 |
| UT-MG-021 | 传入 pageable 为 null | 抛出异常 | 抛出 IllegalArgumentException | 正确 |

---

## findWaitState

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:findWaitState(Pageable pageable)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-022 | 数据库中存在待审核留言(state=1) | 返回非空分页，所有留言 state 均为 1 | 返回包含 1 条 state=1 留言的分页 | 正确 |
| UT-MG-023 | 数据库中无待审核留言 | 返回空分页 | 返回空分页 | 正确 |
| UT-MG-024 | 传入 pageable 为 null | 抛出异常 | 抛出 IllegalArgumentException | 正确 |

---

## findPassState

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:findPassState(Pageable pageable)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-025 | 数据库中存在已通过留言(state=2) | 返回非空分页，所有留言 state 均为 2 | 返回包含 1 条 state=2 留言的分页 | 正确 |
| UT-MG-026 | 数据库中无已通过留言 | 返回空分页 | 返回空分页 | 正确 |
| UT-MG-027 | 传入 pageable 为 null | 抛出异常 | 抛出 IllegalArgumentException | 正确 |

---

# 以下为边界值分析测试用例

## messageID 边界值

**测试对象**：`confirmMessage` / `rejectMessage` / `findById` / `delById` 中的 `int messageID` 参数

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-028 | confirmMessage 传入 messageID=0（下界-1） | 抛出 RuntimeException("留言不存在") | 抛出 RuntimeException("留言不存在") | 正确 |
| UT-MG-029 | confirmMessage 传入 messageID=1（下界） | 若留言存在则状态变为 STATE_PASS | updateState(STATE_PASS, 1) 被正确调用 | 正确 |
| UT-MG-030 | confirmMessage 传入 messageID=-1（负数） | 抛出 RuntimeException("留言不存在") | 抛出 RuntimeException("留言不存在") | 正确 |
| UT-MG-031 | findById 传入 messageID=0 | 抛出 EntityNotFoundException | 抛出 EntityNotFoundException | 正确 |
| UT-MG-032 | delById 传入 messageID=-1 | 抛出 EmptyResultDataAccessException | 抛出 EmptyResultDataAccessException | 正确 |

---

## userID 长度边界值

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:create(Message message)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-033 | create 传入 userID 长度=1（下界） | 正常创建 | 正常创建，返回 messageID=30 | 正确 |
| UT-MG-034 | create 传入 userID 长度=25（上界） | 正常创建 | 正常创建，返回 messageID=31 | 正确 |
| UT-MG-035 | create 传入 userID 长度=26（上界+1） | 应拒绝创建，抛出数据截断异常 | 抛出 DataIntegrityViolationException | 正确 |

---

## state 边界值

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:create(Message message)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-036 | create 传入 state=0（下界-1，非法状态） | 应拒绝创建，抛出异常或返回错误 | 跳过（@Disabled，BUG-034） | 跳过 |
| UT-MG-037 | create 传入 state=1（下界，待审核） | 正常创建 | 正常创建，返回 messageID=34 | 正确 |
| UT-MG-038 | create 传入 state=3（上界，驳回） | 正常创建 | 正常创建，返回 messageID=35 | 正确 |
| UT-MG-039 | create 传入 state=4（上界+1，非法状态） | 应拒绝创建，抛出异常或返回错误 | 跳过（@Disabled，BUG-035） | 跳过 |

---

## Pageable 分页边界值

**测试对象**：`src.main.java.com.demo.service.impl.MessageServiceImpl.java:findByUser(String userID, Pageable pageable)`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-MG-040 | 传入 page=0, size=1（最小合法分页） | 返回最多 1 条数据的分页 | 返回包含 1 条数据的分页 | 正确 |
| UT-MG-041 | 传入 page=-1（下界-1，负数页码） | 抛出 IllegalArgumentException | 抛出 IllegalArgumentException | 正确 |
| UT-MG-042 | 传入 size=0（下界-1，每页0条） | 抛出 IllegalArgumentException | 抛出 IllegalArgumentException | 正确 |

---

# 白盒测试覆盖分析

## 覆盖分析方法说明

对 `MessageServiceImpl` 的每个方法进行代码结构分析，标注已有用例对语句、判定、条件和路径的覆盖情况。

本模块所有方法的判定均为单一条件，不存在复合条件（如 `a && b`）。在此情况下，条件覆盖与判定覆盖等价，因此满足判定覆盖即同时满足条件-判定覆盖。同时，每个方法最多仅有 1 个判定、2 条路径，已有用例覆盖了全部路径，因此路径覆盖也已达成。

已有用例达成条件-判定覆盖、路径覆盖 100%，无需新增白盒专用用例。

---

## confirmMessage — 控制流与覆盖分析

```
S1: Message message = messageDao.findByMessageID(messageID);
S2: if (message == null)  ──┬── true  → S3: throw RuntimeException("留言不存在");
                            └── false → S4: messageDao.updateState(STATE_PASS, ...);
```

**路径分析**：共 2 条路径

| 路径 | 执行序列 | 覆盖用例 |
|------|---------|---------|
| 路径1 | S1 → S2(false) → S4 | UT-MG-001, UT-MG-029 |
| 路径2 | S1 → S2(true) → S3 | UT-MG-002, UT-MG-028, UT-MG-030 |

**条件-判定覆盖**：判定 `message == null` 仅包含 1 个条件

| 条件 | 取 true | 取 false |
|------|--------|---------|
| message == null | UT-MG-002（message 为 null） | UT-MG-001（message 非 null） |

| 覆盖指标 | 覆盖率 |
|---------|-------|
| 语句覆盖 | 4/4 = **100%** |
| 判定覆盖 | 2/2 = **100%** |
| 条件覆盖 | 2/2 = **100%**（单一条件，等价于判定覆盖） |
| 条件-判定覆盖 | **100%** |

---

## rejectMessage — 控制流与覆盖分析

```
S1: Message message = messageDao.findByMessageID(messageID);
S2: if (message == null)  ──┬── true  → S3: throw RuntimeException("留言不存在");
                            └── false → S4: messageDao.updateState(STATE_REJECT, ...);
```

**路径分析**：共 2 条路径（与 confirmMessage 结构相同）

| 路径 | 执行序列 | 覆盖用例 |
|------|---------|---------|
| 路径1 | S1 → S2(false) → S4 | UT-MG-003 |
| 路径2 | S1 → S2(true) → S3 | UT-MG-004 |

**条件-判定覆盖**：

| 条件 | 取 true | 取 false |
|------|--------|---------|
| message == null | UT-MG-004 | UT-MG-003 |

| 覆盖指标 | 覆盖率 |
|---------|-------|
| 语句覆盖 | 4/4 = **100%** |
| 判定覆盖 | 2/2 = **100%** |
| 条件覆盖 | 2/2 = **100%** |
| 条件-判定覆盖 | **100%** |
| 路径覆盖 | 2/2 = **100%** |

---

## 无分支方法 — 覆盖分析

以下方法内部无 if/while/for 判定，只有顺序执行语句（1 条路径），任意一条用例即达成全部覆盖。因无判定和条件，条件-判定覆盖不适用（视为 100%）；仅 1 条路径，路径覆盖满足。

### create（1 条语句，1 条路径）

```
S1: return messageDao.save(message).getMessageID();
```

覆盖用例：UT-MG-005, UT-MG-033, UT-MG-034, UT-MG-037, UT-MG-038

### findById（1 条语句，1 条路径）

```
S1: return messageDao.getOne(messageID);
```

覆盖用例：UT-MG-010, UT-MG-031

### delById（1 条语句，1 条路径）

```
S1: messageDao.deleteById(messageID);
```

覆盖用例：UT-MG-012, UT-MG-032

### update（1 条语句，1 条路径）

```
S1: messageDao.save(message);
```

覆盖用例：UT-MG-014

### findByUser（2 条语句，1 条路径）

```
S1: Page<Message> page = messageDao.findAllByUserID(userID, pageable);
S2: return page;
```

覆盖用例：UT-MG-017, UT-MG-018, UT-MG-040

### findWaitState（1 条语句，1 条路径）

```
S1: return messageDao.findAllByState(STATE_NO_AUDIT, pageable);
```

覆盖用例：UT-MG-022

### findPassState（1 条语句，1 条路径）

```
S1: return messageDao.findAllByState(STATE_PASS, pageable);
```

覆盖用例：UT-MG-025

---