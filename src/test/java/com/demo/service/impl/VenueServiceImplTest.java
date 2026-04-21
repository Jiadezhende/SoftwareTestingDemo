package com.demo.service.impl;

import com.demo.dao.VenueDao;
import com.demo.entity.Venue;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private VenueServiceImpl venueService;

    @Test
    void testFindByVenueID() {
        Venue venue = buildVenue(1, "羽毛球馆", 80);
        when(venueDao.getOne(1)).thenReturn(venue);

        Venue result = venueService.findByVenueID(1);

        assertSame(venue, result);
        verify(venueDao).getOne(1);
    }

    @Test
    void testFindByVenueIDBoundaryWithZeroId() {
        Venue venue = buildVenue(0, "默认场馆", 0);
        when(venueDao.getOne(0)).thenReturn(venue);

        Venue result = venueService.findByVenueID(0);

        assertSame(venue, result);
        verify(venueDao).getOne(0);
    }

    @Test
    void testFindByVenueIDException() {
        RuntimeException exception = new RuntimeException("查询场馆失败");
        when(venueDao.getOne(99)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.findByVenueID(99));

        assertSame(exception, result);
        verify(venueDao).getOne(99);
    }

    @Test
    void testFindByVenueName() {
        Venue venue = buildVenue(2, "篮球馆", 120);
        when(venueDao.findByVenueName("篮球馆")).thenReturn(venue);

        Venue result = venueService.findByVenueName("篮球馆");

        assertSame(venue, result);
        verify(venueDao).findByVenueName("篮球馆");
    }

    @Test
    void testFindByVenueNameBoundaryWithEmptyName() {
        Venue venue = buildVenue(0, "", 0);
        when(venueDao.findByVenueName("")).thenReturn(venue);

        Venue result = venueService.findByVenueName("");

        assertSame(venue, result);
        verify(venueDao).findByVenueName("");
    }

    @Test
    void testFindByVenueNameException() {
        RuntimeException exception = new RuntimeException("按名称查询场馆失败");
        when(venueDao.findByVenueName("异常场馆")).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.findByVenueName("异常场馆"));

        assertSame(exception, result);
        verify(venueDao).findByVenueName("异常场馆");
    }

    @Test
    void testFindAllByPageable() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Venue> page = new PageImpl<>(Collections.singletonList(buildVenue(3, "游泳馆", 150)));
        when(venueDao.findAll(pageable)).thenReturn(page);

        Page<Venue> result = venueService.findAll(pageable);

        assertSame(page, result);
        verify(venueDao).findAll(pageable);
    }

    @Test
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
    void testFindAllByPageableException() {
        Pageable pageable = PageRequest.of(1, 5);
        RuntimeException exception = new RuntimeException("分页查询场馆失败");
        when(venueDao.findAll(pageable)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.findAll(pageable));

        assertSame(exception, result);
        verify(venueDao).findAll(pageable);
    }

    @Test
    void testFindAllByPageableExceptionWithNullPageable() {
        IllegalArgumentException exception = new IllegalArgumentException("pageable 不能为空");
        when(venueDao.findAll((Pageable) null)).thenThrow(exception);

        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> venueService.findAll(null));

        assertSame(exception, result);
        verify(venueDao).findAll((Pageable) null);
    }

    @Test
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
    void testFindAllBoundaryWithEmptyList() {
        List<Venue> venues = Collections.emptyList();
        when(venueDao.findAll()).thenReturn(venues);

        List<Venue> result = venueService.findAll();

        assertSame(venues, result);
        assertEquals(0, result.size());
        verify(venueDao).findAll();
    }

    @Test
    void testFindAllException() {
        RuntimeException exception = new RuntimeException("查询全部场馆失败");
        when(venueDao.findAll()).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.findAll());

        assertSame(exception, result);
        verify(venueDao).findAll();
    }

    @Test
    void testCreate() {
        Venue venue = buildVenue(0, "体操馆", 200);
        Venue savedVenue = buildVenue(6, "体操馆", 200);
        when(venueDao.save(venue)).thenReturn(savedVenue);

        int result = venueService.create(venue);

        assertEquals(6, result);
        verify(venueDao).save(venue);
    }

    @Test
    void testCreateBoundaryWithZeroId() {
        Venue venue = buildVenue(0, "边界体操馆", 0);
        Venue savedVenue = buildVenue(0, "边界体操馆", 0);
        when(venueDao.save(venue)).thenReturn(savedVenue);

        int result = venueService.create(venue);

        assertEquals(0, result);
        verify(venueDao).save(venue);
    }

    @Test
    void testCreateException() {
        Venue venue = buildVenue(0, "异常体操馆", 200);
        RuntimeException exception = new RuntimeException("保存场馆失败");
        when(venueDao.save(venue)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.create(venue));

        assertSame(exception, result);
        verify(venueDao).save(venue);
    }

    @Test
    void testCreateBoundaryWithNullSavedEntity() {
        Venue venue = buildVenue(0, "持久化返回空对象", 200);
        when(venueDao.save(venue)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> venueService.create(venue));

        verify(venueDao).save(venue);
    }

    @Test
    void testUpdate() {
        Venue venue = buildVenue(7, "健身房", 90);

        venueService.update(venue);

        verify(venueDao).save(venue);
    }

    @Test
    void testUpdateBoundaryWithZeroId() {
        Venue venue = buildVenue(0, "边界健身房", 0);

        venueService.update(venue);

        verify(venueDao).save(venue);
    }

    @Test
    void testUpdateException() {
        Venue venue = buildVenue(7, "异常健身房", 90);
        RuntimeException exception = new RuntimeException("更新场馆失败");
        when(venueDao.save(venue)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.update(venue));

        assertSame(exception, result);
        verify(venueDao).save(venue);
    }

    @Test
    void testUpdateBoundaryWithNullVenue() {
        IllegalArgumentException exception = new IllegalArgumentException("venue 不能为空");
        when(venueDao.save(null)).thenThrow(exception);

        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> venueService.update(null));

        assertSame(exception, result);
        verify(venueDao).save(null);
    }

    @Test
    void testDelById() {
        venueService.delById(8);

        verify(venueDao).deleteById(8);
    }

    @Test
    void testDelByIdBoundaryWithZeroId() {
        venueService.delById(0);

        verify(venueDao).deleteById(0);
    }

    @Test
    void testDelByIdException() {
        RuntimeException exception = new RuntimeException("删除场馆失败");
        doThrow(exception).when(venueDao).deleteById(9);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.delById(9));

        assertSame(exception, result);
        verify(venueDao).deleteById(9);
    }

    @Test
    void testCountVenueName() {
        when(venueDao.countByVenueName("综合馆")).thenReturn(2);

        int result = venueService.countVenueName("综合馆");

        assertEquals(2, result);
        verify(venueDao).countByVenueName("综合馆");
    }

    @Test
    void testCountVenueNameBoundaryWithEmptyName() {
        when(venueDao.countByVenueName("")).thenReturn(0);

        int result = venueService.countVenueName("");

        assertEquals(0, result);
        verify(venueDao).countByVenueName("");
    }

    @Test
    void testCountVenueNameException() {
        RuntimeException exception = new RuntimeException("统计场馆名称失败");
        when(venueDao.countByVenueName("异常场馆")).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> venueService.countVenueName("异常场馆"));

        assertSame(exception, result);
        verify(venueDao).countByVenueName("异常场馆");
    }

    @Test
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
