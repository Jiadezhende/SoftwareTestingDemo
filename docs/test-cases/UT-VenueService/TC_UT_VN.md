# 测试用例设计文档 — VenueServiceImpl

## 文档信息

| 字段      | 值 |
|----------|----|
| 模块名称  | `VenueServiceImpl` |
| 负责成员  | 成员C |
| 用例编号段 | `UT-VN-001 ~ UT-VN-099` |
| 创建日期  | 2026-04-17 |
| 最后更新  | 2026-04-21 |

### 变更记录

| 版本  | 日期 | 修改人 | 说明 |
|------|------|--------|------|
| v1.0 | 2026-04-17 | 成员C | 完成 VenueServiceImpl 单元测试设计与执行 |
| v1.1 | 2026-04-18 | 成员C | 更新 create 用例描述，明确校验返回值来自持久化后的对象 |
| v1.2 | 2026-04-21 | 成员C | 按等价类、边界值、异常路径补充 `VenueServiceImpl` 全部公开方法用例 |

---

## findByVenueID

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findByVenueID(int id)`
**测试函数**：`testFindByVenueID_found()`、`testFindByVenueID_zeroId()`、`testFindByVenueID_daoException()`
**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-001 | 传入存在的场馆 ID | 调用 `venueDao.getOne(id)` 并返回对应 Venue 对象 | 返回 DAO 模拟的 Venue 对象，字段保持一致 | 正确 |
| UT-VN-009 | 边界值：传入 `id=0` 查询场馆 | 调用 `venueDao.getOne(0)` 并返回对应对象 | 返回 DAO 模拟的 `venueID=0` 场馆对象 | 正确 |
| UT-VN-010 | 异常路径：DAO 按 ID 查询场馆抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

## findByVenueName

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findByVenueName(String venueName)`
**测试函数**：`testFindByVenueName_found()`、`testFindByVenueName_emptyName()`、`testFindByVenueName_daoException()`
**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-002 | 查询存在的场馆名 | 调用 `venueDao.findByVenueName(venueName)` 并返回对应 Venue 对象 | 返回 DAO 模拟的 Venue 对象 | 正确 |
| UT-VN-011 | 边界值：传入空字符串场馆名 | 调用 `venueDao.findByVenueName("")` 并返回 DAO 结果 | 返回 DAO 模拟的空名称场馆对象 | 正确 |
| UT-VN-012 | 异常路径：DAO 按名称查询场馆抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

## findAll(Pageable)

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findAll(Pageable pageable)`
**测试函数**：`testFindAll_pageable()`、`testFindAll_pageable_emptyPage()`、`testFindAll_pageable_daoException()`、`testFindAll_pageable_nullPageable()`
**设计技术**：`等价类划分 + 边界值分析 + 决策表`

### 用例设计（等价类/边界值/决策表）

**等价类划分（Pageable）**

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `pageable != null` | 返回 `venueDao.findAll(pageable)` 的结果 |
| EC2 | `pageable == null` | 异常向上透传（由 DAO 或下层抛出） |

**边界值分析（分页）**

| 参数 | 边界值 | 覆盖用例 |
|------|--------|----------|
| `pageSize` | `1` | `UT-VN-013`（空分页） |
| `pageNumber` | `0`（第一页） | `UT-VN-003` / `UT-VN-013` |

**决策表（核心规则）**

| 条件/规则 | R1 | R2 | R3 | R4 |
|---|---|---|---|---|
| `pageable == null` | 否 | 否 | 否 | 是 |
| DAO 返回空分页 | 否 | 是 | — | — |
| DAO 抛异常 | 否 | 否 | 是 | 是 |
| 期望 | 返回分页结果 | 返回空分页 | 异常透传 | 异常透传 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-003 | 传入分页参数查询场馆列表 | 调用 `venueDao.findAll(pageable)` 并返回分页结果 | 返回 DAO 模拟的分页对象，记录数正确 | 正确 |
| UT-VN-013 | 边界值：传入第一页且 DAO 返回空分页 | 调用 `venueDao.findAll(pageable)` 并原样返回空分页对象 | 返回空分页对象，`totalElements=0` | 正确 |
| UT-VN-014 | 异常路径：DAO 分页查询场馆抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-VN-025 | 等价类：`pageable=null` | 调用 `venueDao.findAll(null)`，异常向上透传 | 捕获到 DAO 抛出的 `IllegalArgumentException`，对象一致 | 正确 |

## findAll

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:findAll()`
**测试函数**：`testFindAll_noParam()`、`testFindAll_noParam_emptyList()`、`testFindAll_noParam_daoException()`
**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-004 | 查询全部场馆 | 调用 `venueDao.findAll()` 并返回列表结果 | 返回 DAO 模拟的场馆列表，共 2 条记录 | 正确 |
| UT-VN-015 | 边界值：DAO 返回空场馆列表 | 调用 `venueDao.findAll()` 并原样返回空列表 | 返回空列表，`size=0` | 正确 |
| UT-VN-016 | 异常路径：DAO 查询全部场馆抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

