package com.demo.service.impl;

import com.demo.dao.VenueDao;
import com.demo.entity.Venue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VenueService 单元测试")
class VenueServiceImplTest {

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private VenueServiceImpl venueService;

    // ==================== findByVenueID ====================

    @Test
    @DisplayName("UT-VN-001 - findByVenueID: 传入存在的场馆 ID 时返回对应 Venue 对象")
    void testFindByVenueID() {
        Venue venue = buildVenue(1, "羽毛球馆", 80);
        when(venueDao.getOne(1)).thenReturn(venue);

        Venue result = venueService.findByVenueID(1);

        assertSame(venue, result);
        verify(venueDao).getOne(1);
    }

    @Test
    @DisplayName("UT-VN-009 - findByVenueID: 传入 id=0 查询场馆时委托 DAO 查询并返回对象")
    void testFindByVenueIDBoundaryWithZeroId() {
        Venue venue = buildVenue(0, "默认场馆", 0);
        when(venueDao.getOne(0)).thenReturn(venue);

        Venue result = venueService.findByVenueID(0);

        assertSame(venue, result);
        verify(venueDao).getOne(0);
    }

    @Test
    @DisplayName("UT-VN-010 - findByVenueID: DAO 按 ID 查询场馆抛出异常时异常向上透传")
    void testFindByVenueIDException() {
        RuntimeException exception = new RuntimeException("查询场馆失败");
        when(venueDao.getOne(99)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.findByVenueID(99));

        assertSame(exception, result);
        verify(venueDao).getOne(99);
    }

    // ==================== findByVenueName ====================

    @Test
    @DisplayName("UT-VN-002 - findByVenueName: 查询存在的场馆名时返回对应 Venue 对象")
    void testFindByVenueName() {
        Venue venue = buildVenue(2, "篮球馆", 120);
        when(venueDao.findByVenueName("篮球馆")).thenReturn(venue);

        Venue result = venueService.findByVenueName("篮球馆");

        assertSame(venue, result);
        verify(venueDao).findByVenueName("篮球馆");
    }

    @Test
    @DisplayName("UT-VN-011 - findByVenueName: 传入空字符串场馆名时委托 DAO 查询并返回结果")
    void testFindByVenueNameBoundaryWithEmptyName() {
        Venue venue = buildVenue(0, "", 0);
        when(venueDao.findByVenueName("")).thenReturn(venue);

        Venue result = venueService.findByVenueName("");

        assertSame(venue, result);
        verify(venueDao).findByVenueName("");
    }

    @Test
    @DisplayName("UT-VN-012 - findByVenueName: DAO 按名称查询场馆抛出异常时异常向上透传")
    void testFindByVenueNameException() {
        RuntimeException exception = new RuntimeException("按名称查询场馆失败");
        when(venueDao.findByVenueName("异常场馆")).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.findByVenueName("异常场馆"));

