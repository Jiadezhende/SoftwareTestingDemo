package com.demo.service.impl;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void testFindByIdDelegatesToDao() {
        Order order = buildOrder(1, 11, 2, "userA");
        when(orderDao.getOne(1)).thenReturn(order);

        Order result = orderService.findById(1);

        assertSame(order, result);
        verify(orderDao).getOne(1);
    }

    @Test
    void testFindDateOrderDelegatesToDao() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 2, 10, 0);
        LocalDateTime end = start.plusHours(3);
        List<Order> orders = Arrays.asList(
                buildOrder(1, 5, 2, "userA"),
                buildOrder(2, 5, 2, "userB")
        );
        when(orderDao.findByVenueIDAndStartTimeIsBetween(5, start, end)).thenReturn(orders);

        List<Order> result = orderService.findDateOrder(5, start, end);

        assertSame(orders, result);
        verify(orderDao).findByVenueIDAndStartTimeIsBetween(5, start, end);
    }

    @Test
    void testFindUserOrderDelegatesToDao() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Collections.singletonList(buildOrder(3, 6, 1, "userA")));
        when(orderDao.findAllByUserID("userA", pageable)).thenReturn(page);

        Page<Order> result = orderService.findUserOrder("userA", pageable);

        assertSame(page, result);
        verify(orderDao).findAllByUserID("userA", pageable);
    }

    @Test
    void testSubmitCreatesOrderWithCalculatedTotal() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        orderService.submit("Badminton Hall", startTime, 1, "userA");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDao).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderService.STATE_NO_AUDIT, savedOrder.getState());
        assertEquals(1, savedOrder.getHours());
        assertEquals(8, savedOrder.getVenueID());
        assertEquals(startTime, savedOrder.getStartTime());
        assertEquals("userA", savedOrder.getUserID());
        assertEquals(120, savedOrder.getTotal());
        assertNotNull(savedOrder.getOrderTime());
    }

    @Test
    void testSubmitWithZeroHoursStillSavesOrder() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        orderService.submit("Badminton Hall", startTime, 0, "userA");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDao).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(0, savedOrder.getHours());
        assertEquals(0, savedOrder.getTotal());
    }

    @Test
    void testSubmitWithUnknownVenueThrowsNullPointerException() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Unknown Venue")).thenReturn(null);

        assertThrows(NullPointerException.class,
                () -> orderService.submit("Unknown Venue", startTime, 2, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    void testUpdateOrderUpdatesExistingOrderUsingVenuePrice() {
        Venue venue = buildVenue(9, "Tennis Court", 150);
        Order existingOrder = buildOrder(7, 3, 2, "oldUser");
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 4, 14, 0);
        when(venueDao.findByVenueName("Tennis Court")).thenReturn(venue);
        when(orderDao.findByOrderID(7)).thenReturn(existingOrder);

        orderService.updateOrder(7, "Tennis Court", startTime, 1, "userB");

        assertEquals(OrderService.STATE_NO_AUDIT, existingOrder.getState());
        assertEquals(1, existingOrder.getHours());
        assertEquals(9, existingOrder.getVenueID());
        assertEquals(startTime, existingOrder.getStartTime());
        assertEquals("userB", existingOrder.getUserID());
        assertEquals(150, existingOrder.getTotal());
        assertNotNull(existingOrder.getOrderTime());
        verify(orderDao).save(existingOrder);
    }

    @Test
    void testUpdateOrderWithMissingOrderThrowsNullPointerException() {
        Venue venue = buildVenue(9, "Tennis Court", 150);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 4, 14, 0);
        when(venueDao.findByVenueName("Tennis Court")).thenReturn(venue);
        when(orderDao.findByOrderID(404)).thenReturn(null);

        assertThrows(NullPointerException.class,
                () -> orderService.updateOrder(404, "Tennis Court", startTime, 3, "userB"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    void testDeleteOrderDelegatesToDao() {
        orderService.delOrder(10);

        verify(orderDao).deleteById(10);
    }

    @Test
    void testConfirmOrderUpdatesStateWhenOrderExists() {
        Order order = buildOrder(11, 1, 2, "userA");
        when(orderDao.findByOrderID(11)).thenReturn(order);

        orderService.confirmOrder(11);

        verify(orderDao).updateState(OrderService.STATE_WAIT, 11);
    }

    @Test
    void testConfirmOrderThrowsWhenOrderMissing() {
        when(orderDao.findByOrderID(12)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.confirmOrder(12));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    void testFinishOrderUpdatesStateWhenOrderExists() {
        Order order = buildOrder(13, 2, 2, "userA");
        when(orderDao.findByOrderID(13)).thenReturn(order);

        orderService.finishOrder(13);

        verify(orderDao).updateState(OrderService.STATE_FINISH, 13);
    }

    @Test
    void testFinishOrderThrowsWhenOrderMissing() {
        when(orderDao.findByOrderID(14)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.finishOrder(14));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    void testRejectOrderUpdatesStateWhenOrderExists() {
        Order order = buildOrder(15, 2, 2, "userA");
        when(orderDao.findByOrderID(15)).thenReturn(order);

        orderService.rejectOrder(15);

        verify(orderDao).updateState(OrderService.STATE_REJECT, 15);
    }

    @Test
    void testRejectOrderThrowsWhenOrderMissing() {
        when(orderDao.findByOrderID(16)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.rejectOrder(16));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    void testFindNoAuditOrderDelegatesToDao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Collections.singletonList(buildOrder(17, 5, 1, "userC")));
        when(orderDao.findAllByState(OrderService.STATE_NO_AUDIT, pageable)).thenReturn(page);

        Page<Order> result = orderService.findNoAuditOrder(pageable);

        assertSame(page, result);
        verify(orderDao).findAllByState(OrderService.STATE_NO_AUDIT, pageable);
    }

    @Test
    void testFindAuditOrderDelegatesToDao() {
        List<Order> orders = Arrays.asList(
                buildOrder(18, 5, OrderService.STATE_WAIT, "userD"),
                buildOrder(19, 5, OrderService.STATE_FINISH, "userE")
        );
        when(orderDao.findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH)).thenReturn(orders);

        List<Order> result = orderService.findAuditOrder();

        assertSame(orders, result);
        verify(orderDao).findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH);
    }

    private Order buildOrder(int orderId, int venueId, int state, String userId) {
        Order order = new Order();
        order.setOrderID(orderId);
        order.setVenueID(venueId);
        order.setState(state);
        order.setUserID(userId);
        order.setHours(2);
        order.setTotal(200);
        order.setOrderTime(LocalDateTime.of(2026, 4, 2, 8, 0));
        order.setStartTime(LocalDateTime.of(2026, 4, 3, 8, 0));
        return order;
    }

    private Venue buildVenue(int venueId, String venueName, int price) {
        Venue venue = new Venue();
        venue.setVenueID(venueId);
        venue.setVenueName(venueName);
        venue.setPrice(price);
        return venue;
    }
}
