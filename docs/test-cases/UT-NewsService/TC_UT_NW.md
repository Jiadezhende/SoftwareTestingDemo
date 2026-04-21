# 测试用例设计文档 — NewsServiceImpl

## 文档信息

| 字段      | 值 |
|----------|----|
| 模块名称  | `NewsServiceImpl` |
| 负责成员  | 成员C |
| 用例编号段 | `UT-NW-001 ~ UT-NW-099` |
| 创建日期  | 2026-04-17 |
| 最后更新  | 2026-04-21 |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-04-17 | 成员C | 完成 NewsServiceImpl 单元测试设计与执行 |
| v1.1 | 2026-04-18 | 成员C | 更新 create 用例描述，明确校验返回值来自持久化后的对象 |
| v1.2 | 2026-04-21 | 成员C | 按等价类、边界值、异常路径补充 `findAll`、`findById`、`create`、`delById`、`update` 用例 |

---

## findAll

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:findAll(Pageable pageable)`
**测试函数**：`testFindAll()`、`testFindAllBoundaryWithEmptyPage()`、`testFindAllException()`、`testFindAllExceptionWithNullPageable()`

### 用例设计（等价类/边界值/决策表）

**等价类划分（Pageable）**

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `pageable != null` | 返回 `newsDao.findAll(pageable)` 的结果 |
| EC2 | `pageable == null` | 异常向上透传（由 DAO 或下层抛出） |

**边界值分析（分页）**

| 参数 | 边界值 | 覆盖用例 |
|------|--------|----------|
| `pageSize` | `1` | `UT-NW-006`（空分页） |
| `pageNumber` | `0`（第一页） | `UT-NW-001` / `UT-NW-006` |

**决策表（核心规则）**

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

## findById

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:findById(int newsID)`
**测试函数**：`testFindById()`、`testFindByIdBoundaryWithZeroId()`、`testFindByIdException()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-002 | 查询存在的新闻 ID | 调用 `newsDao.getOne(newsID)` 并返回对应 News 对象 | 返回 DAO 模拟的 News 对象 | 正确 |
| UT-NW-008 | 边界值：查询 `newsID=0` 的新闻 | 调用 `newsDao.getOne(0)` 并返回对应对象 | 返回 DAO 模拟的 `newsID=0` 新闻对象 | 正确 |
| UT-NW-009 | 异常路径：DAO 查询单条新闻抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

## create

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:create(News news)`
**测试函数**：`testCreate()`、`testCreateBoundaryWithZeroId()`、`testCreateException()`、`testCreateBoundaryWithNullSavedEntity()`、`testCreate_EmptyTitle_ShouldBeRejected()`、`testCreate_EmptyContent_ShouldBeRejected()`

### 用例设计（等价类/边界值/决策表）

**等价类划分（入参 News）**

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `news != null` 且 `title`、`content` 均为非空字符串 | 调用 `newsDao.save(news)` 并返回 `save` 结果的 `newsID` |
| EC2 | `news.title == ""` | 抛出业务异常，且不调用 `newsDao.save(news)` |
| EC3 | `news.content == ""` | 抛出业务异常，且不调用 `newsDao.save(news)` |
| EC4 | `news == null` | 异常向上透传（由 DAO 或下层抛出） |

**边界值分析（返回值来源）**

| 关注点 | 边界/特殊值 | 覆盖用例 |
|--------|------------|----------|
| `save(news)` 返回对象 | `null` | `UT-NW-017` |
| 返回 `newsID` | `0` | `UT-NW-010` |
| `title` | `""` | `UT-NW-019` |
| `content` | `""` | `UT-NW-020` |

**决策表（核心规则）**

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
| UT-NW-003 | 新增合法新闻对象，入参 `newsID=0` | 调用 `newsDao.save(news)`，返回持久化后对象的 `newsID`，而不是入参对象的 ID | 入参对象 `newsID=0`，DAO 返回对象 `newsID=3`，方法最终返回 `3` | 正确 |
| UT-NW-010 | 边界值：新增新闻后持久化对象 `newsID=0` | 调用 `newsDao.save(news)`，返回持久化后对象的 `newsID=0` | 方法最终返回 `0`，证明 service 未额外加工返回值 | 正确 |
| UT-NW-011 | 异常路径：DAO 保存新闻抛出异常 | 异常向上透传，不返回默认值 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-NW-017 | 边界值：DAO `save` 返回 `null` | 调用 `newsDao.save(news)` 后获取 `newsID` 触发空指针异常 | 捕获到 `NullPointerException` | 正确 |
| UT-NW-019 | 缺陷占位：`title` 为空的新闻不应被创建 | 应抛出业务异常，且不调用 `newsDao.save(news)` | 已补充 `@Disabled` 占位测试 `testCreate_EmptyTitle_ShouldBeRejected()`，当前实现未校验该场景 | 待测 |
| UT-NW-020 | 缺陷占位：`content` 为空的新闻不应被创建 | 应抛出业务异常，且不调用 `newsDao.save(news)` | 已补充 `@Disabled` 占位测试 `testCreate_EmptyContent_ShouldBeRejected()`，当前实现未校验该场景 | 待测 |

## delById

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:delById(int newsID)`
**测试函数**：`testDelById()`、`testDelByIdBoundaryWithZeroId()`、`testDelByIdException()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-004 | 删除指定新闻 ID | 调用 `newsDao.deleteById(newsID)` 删除对应记录 | 校验到 `deleteById(4)` 被调用 1 次 | 正确 |
| UT-NW-012 | 边界值：删除 `newsID=0` 的新闻 | 调用 `newsDao.deleteById(0)` | 校验到 `deleteById(0)` 被正确调用 | 正确 |
| UT-NW-013 | 异常路径：DAO 删除新闻抛出异常 | 异常向上透传，不做吞并或忽略 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

## update

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:update(News news)`
**测试函数**：`testUpdate()`、`testUpdateBoundaryWithZeroId()`、`testUpdateException()`、`testUpdateBoundaryWithNullNews()`

### 用例设计（等价类/边界值/决策表）

**等价类划分（入参 News）**

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `news != null` | 调用 `newsDao.save(news)` 并完成更新请求转发 |
| EC2 | `news == null` | 异常向上透传（由 DAO 或下层抛出） |

**边界值分析（newsID）**

| 参数 | 边界值 | 覆盖用例 |
|------|--------|----------|
| `news.newsID` | `0` | `UT-NW-014` |
| `news` | `null` | `UT-NW-018` |

**决策表（核心规则）**

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