## create

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:create(Venue venue)`
**测试函数**：`testCreate_success()`、`testCreate_zeroId()`、`testCreate_daoException()`、`testCreate_nullSavedEntity()`、`testCreate_negativePrice()`、`testCreate_emptyVenueName()`、`testCreate_duplicateVenueName()`
**设计技术**：`等价类划分 + 边界值分析 + 决策表`

### 用例设计（等价类/边界值/决策表）

**等价类划分（入参 Venue）**

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `venue != null` 且 `price >= 0` 且 `venueName` 非空且未重复 | 调用 `venueDao.save(venue)` 并返回 `save` 结果的 `venueID` |
| EC2 | `venue.price < 0` | 抛出业务异常，且不调用 `venueDao.save(venue)` |
| EC3 | `venue.venueName == ""` | 抛出业务异常，且不调用 `venueDao.save(venue)` |
| EC4 | `venueName` 已存在（需先查询数量） | 抛出业务异常，且不调用 `venueDao.save(venue)` |
| EC5 | `venue == null` | 异常向上透传（由 DAO 或下层抛出） |

**边界值分析（返回值来源）**

| 关注点 | 边界/特殊值 | 覆盖用例 |
|--------|------------|----------|
| `save(venue)` 返回对象 | `null` | `UT-VN-026` |
| 返回 `venueID` | `0` | `UT-VN-017` |
| `price` | `-1` | `UT-VN-029` |
| `venueName` | `""` | `UT-VN-030` |
| `countVenueName(venueName)` | `1`（已存在） | `UT-VN-031` |

**决策表（核心规则）**

| 条件/规则 | R1 | R2 | R3 | R4 | R5 | R6 | R7 |
|---|---|---|---|---|---|---|---|
| `price < 0` | 否 | 否 | 否 | 否 | 是 | 否 | 否 |
| `venueName == ""` | 否 | 否 | 否 | 否 | 否 | 是 | 否 |
| `countVenueName(venueName) > 0` | 否 | 否 | 否 | 否 | 否 | 否 | 是 |
| `venue == null` | 否 | 否 | 否 | 是 | — | — | — |
| DAO 返回 `savedVenue == null` | 否 | 是 | 否 | — | — | — | — |
| DAO 抛异常 | 否 | 否 | 是 | 是 | — | — | — |
| 期望 | 返回 `savedVenue.venueID` | 抛出 `NullPointerException` | 异常透传 | 异常透传 | 抛出业务异常且不保存 | 抛出业务异常且不保存 | 抛出业务异常且不保存 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-005 | 新增合法场馆对象，入参 `venueID=0` | 调用 `venueDao.save(venue)`，返回持久化后对象的 `venueID`，而不是入参对象的 ID | 入参对象 `venueID=0`，DAO 返回对象 `venueID=6`，方法最终返回 `6` | 正确 |
| UT-VN-017 | 边界值：新增场馆后持久化对象 `venueID=0` | 调用 `venueDao.save(venue)`，返回持久化后对象的 `venueID=0` | 方法最终返回 `0`，证明 service 未额外加工返回值 | 正确 |
| UT-VN-018 | 异常路径：DAO 保存场馆抛出异常 | 异常向上透传，不返回默认值 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-VN-026 | 边界值：DAO `save` 返回 `null` | 调用 `venueDao.save(venue)` 后获取 `venueID` 触发空指针异常 | 捕获到 `NullPointerException` | 正确 |
| UT-VN-029 | 缺陷占位：`price < 0` 的场馆不应被创建 | 应抛出业务异常，且不调用 `venueDao.save(venue)` | 已补充 `@Disabled` 占位测试 `testCreate_negativePrice()`，当前实现未校验该场景 | 待测 |
| UT-VN-030 | 缺陷占位：`venueName` 为空的场馆不应被创建 | 应抛出业务异常，且不调用 `venueDao.save(venue)` | 已补充 `@Disabled` 占位测试 `testCreate_emptyVenueName()`，当前实现未校验该场景 | 待测 |
| UT-VN-031 | 缺陷占位：重复 `venueName` 的场馆不应被创建 | 应先校验 `countVenueName(venueName)`，若已存在则抛出业务异常，且不调用 `venueDao.save(venue)` | 已补充 `@Disabled` 占位测试 `testCreate_duplicateVenueName()`，当前实现未调用重名校验 | 待测 |

## update

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:update(Venue venue)`
**测试函数**：`testUpdate_success()`、`testUpdate_zeroId()`、`testUpdate_daoException()`、`testUpdate_nullVenue()`
**设计技术**：`等价类划分 + 边界值分析`

