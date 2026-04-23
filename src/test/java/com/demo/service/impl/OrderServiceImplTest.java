package com.demo.service.impl;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.service.OrderService;
import org.junit.jupiter.api.DisplayName;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 单元测试")
class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private OrderServiceImpl orderService;

    // ==================== findById ====================

    @Test
    @DisplayName("UT-OR-001 - findById: 输入存在的订单 ID 时返回对应 Order")
    void testFindByIdDelegatesToDao() {
        Order order = buildOrder(1, 11, 2, "userA");
        when(orderDao.getOne(1)).thenReturn(order);

        Order result = orderService.findById(1);

        assertSame(order, result);
        verify(orderDao).getOne(1);
    }

    // ==================== findDateOrder ====================

    @Test
    @DisplayName("UT-OR-002 - findDateOrder: 合法场馆 ID 和时间区间时返回订单列表")
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

    // ==================== findUserOrder ====================

    @Test
    @DisplayName("UT-OR-003 - findUserOrder: 合法用户 ID 时返回分页订单")
    void testFindUserOrderDelegatesToDao() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Collections.singletonList(buildOrder(3, 6, 1, "userA")));
        when(orderDao.findAllByUserID("userA", pageable)).thenReturn(page);

        Page<Order> result = orderService.findUserOrder("userA", pageable);

        assertSame(page, result);
        verify(orderDao).findAllByUserID("userA", pageable);
    }

    // ==================== submit ====================

    @Test
    @DisplayName("UT-OR-004 - submit: 有效输入时计算金额并保存待审核订单")
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
    @DisplayName("UT-OR-005 - submit: hours=0 时应拒绝并抛出异常")
    void testSubmitWithZeroHoursShouldBeRejected() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Badminton Hall", startTime, 0, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("UT-OR-006 - submit: 场馆不存在时应拒绝并抛出异常")
    void testSubmitWithUnknownVenueShouldBeRejected() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Unknown Venue")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Unknown Venue", startTime, 2, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    // ==================== updateOrder ====================

    @Test
    @DisplayName("UT-OR-007 - updateOrder: 有效输入时更新订单并重新计算金额")
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
    @DisplayName("UT-OR-008 - updateOrder: 订单不存在时应拒绝并抛出异常")
    void testUpdateOrderWithMissingOrderShouldBeRejected() {
        Venue venue = buildVenue(9, "Tennis Court", 150);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 4, 14, 0);
        when(venueDao.findByVenueName("Tennis Court")).thenReturn(venue);
        when(orderDao.findByOrderID(404)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(404, "Tennis Court", startTime, 3, "userB"));
        verify(orderDao, never()).save(any(Order.class));
    }

    // ==================== delOrder ====================

    @Test
    @DisplayName("UT-OR-009 - delOrder: 委托 DAO 删除指定订单")
    void testDeleteOrderDelegatesToDao() {
        orderService.delOrder(10);

        verify(orderDao).deleteById(10);
    }

    // ==================== confirmOrder / finishOrder / rejectOrder ====================

    @Test
    @DisplayName("UT-OR-010 - confirmOrder: 订单存在时更新状态为 STATE_WAIT")
    void testConfirmOrderUpdatesStateWhenOrderExists() {
        Order order = buildOrder(11, 1, 2, "userA");
        when(orderDao.findByOrderID(11)).thenReturn(order);

        orderService.confirmOrder(11);

        verify(orderDao).updateState(OrderService.STATE_WAIT, 11);
    }

    @Test
    @DisplayName("UT-OR-011 - confirmOrder: 订单不存在时抛出异常")
    void testConfirmOrderThrowsWhenOrderMissing() {
        when(orderDao.findByOrderID(12)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.confirmOrder(12));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    @DisplayName("UT-OR-012 - finishOrder: 订单存在时更新状态为 STATE_FINISH")
    void testFinishOrderUpdatesStateWhenOrderExists() {
        Order order = buildOrder(13, 2, 2, "userA");
        when(orderDao.findByOrderID(13)).thenReturn(order);

        orderService.finishOrder(13);

        verify(orderDao).updateState(OrderService.STATE_FINISH, 13);
    }

    @Test
    @DisplayName("UT-OR-013 - finishOrder: 订单不存在时抛出异常")
    void testFinishOrderThrowsWhenOrderMissing() {
        when(orderDao.findByOrderID(14)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.finishOrder(14));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    @DisplayName("UT-OR-014 - rejectOrder: 订单存在时更新状态为 STATE_REJECT")
    void testRejectOrderUpdatesStateWhenOrderExists() {
        Order order = buildOrder(15, 2, 2, "userA");
        when(orderDao.findByOrderID(15)).thenReturn(order);

        orderService.rejectOrder(15);

        verify(orderDao).updateState(OrderService.STATE_REJECT, 15);
    }

    @Test
    @DisplayName("UT-OR-015 - rejectOrder: 订单不存在时抛出异常")
    void testRejectOrderThrowsWhenOrderMissing() {
        when(orderDao.findByOrderID(16)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.rejectOrder(16));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    @DisplayName("UT-OR-018 - confirmOrder: 非 STATE_NO_AUDIT 前置状态时应抛出异常")
    void testConfirmOrderInvalidPreStateShouldBeRejected() {
        Order order = buildOrder(20, 2, OrderService.STATE_WAIT, "userA");
        when(orderDao.findByOrderID(20)).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> orderService.confirmOrder(20));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    @DisplayName("UT-OR-019 - finishOrder: 非 STATE_WAIT 前置状态时应抛出异常")
    void testFinishOrderInvalidPreStateShouldBeRejected() {
        Order order = buildOrder(21, 2, OrderService.STATE_NO_AUDIT, "userA");
        when(orderDao.findByOrderID(21)).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> orderService.finishOrder(21));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    @DisplayName("UT-OR-020 - rejectOrder: 非 STATE_NO_AUDIT 前置状态时应抛出异常")
    void testRejectOrderInvalidPreStateShouldBeRejected() {
        Order order = buildOrder(22, 2, OrderService.STATE_WAIT, "userA");
        when(orderDao.findByOrderID(22)).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> orderService.rejectOrder(22));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    // ==================== findNoAuditOrder / findAuditOrder ====================

    @Test
    @DisplayName("UT-OR-016 - findNoAuditOrder: 委托 DAO 查询待审核分页订单")
    void testFindNoAuditOrderDelegatesToDao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Collections.singletonList(buildOrder(17, 5, 1, "userC")));
        when(orderDao.findAllByState(OrderService.STATE_NO_AUDIT, pageable)).thenReturn(page);

        Page<Order> result = orderService.findNoAuditOrder(pageable);

        assertSame(page, result);
        verify(orderDao).findAllByState(OrderService.STATE_NO_AUDIT, pageable);
    }

    @Test
    @DisplayName("UT-OR-017 - findAuditOrder: 委托 DAO 查询已审核订单列表")
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
