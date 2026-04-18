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
    void testFindByVenueName() {
        Venue venue = buildVenue(2, "篮球馆", 120);
        when(venueDao.findByVenueName("篮球馆")).thenReturn(venue);

        Venue result = venueService.findByVenueName("篮球馆");

        assertSame(venue, result);
        verify(venueDao).findByVenueName("篮球馆");
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
    void testCreate() {
        Venue venue = buildVenue(0, "体操馆", 200);
        Venue savedVenue = buildVenue(6, "体操馆", 200);
        when(venueDao.save(venue)).thenReturn(savedVenue);

        int result = venueService.create(venue);

        assertEquals(6, result);
        verify(venueDao).save(venue);
    }

    @Test
    void testUpdate() {
        Venue venue = buildVenue(7, "健身房", 90);

        venueService.update(venue);

        verify(venueDao).save(venue);
    }

    @Test
    void testDelById() {
        venueService.delById(8);

        verify(venueDao).deleteById(8);
    }

    @Test
    void testCountVenueName() {
        when(venueDao.countByVenueName("综合馆")).thenReturn(2);

        int result = venueService.countVenueName("综合馆");

        assertEquals(2, result);
        verify(venueDao).countByVenueName("综合馆");
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
