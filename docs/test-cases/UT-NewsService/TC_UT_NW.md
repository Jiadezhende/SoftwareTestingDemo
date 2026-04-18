# 测试用例设计文档 — NewsServiceImpl

## 文档信息

| 字段      | 值 |
|----------|----|
| 模块名称  | `NewsServiceImpl` |
| 负责成员  | 成员C |
| 用例编号段 | `UT-NW-001 ~ UT-NW-099` |
| 创建日期  | 2026-04-17 |
| 最后更新  | 2026-04-18 |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-04-17 | 成员C | 完成 NewsServiceImpl 单元测试设计与执行 |
| v1.1 | 2026-04-18 | 成员C | 更新 create 用例描述，明确校验返回值来自持久化后的对象 |

---

## findAll

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:findAll(Pageable pageable)`
**测试函数**：`src.test.java.com.demo.service.impl.NewsServiceImplTest:testFindAll()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-001 | 传入分页参数查询新闻列表 | 调用 `newsDao.findAll(pageable)` 并返回分页结果 | 返回 DAO 模拟的分页对象，记录数正确 | 正确 |

## findById

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:findById(int newsID)`
**测试函数**：`src.test.java.com.demo.service.impl.NewsServiceImplTest:testFindById()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-002 | 查询存在的新闻 ID | 调用 `newsDao.getOne(newsID)` 并返回对应 News 对象 | 返回 DAO 模拟的 News 对象 | 正确 |

## create

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:create(News news)`
**测试函数**：`src.test.java.com.demo.service.impl.NewsServiceImplTest:testCreate()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-003 | 新增合法新闻对象，入参 `newsID=0` | 调用 `newsDao.save(news)`，返回持久化后对象的 `newsID`，而不是入参对象的 ID | 入参对象 `newsID=0`，DAO 返回对象 `newsID=3`，方法最终返回 `3` | 正确 |

## delById

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:delById(int newsID)`
**测试函数**：`src.test.java.com.demo.service.impl.NewsServiceImplTest:testDelById()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-004 | 删除指定新闻 ID | 调用 `newsDao.deleteById(newsID)` 删除对应记录 | 校验到 `deleteById(4)` 被调用 1 次 | 正确 |

## update

**测试对象**：`src.main.java.com.demo.service.impl.NewsServiceImpl.java:update(News news)`
**测试函数**：`src.test.java.com.demo.service.impl.NewsServiceImplTest:testUpdate()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-NW-005 | 更新已有新闻对象 | 调用 `newsDao.save(news)` 完成更新 | 校验到 `save` 被调用 1 次且参数正确 | 正确 |
