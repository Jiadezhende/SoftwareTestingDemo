# 测试用例设计文档 — VenueServiceImpl

## 文档信息

| 字段      | 值 |
|----------|----|
| 模块名称  | `VenueServiceImpl` |
| 负责成员  | 成员C |
| 用例编号段 | `UT-VN-001 ~ UT-VN-099` |
| 创建日期  | 2026-04-17 |
| 最后更新  | 2026-04-18 |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-04-17 | 成员C | 完成 VenueServiceImpl 单元测试设计与执行 |
| v1.1 | 2026-04-18 | 成员C | 更新 create 用例描述，明确校验返回值来自持久化后的对象 |

---

## findByVenueID

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findByVenueID(int id)`
**测试函数**：`src.test.java.com.demo.service.impl.VenueServiceImplTest:testFindByVenueID()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-001 | 传入存在的场馆 ID | 调用 `venueDao.getOne(id)` 并返回对应 Venue 对象 | 返回 DAO 模拟的 Venue 对象，字段保持一致 | 正确 |

## findByVenueName

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findByVenueName(String venueName)`
**测试函数**：`src.test.java.com.demo.service.impl.VenueServiceImplTest:testFindByVenueName()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-002 | 查询存在的场馆名 | 调用 `venueDao.findByVenueName(venueName)` 并返回对应 Venue 对象 | 返回 DAO 模拟的 Venue 对象 | 正确 |

## findAll(Pageable)

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findAll(Pageable pageable)`
**测试函数**：`src.test.java.com.demo.service.impl.VenueServiceImplTest:testFindAllByPageable()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-003 | 传入分页参数查询场馆列表 | 调用 `venueDao.findAll(pageable)` 并返回分页结果 | 返回 DAO 模拟的分页对象，记录数正确 | 正确 |

## findAll

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findAll()`
**测试函数**：`src.test.java.com.demo.service.impl.VenueServiceImplTest:testFindAll()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-004 | 查询全部场馆 | 调用 `venueDao.findAll()` 并返回列表结果 | 返回 DAO 模拟的场馆列表，共 2 条记录 | 正确 |

## create

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:create(Venue venue)`
**测试函数**：`src.test.java.com.demo.service.impl.VenueServiceImplTest:testCreate()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-005 | 新增合法场馆对象，入参 `venueID=0` | 调用 `venueDao.save(venue)`，返回持久化后对象的 `venueID`，而不是入参对象的 ID | 入参对象 `venueID=0`，DAO 返回对象 `venueID=6`，方法最终返回 `6` | 正确 |

## update

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:update(Venue venue)`
**测试函数**：`src.test.java.com.demo.service.impl.VenueServiceImplTest:testUpdate()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-006 | 更新已有场馆对象 | 调用 `venueDao.save(venue)` 完成更新 | 校验到 `save` 被调用 1 次且参数正确 | 正确 |

## delById

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:delById(int id)`
**测试函数**：`src.test.java.com.demo.service.impl.VenueServiceImplTest:testDelById()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-007 | 删除指定场馆 ID | 调用 `venueDao.deleteById(id)` 删除对应记录 | 校验到 `deleteById(8)` 被调用 1 次 | 正确 |

## countVenueName

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:countVenueName(String venueName)`
**测试函数**：`src.test.java.com.demo.service.impl.VenueServiceImplTest:testCountVenueName()`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-008 | 统计指定场馆名的数量 | 调用 `venueDao.countByVenueName(venueName)` 并返回数量 | 返回 DAO 模拟数量值 `2` | 正确 |