### 用例设计（等价类/边界值/决策表）

**等价类划分（入参 Venue）**

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `venue != null` | 调用 `venueDao.save(venue)` 并完成更新请求转发 |
| EC2 | `venue == null` | 异常向上透传（由 DAO 或下层抛出） |

**边界值分析（venueID）**

| 参数 | 边界值 | 覆盖用例 |
|------|--------|----------|
| `venue.venueID` | `0` | `UT-VN-019` |
| `venue` | `null` | `UT-VN-027` |

**决策表（核心规则）**

| 条件/规则 | R1 | R2 | R3 |
|---|---|---|---|
| `venue == null` | 否 | 否 | 是 |
| DAO 抛异常 | 否 | 是 | 是 |
| 期望 | 正常转发保存 | 异常透传 | 异常透传 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-006 | 更新已有场馆对象 | 调用 `venueDao.save(venue)` 完成更新 | 校验到 `save` 被调用 1 次且参数正确 | 正确 |
| UT-VN-019 | 边界值：更新 `venueID=0` 的场馆对象 | 调用 `venueDao.save(venue)` 完成更新请求转发 | 校验到 `save` 被调用 1 次且参数为 `venueID=0` 对象 | 正确 |
| UT-VN-020 | 异常路径：DAO 更新场馆抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-VN-027 | 等价类：`venue=null` | 调用 `venueDao.save(null)`，异常向上透传 | 捕获到 DAO 抛出的 `IllegalArgumentException`，对象一致 | 正确 |

## delById

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:delById(int id)`
**测试函数**：`testDelById_success()`、`testDelById_zeroId()`、`testDelById_daoException()`
**设计技术**：`等价类划分 + 边界值分析`

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-007 | 删除指定场馆 ID | 调用 `venueDao.deleteById(id)` 删除对应记录 | 校验到 `deleteById(8)` 被调用 1 次 | 正确 |
| UT-VN-021 | 边界值：删除 `id=0` 的场馆 | 调用 `venueDao.deleteById(0)` | 校验到 `deleteById(0)` 被正确调用 | 正确 |
| UT-VN-022 | 异常路径：DAO 删除场馆抛出异常 | 异常向上透传，不做吞并或忽略 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |

## countVenueName

**测试对象**：`src.main.java.com.demo.service.impl.VenueServiceImpl.java:countVenueName(String venueName)`
**测试函数**：`testCountVenueName_found()`、`testCountVenueName_emptyName()`、`testCountVenueName_daoException()`、`testCountVenueName_nullName()`
**设计技术**：`等价类划分 + 边界值分析`

### 用例设计（等价类/边界值/决策表）

**等价类划分（venueName）**

| 等价类 | 输入特征 | 预期行为 |
|-------|----------|----------|
| EC1 | `venueName` 为普通非空字符串 | 返回 `venueDao.countByVenueName(venueName)` |
| EC2 | `venueName == ""` | 返回 `venueDao.countByVenueName("")` |
| EC3 | `venueName == null` | 异常向上透传（由 DAO 或下层抛出） |

**边界值分析**

| 参数 | 边界值 | 覆盖用例 |
|------|--------|----------|
| `venueName` | `""` | `UT-VN-023` |
| `venueName` | `null` | `UT-VN-028` |

**决策表（核心规则）**

| 条件/规则 | R1 | R2 | R3 | R4 |
|---|---|---|---|---|
| `venueName == null` | 否 | 否 | 否 | 是 |
| `venueName == ""` | 否 | 是 | 否 | — |
| DAO 抛异常 | 否 | 否 | 是 | 是 |
| 期望 | 返回计数 | 返回计数 | 异常透传 | 异常透传 |

| 用例编号 | 用例描述 | 预期结果 | 测试结果 | 结论 |
|---------|---------|---------|---------|------|
| UT-VN-008 | 统计指定场馆名的数量 | 调用 `venueDao.countByVenueName(venueName)` 并返回数量 | 返回 DAO 模拟数量值 `2` | 正确 |
| UT-VN-023 | 边界值：统计空字符串场馆名数量 | 调用 `venueDao.countByVenueName("")` 并返回数量 | 返回 DAO 模拟数量值 `0` | 正确 |
| UT-VN-024 | 异常路径：DAO 统计场馆名数量抛出异常 | 异常向上透传，不做吞并或转换 | 捕获到 DAO 抛出的 `RuntimeException`，对象一致 | 正确 |
| UT-VN-028 | 等价类：`venueName=null` | 调用 `venueDao.countByVenueName(null)`，异常向上透传 | 捕获到 DAO 抛出的 `IllegalArgumentException`，对象一致 | 正确 |
