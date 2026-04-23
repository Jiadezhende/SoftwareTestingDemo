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
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
    void testFindByIdReturnsNullWhenOrderDoesNotExist() {
        when(orderDao.getOne(999)).thenReturn(null);

        Order result = orderService.findById(999);

        assertNull(result);
        verify(orderDao).getOne(999);
    }

    @Test
    void testFindByIdWithZeroIdShouldBeRejected() {
        assertThrows(IllegalArgumentException.class, () -> orderService.findById(0));
        verify(orderDao, never()).getOne(0);
    }


    @Test
    @DisplayName("findById: DAO 操作异常时异常向上透传")
    void testFindByIdPropagatesDaoException() {
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(orderDao.getOne(500)).thenThrow(expected);

        DataAccessResourceFailureException thrown =
                assertThrows(DataAccessResourceFailureException.class, () -> orderService.findById(500));

        assertSame(expected, thrown);
        verify(orderDao).getOne(500);
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
    void testFindDateOrderReturnsEmptyListWhenNoOrderMatches() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 2, 10, 0);
        LocalDateTime end = start.plusHours(3);
        when(orderDao.findByVenueIDAndStartTimeIsBetween(5, start, end)).thenReturn(Collections.emptyList());

        List<Order> result = orderService.findDateOrder(5, start, end);

        assertEquals(0, result.size());
        verify(orderDao).findByVenueIDAndStartTimeIsBetween(5, start, end);
    }

    @Test
    void testFindDateOrderWithInvalidTimeRangeShouldBeRejected() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 2, 13, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 2, 10, 0);

        assertThrows(IllegalArgumentException.class, () -> orderService.findDateOrder(5, start, end));
        verify(orderDao, never()).findByVenueIDAndStartTimeIsBetween(anyInt(), any(), any());
    }

    @Test
    @DisplayName("findDateOrder: DAO 操作异常时异常向上透传")
    void testFindDateOrderPropagatesDaoException() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 2, 10, 0);
        LocalDateTime end = start.plusHours(3);
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(orderDao.findByVenueIDAndStartTimeIsBetween(5, start, end)).thenThrow(expected);

        DataAccessResourceFailureException thrown = assertThrows(
                DataAccessResourceFailureException.class,
                () -> orderService.findDateOrder(5, start, end)
        );

        assertSame(expected, thrown);
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
    void testFindUserOrderReturnsEmptyPageForUnknownUser() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Collections.emptyList());
        when(orderDao.findAllByUserID("ghost", pageable)).thenReturn(page);

        Page<Order> result = orderService.findUserOrder("ghost", pageable);

        assertSame(page, result);
        assertEquals(0, result.getContent().size());
        verify(orderDao).findAllByUserID("ghost", pageable);
    }

    @Test
    void testFindUserOrderWithNullUserIdShouldBeRejected() {
        Pageable pageable = PageRequest.of(0, 5);

        assertThrows(IllegalArgumentException.class, () -> orderService.findUserOrder(null, pageable));
        verify(orderDao, never()).findAllByUserID(eq(null), eq(pageable));
    }


    @Test
    @DisplayName("findUserOrder: DAO 操作异常时异常向上透传")
    void testFindUserOrderPropagatesDaoException() {
        Pageable pageable = PageRequest.of(0, 5);
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(orderDao.findAllByUserID("userA", pageable)).thenThrow(expected);

        DataAccessResourceFailureException thrown = assertThrows(
                DataAccessResourceFailureException.class,
                () -> orderService.findUserOrder("userA", pageable)
        );

        assertSame(expected, thrown);
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
    void testSubmitWithZeroHoursShouldBeRejected() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Badminton Hall", startTime, 0, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    void testSubmitWithNegativeHoursShouldBeRejected() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Badminton Hall", startTime, -1, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    void testSubmitWithUnknownVenueShouldBeRejected() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Unknown Venue")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Unknown Venue", startTime, 2, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    void testSubmitWithNullStartTimeShouldBeRejected() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Badminton Hall", null, 2, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    void testSubmitWithNullUserIdShouldBeRejected() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Badminton Hall", startTime, 2, null));
        verify(orderDao, never()).save(any(Order.class));
    }


    @Test
    @DisplayName("submit: DAO 操作异常时异常向上透传，后续 save 不执行")
    void testSubmitPropagatesVenueDaoExceptionAndDoesNotSave() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(venueDao.findByVenueName("Badminton Hall")).thenThrow(expected);

        DataAccessResourceFailureException thrown = assertThrows(
                DataAccessResourceFailureException.class,
                () -> orderService.submit("Badminton Hall", startTime, 2, "userA")
        );

        assertSame(expected, thrown);
        verify(venueDao).findByVenueName("Badminton Hall");
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
    void testUpdateOrderWithMissingOrderShouldBeRejected() {
        Venue venue = buildVenue(9, "Tennis Court", 150);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 4, 14, 0);
        when(venueDao.findByVenueName("Tennis Court")).thenReturn(venue);
        when(orderDao.findByOrderID(404)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(404, "Tennis Court", startTime, 3, "userB"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    void testUpdateOrderWithZeroHoursShouldBeRejected() {
        Venue venue = buildVenue(9, "Tennis Court", 150);
        Order existingOrder = buildOrder(23, 3, 2, "oldUser");
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 4, 14, 0);
        when(venueDao.findByVenueName("Tennis Court")).thenReturn(venue);
        when(orderDao.findByOrderID(23)).thenReturn(existingOrder);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(23, "Tennis Court", startTime, 0, "userB"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    void testUpdateOrderWithNegativeHoursShouldBeRejected() {
        Venue venue = buildVenue(9, "Tennis Court", 150);
        Order existingOrder = buildOrder(23, 3, 2, "oldUser");
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 4, 14, 0);
        when(venueDao.findByVenueName("Tennis Court")).thenReturn(venue);
        when(orderDao.findByOrderID(23)).thenReturn(existingOrder);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(23, "Tennis Court", startTime, -1, "userB"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    void testUpdateOrderWithUnknownVenueShouldBeRejected() {
        Order existingOrder = buildOrder(24, 3, 2, "oldUser");
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 4, 14, 0);
        when(venueDao.findByVenueName("Unknown Venue")).thenReturn(null);
        when(orderDao.findByOrderID(24)).thenReturn(existingOrder);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(24, "Unknown Venue", startTime, 2, "userB"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("updateOrder: DAO 操作异常时异常向上透传，后续 save 不执行")
    void testUpdateOrderPropagatesDaoExceptionAndDoesNotSave() {
        Venue venue = buildVenue(9, "Tennis Court", 150);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 4, 14, 0);
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(venueDao.findByVenueName("Tennis Court")).thenReturn(venue);
        when(orderDao.findByOrderID(23)).thenThrow(expected);

        DataAccessResourceFailureException thrown = assertThrows(
                DataAccessResourceFailureException.class,
                () -> orderService.updateOrder(23, "Tennis Court", startTime, 2, "userB")
        );

        assertSame(expected, thrown);
        verify(venueDao).findByVenueName("Tennis Court");
        verify(orderDao).findByOrderID(23);
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    void testDeleteOrderDelegatesToDao() {
        orderService.delOrder(10);

        verify(orderDao).deleteById(10);
    }

    @Test
    void testDeleteOrderDelegatesToDaoWhenOrderDoesNotExist() {
        orderService.delOrder(999);

        verify(orderDao).deleteById(999);
    }

    @Test
    void testDeleteOrderWithZeroIdShouldBeRejected() {
        assertThrows(IllegalArgumentException.class, () -> orderService.delOrder(0));
        verify(orderDao, never()).deleteById(eq(0));
    }


    @Test
    @DisplayName("delOrder: DAO 操作异常时异常向上透传")
    void testDeleteOrderPropagatesDaoException() {
        EmptyResultDataAccessException expected = new EmptyResultDataAccessException(1);
        doThrow(expected).when(orderDao).deleteById(999);

        EmptyResultDataAccessException thrown =
                assertThrows(EmptyResultDataAccessException.class, () -> orderService.delOrder(999));

        assertSame(expected, thrown);
        verify(orderDao).deleteById(999);
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
    @DisplayName("confirmOrder: DAO 操作异常时异常向上透传，后续 updateState 不执行")
    void testConfirmOrderPropagatesDaoExceptionAndDoesNotUpdateState() {
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(orderDao.findByOrderID(30)).thenThrow(expected);

        DataAccessResourceFailureException thrown = assertThrows(
                DataAccessResourceFailureException.class,
                () -> orderService.confirmOrder(30)
        );

        assertSame(expected, thrown);
        verify(orderDao).findByOrderID(30);
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
    @DisplayName("finishOrder: DAO 操作异常时异常向上透传，后续 updateState 不执行")
    void testFinishOrderPropagatesDaoExceptionAndDoesNotUpdateState() {
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(orderDao.findByOrderID(31)).thenThrow(expected);

        DataAccessResourceFailureException thrown = assertThrows(
                DataAccessResourceFailureException.class,
                () -> orderService.finishOrder(31)
        );

        assertSame(expected, thrown);
        verify(orderDao).findByOrderID(31);
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
    @DisplayName("rejectOrder: DAO 操作异常时异常向上透传，后续 updateState 不执行")
    void testRejectOrderPropagatesDaoExceptionAndDoesNotUpdateState() {
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(orderDao.findByOrderID(32)).thenThrow(expected);

        DataAccessResourceFailureException thrown = assertThrows(
                DataAccessResourceFailureException.class,
                () -> orderService.rejectOrder(32)
        );

        assertSame(expected, thrown);
        verify(orderDao).findByOrderID(32);
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    @DisplayName("BB-SC-06 - confirmOrder: STATE_WAIT/STATE_FINISH/STATE_REJECT 时应抛出异常")
    void testConfirmOrderWithWaitStateShouldBeRejected() {
        assertConfirmInvalidStateRejected(20, OrderService.STATE_WAIT);
    }


    @Test
    @DisplayName("BB-SC-07 - finishOrder: STATE_NO_AUDIT/STATE_REJECT 时应抛出异常")
    void testFinishOrderWithNoAuditStateShouldBeRejected() {
        assertFinishInvalidStateRejected(21, OrderService.STATE_NO_AUDIT);
    }


    @Test
    @DisplayName("BB-SC-08 - rejectOrder: STATE_WAIT/STATE_FINISH 时应抛出异常")
    void testRejectOrderWithWaitStateShouldBeRejected() {
        assertRejectInvalidStateRejected(22, OrderService.STATE_WAIT);
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
    void testFindNoAuditOrderReturnsEmptyPageWhenNoPendingOrderExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Collections.emptyList());
        when(orderDao.findAllByState(OrderService.STATE_NO_AUDIT, pageable)).thenReturn(page);

        Page<Order> result = orderService.findNoAuditOrder(pageable);

        assertSame(page, result);
        assertEquals(0, result.getContent().size());
        verify(orderDao).findAllByState(OrderService.STATE_NO_AUDIT, pageable);
    }

    @Test
    void testFindNoAuditOrderWithNullPageableShouldBeRejected() {
        assertThrows(IllegalArgumentException.class, () -> orderService.findNoAuditOrder(null));
        verify(orderDao, never()).findAllByState(eq(OrderService.STATE_NO_AUDIT), eq(null));
    }

    @Test
    @DisplayName("findNoAuditOrder: DAO 操作异常时异常向上透传")
    void testFindNoAuditOrderPropagatesDaoException() {
        Pageable pageable = PageRequest.of(0, 10);
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(orderDao.findAllByState(OrderService.STATE_NO_AUDIT, pageable)).thenThrow(expected);

        DataAccessResourceFailureException thrown = assertThrows(
                DataAccessResourceFailureException.class,
                () -> orderService.findNoAuditOrder(pageable)
        );

        assertSame(expected, thrown);
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

    @Test
    void testFindAuditOrderReturnsEmptyListWhenNoAuditedOrderExists() {
        when(orderDao.findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH))
                .thenReturn(Collections.emptyList());

        List<Order> result = orderService.findAuditOrder();

        assertEquals(0, result.size());
        verify(orderDao).findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH);
    }

    @Test
    @DisplayName("findAuditOrder: DAO 操作异常时异常向上透传")
    void testFindAuditOrderPropagatesDaoException() {
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(orderDao.findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH)).thenThrow(expected);

        DataAccessResourceFailureException thrown = assertThrows(
                DataAccessResourceFailureException.class,
                () -> orderService.findAuditOrder()
        );

        assertSame(expected, thrown);
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

    private void assertConfirmInvalidStateRejected(int orderId, int state) {
        Order order = buildOrder(orderId, 2, state, "userA");
        when(orderDao.findByOrderID(orderId)).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> orderService.confirmOrder(orderId));
        verify(orderDao, never()).updateState(anyInt(), eq(orderId));
    }

    private void assertFinishInvalidStateRejected(int orderId, int state) {
        Order order = buildOrder(orderId, 2, state, "userA");
        when(orderDao.findByOrderID(orderId)).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> orderService.finishOrder(orderId));
        verify(orderDao, never()).updateState(anyInt(), eq(orderId));
    }

    private void assertRejectInvalidStateRejected(int orderId, int state) {
        Order order = buildOrder(orderId, 2, state, "userA");
        when(orderDao.findByOrderID(orderId)).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> orderService.rejectOrder(orderId));
        verify(orderDao, never()).updateState(anyInt(), eq(orderId));
    }

    private Venue buildVenue(int venueId, String venueName, int price) {
        Venue venue = new Venue();
        venue.setVenueID(venueId);
        venue.setVenueName(venueName);
        venue.setPrice(price);
        return venue;
    }
}