        assertSame(exception, result);
        verify(venueDao).findByVenueName("异常场馆");
    }

    // ==================== findAll(Pageable) ====================

    @Test
    @DisplayName("UT-VN-003 - findAll(Pageable): 传入分页参数查询场馆列表时返回分页结果")
    void testFindAllByPageable() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Venue> page = new PageImpl<>(Collections.singletonList(buildVenue(3, "游泳馆", 150)));
        when(venueDao.findAll(pageable)).thenReturn(page);

        Page<Venue> result = venueService.findAll(pageable);

        assertSame(page, result);
        verify(venueDao).findAll(pageable);
    }

    @Test
    @DisplayName("UT-VN-013 - findAll(Pageable): 传入第一页且 DAO 返回空分页时原样返回空分页对象")
    void testFindAllByPageableBoundaryWithEmptyPage() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<Venue> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(venueDao.findAll(pageable)).thenReturn(emptyPage);

        Page<Venue> result = venueService.findAll(pageable);

        assertSame(emptyPage, result);
        assertEquals(0, result.getTotalElements());
        verify(venueDao).findAll(pageable);
    }

    @Test
    @DisplayName("UT-VN-014 - findAll(Pageable): DAO 分页查询场馆抛出异常时异常向上透传")
    void testFindAllByPageableException() {
        Pageable pageable = PageRequest.of(1, 5);
        RuntimeException exception = new RuntimeException("分页查询场馆失败");
        when(venueDao.findAll(pageable)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.findAll(pageable));

        assertSame(exception, result);
        verify(venueDao).findAll(pageable);
    }

    @Test
    @DisplayName("UT-VN-025 - findAll(Pageable): pageable=null 时调用 DAO 并异常向上透传")
    void testFindAllByPageableExceptionWithNullPageable() {
        IllegalArgumentException exception = new IllegalArgumentException("pageable 不能为空");
        when(venueDao.findAll((Pageable) null)).thenThrow(exception);

        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> venueService.findAll(null));

        assertSame(exception, result);
        verify(venueDao).findAll((Pageable) null);
    }

    // ==================== findAll ====================

    @Test
    @DisplayName("UT-VN-004 - findAll: 查询全部场馆时返回场馆列表")
    void testFindAll() {
        List<Venue> venues = Arrays.asList(
                buildVenue(4, "网球馆", 100),
                buildVenue(5, "乒乓球馆", 60)
        );
        when(venueDao.findAll()).thenReturn(venues);

        List<Venue> result = venueService.findAll();

        assertSame(venues, result);
        verify(venueDao).findAll();
    }

    @Test
    @DisplayName("UT-VN-015 - findAll: DAO 返回空场馆列表时原样返回空列表")
    void testFindAllBoundaryWithEmptyList() {
        List<Venue> venues = Collections.emptyList();
        when(venueDao.findAll()).thenReturn(venues);

        List<Venue> result = venueService.findAll();

        assertSame(venues, result);
        assertEquals(0, result.size());
        verify(venueDao).findAll();
    }

    @Test
    @DisplayName("UT-VN-016 - findAll: DAO 查询全部场馆抛出异常时异常向上透传")
    void testFindAllException() {
        RuntimeException exception = new RuntimeException("查询全部场馆失败");
        when(venueDao.findAll()).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.findAll());

        assertSame(exception, result);
        verify(venueDao).findAll();
    }

    // ==================== create ====================

    @Test
    @DisplayName("UT-VN-005 - create: 新增合法场馆对象时返回持久化后对象的 venueID")
    void testCreate() {
        Venue venue = buildVenue(0, "体操馆", 200);
        Venue savedVenue = buildVenue(6, "体操馆", 200);
        when(venueDao.save(venue)).thenReturn(savedVenue);

        int result = venueService.create(venue);

        assertEquals(6, result);
        verify(venueDao).save(venue);
    }

    @Test
    @DisplayName("UT-VN-017 - create: 新增场馆后持久化对象 venueID=0 时返回 0")
    void testCreateBoundaryWithZeroId() {
        Venue venue = buildVenue(0, "边界体操馆", 0);
        Venue savedVenue = buildVenue(0, "边界体操馆", 0);
        when(venueDao.save(venue)).thenReturn(savedVenue);

        int result = venueService.create(venue);

        assertEquals(0, result);
        verify(venueDao).save(venue);
    }

    @Test
    @DisplayName("UT-VN-018 - create: DAO 保存场馆抛出异常时异常向上透传")
    void testCreateException() {
        Venue venue = buildVenue(0, "异常体操馆", 200);
        RuntimeException exception = new RuntimeException("保存场馆失败");
        when(venueDao.save(venue)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.create(venue));

        assertSame(exception, result);
        verify(venueDao).save(venue);
    }

    @Test
    @DisplayName("UT-VN-026 - create: DAO save 返回 null 时触发 NullPointerException")
    void testCreateBoundaryWithNullSavedEntity() {
        Venue venue = buildVenue(0, "持久化返回空对象", 200);
        when(venueDao.save(venue)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> venueService.create(venue));

        verify(venueDao).save(venue);
    }

    @Test
    @Disabled("未实现：price < 0 的场馆应被拒绝")
    @DisplayName("UT-VN-029 - create: price < 0 时应抛出异常")
    void testCreate_NegativePrice_ShouldBeRejected() {
        Venue venue = buildVenue(0, "异常价格场馆", -1);

        assertThrows(Exception.class, () -> venueService.create(venue));
        verify(venueDao, never()).save(any(Venue.class));
    }

    @Test
    @Disabled("未实现：venueName 为空的场馆应被拒绝")
    @DisplayName("UT-VN-030 - create: venueName 为空时应抛出异常")
    void testCreate_EmptyVenueName_ShouldBeRejected() {
        Venue venue = buildVenue(0, "", 80);

        assertThrows(Exception.class, () -> venueService.create(venue));
        verify(venueDao, never()).save(any(Venue.class));
    }

    @Test
    @Disabled("未实现：重复 venueName 应被拒绝")
    @DisplayName("UT-VN-031 - create: venueName 重复时应抛出异常")
    void testCreate_DuplicateVenueName_ShouldBeRejected() {
        Venue venue = buildVenue(0, "羽毛球馆", 80);
        when(venueDao.countByVenueName("羽毛球馆")).thenReturn(1);

        assertThrows(Exception.class, () -> venueService.create(venue));
        verify(venueDao).countByVenueName("羽毛球馆");
        verify(venueDao, never()).save(any(Venue.class));
    }

    // ==================== update ====================

    @Test
    @DisplayName("UT-VN-006 - update: 更新已有场馆对象时委托 DAO 保存")
    void testUpdate() {
        Venue venue = buildVenue(7, "健身房", 90);

        venueService.update(venue);

        verify(venueDao).save(venue);
    }

    @Test
    @DisplayName("UT-VN-019 - update: 更新 venueID=0 的场馆对象时委托 DAO 保存")
    void testUpdateBoundaryWithZeroId() {
        Venue venue = buildVenue(0, "边界健身房", 0);

        venueService.update(venue);

        verify(venueDao).save(venue);
    }

    @Test
    @DisplayName("UT-VN-020 - update: DAO 更新场馆抛出异常时异常向上透传")
    void testUpdateException() {
        Venue venue = buildVenue(7, "异常健身房", 90);
        RuntimeException exception = new RuntimeException("更新场馆失败");
        when(venueDao.save(venue)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.update(venue));

        assertSame(exception, result);
        verify(venueDao).save(venue);
    }

    @Test
    @DisplayName("UT-VN-027 - update: venue=null 时调用 DAO 并异常向上透传")
    void testUpdateBoundaryWithNullVenue() {
        IllegalArgumentException exception = new IllegalArgumentException("venue 不能为空");
        when(venueDao.save(null)).thenThrow(exception);

        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> venueService.update(null));

        assertSame(exception, result);
        verify(venueDao).save(null);
    }

    // ==================== delById ====================

    @Test
    @DisplayName("UT-VN-007 - delById: 删除指定场馆 ID 时委托 DAO 删除对应记录")
    void testDelById() {
        venueService.delById(8);

        verify(venueDao).deleteById(8);
    }

    @Test
    @DisplayName("UT-VN-021 - delById: 删除 id=0 的场馆时委托 DAO 删除")
    void testDelByIdBoundaryWithZeroId() {
        venueService.delById(0);

        verify(venueDao).deleteById(0);
    }

    @Test
    @DisplayName("UT-VN-022 - delById: DAO 删除场馆抛出异常时异常向上透传")
    void testDelByIdException() {
        RuntimeException exception = new RuntimeException("删除场馆失败");
        doThrow(exception).when(venueDao).deleteById(9);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.delById(9));

        assertSame(exception, result);
        verify(venueDao).deleteById(9);
    }

    // ==================== countVenueName ====================

    @Test
    @DisplayName("UT-VN-008 - countVenueName: 统计指定场馆名的数量时返回 DAO 结果")
    void testCountVenueName() {
        when(venueDao.countByVenueName("综合馆")).thenReturn(2);

        int result = venueService.countVenueName("综合馆");

        assertEquals(2, result);
        verify(venueDao).countByVenueName("综合馆");
    }

    @Test
    @DisplayName("UT-VN-023 - countVenueName: 统计空字符串场馆名数量时返回 DAO 结果")
    void testCountVenueNameBoundaryWithEmptyName() {
        when(venueDao.countByVenueName("")).thenReturn(0);

        int result = venueService.countVenueName("");

        assertEquals(0, result);
        verify(venueDao).countByVenueName("");
    }

    @Test
    @DisplayName("UT-VN-024 - countVenueName: DAO 统计场馆名数量抛出异常时异常向上透传")
    void testCountVenueNameException() {
        RuntimeException exception = new RuntimeException("统计场馆名称失败");
        when(venueDao.countByVenueName("异常场馆")).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.countVenueName("异常场馆"));

        assertSame(exception, result);
        verify(venueDao).countByVenueName("异常场馆");
    }

    @Test
    @DisplayName("UT-VN-028 - countVenueName: venueName=null 时调用 DAO 并异常向上透传")
    void testCountVenueNameExceptionWithNullName() {
        IllegalArgumentException exception = new IllegalArgumentException("venueName 不能为空");
        when(venueDao.countByVenueName(null)).thenThrow(exception);

        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> venueService.countVenueName(null));

        assertSame(exception, result);
        verify(venueDao).countByVenueName(null);
    }

    private Venue buildVenue(int venueId, String venueName, int price) {
        Venue venue = new Venue();
        venue.setVenueID(venueId);
        venue.setVenueName(venueName);
        venue.setDescription(venueName + "描述");
        venue.setPrice(price);
        venue.setPicture("/static/" + venueId + ".jpg");
        venue.setAddress("测试地址" + venueId + "号");
        venue.setOpen_time("08:00");
        venue.setClose_time("22:00");
        return venue;
    }
}
