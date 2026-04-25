package com.demo.service.impl;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private OrderServiceImpl orderService;

    // =====================================================================
    // findById
    // =====================================================================

    @Test
    @DisplayName("UT-OR-001 - findById: 传入存在的 orderID 时返回对应 Order 对象")
    void testFindById_delegatesToDao() {
        Order order = buildOrder(1, 11, 2, "userA");
        when(orderDao.getOne(1)).thenReturn(order);

        Order result = orderService.findById(1);

        assertSame(order, result);
        verify(orderDao).getOne(1);
    }

    @Test
    @DisplayName("UT-OR-002 - findById: 传入不存在的 orderID 时返回 null")
    void testFindById_notFound() {
        when(orderDao.getOne(999)).thenReturn(null);

        Order result = orderService.findById(999);

        assertNull(result);
        verify(orderDao).getOne(999);
    }

    @Test
    @DisplayName("UT-OR-003 - findById: [BUG-014] 传入非法 orderID（=0）时服务层未拦截，仍继续调用 DAO 查询")
    void testFindById_zeroId() {
        assertThrows(IllegalArgumentException.class, () -> orderService.findById(0));
        verify(orderDao, never()).getOne(0);
    }

    @Test
    @DisplayName("UT-OR-004 - findById: DAO 操作异常时异常向上透传")
    void testFindById_daoException() {
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(orderDao.getOne(500)).thenThrow(expected);

        DataAccessResourceFailureException thrown =
                assertThrows(DataAccessResourceFailureException.class, () -> orderService.findById(500));

        assertSame(expected, thrown);
        verify(orderDao).getOne(500);
    }

    // =====================================================================
    // findDateOrder
    // =====================================================================

    @Test
    @DisplayName("UT-OR-005 - findDateOrder: 传入合法时间区间时返回该区间内的订单列表")
    void testFindDateOrder_delegatesToDao() {
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
    @DisplayName("UT-OR-006 - findDateOrder: 时间区间内无订单时返回空列表")
    void testFindDateOrder_noMatch() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 2, 10, 0);
        LocalDateTime end = start.plusHours(3);
        when(orderDao.findByVenueIDAndStartTimeIsBetween(5, start, end)).thenReturn(Collections.emptyList());

        List<Order> result = orderService.findDateOrder(5, start, end);

        assertEquals(0, result.size());
        verify(orderDao).findByVenueIDAndStartTimeIsBetween(5, start, end);
    }

    @Test
    @DisplayName("UT-OR-007 - findDateOrder: [BUG-013] 传入 startTime > endTime 的非法时间区间时服务层未拦截，仍继续调用 DAO 查询")
    void testFindDateOrder_invalidTimeRange() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 2, 13, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 2, 10, 0);

        assertThrows(IllegalArgumentException.class, () -> orderService.findDateOrder(5, start, end));
        verify(orderDao, never()).findByVenueIDAndStartTimeIsBetween(anyInt(), any(), any());
    }

    @Test
    @DisplayName("UT-OR-008 - findDateOrder: DAO 操作异常时异常向上透传")
    void testFindDateOrder_daoException() {
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

    // =====================================================================
    // findUserOrder
    // =====================================================================

    @Test
    @DisplayName("UT-OR-009 - findUserOrder: 传入有订单记录的 userID 时返回对应分页结果")
    void testFindUserOrder_delegatesToDao() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Collections.singletonList(buildOrder(3, 6, 1, "userA")));
        when(orderDao.findAllByUserID("userA", pageable)).thenReturn(page);

        Page<Order> result = orderService.findUserOrder("userA", pageable);

        assertSame(page, result);
        verify(orderDao).findAllByUserID("userA", pageable);
    }

    @Test
    @DisplayName("UT-OR-010 - findUserOrder: 传入无订单记录的 userID 时返回空分页")
    void testFindUserOrder_unknownUser() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(Collections.emptyList());
        when(orderDao.findAllByUserID("ghost", pageable)).thenReturn(page);

        Page<Order> result = orderService.findUserOrder("ghost", pageable);

        assertSame(page, result);
        assertEquals(0, result.getContent().size());
        verify(orderDao).findAllByUserID("ghost", pageable);
    }

    @Test
    @DisplayName("UT-OR-011 - findUserOrder: [BUG-015] 传入空用户标识时服务层未拦截，仍继续调用 DAO 查询")
    void testFindUserOrder_nullUserId() {
        Pageable pageable = PageRequest.of(0, 5);

        assertThrows(IllegalArgumentException.class, () -> orderService.findUserOrder(null, pageable));
        verify(orderDao, never()).findAllByUserID(eq(null), eq(pageable));
    }

    @Test
    @DisplayName("UT-OR-012 - findUserOrder: DAO 操作异常时异常向上透传")
    void testFindUserOrder_daoException() {
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

    // =====================================================================
    // submit
    // =====================================================================

    @Test
    @DisplayName("UT-OR-013 - submit: 传入合法参数时正确计算总价并保存订单")
    void testSubmit_success() {
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
    @DisplayName("UT-OR-014 - submit: [BUG-002] 传入 hours=0 时服务层未拦截，仍保存订单")
    void testSubmit_zeroHours() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Badminton Hall", startTime, 0, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("UT-OR-015 - submit: [BUG-003] 传入不存在的 venueName 时直接抛出 NullPointerException")
    void testSubmit_unknownVenue() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Unknown Venue")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Unknown Venue", startTime, 2, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("UT-OR-016 - submit: [BUG-009] 传入 startTime=null 时服务层未拦截，仍继续保存订单")
    void testSubmit_nullStartTime() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Badminton Hall", null, 2, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("UT-OR-017 - submit: [BUG-010] 传入 userID=null 时服务层未拦截，仍继续保存订单")
    void testSubmit_nullUserId() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Badminton Hall", startTime, 2, null));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("UT-OR-018 - submit: [BUG-002] 传入 hours=-1 时服务层未拦截，仍保存订单")
    void testSubmit_negativeHours() {
        Venue venue = buildVenue(8, "Badminton Hall", 120);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 3, 9, 0);
        when(venueDao.findByVenueName("Badminton Hall")).thenReturn(venue);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.submit("Badminton Hall", startTime, -1, "userA"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("UT-OR-019 - submit: DAO 操作异常时异常向上透传，后续 save 不执行")
    void testSubmit_venueDaoException() {
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

    // =====================================================================
    // updateOrder
    // =====================================================================

    @Test
    @DisplayName("UT-OR-020 - updateOrder: 传入合法参数时正确更新订单并使用场馆单价计算总价")
    void testUpdateOrder_success() {
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
    @DisplayName("UT-OR-021 - updateOrder: [BUG-004] 传入不存在的 orderID 时直接抛出 NullPointerException")
    void testUpdateOrder_missingOrder() {
        Venue venue = buildVenue(9, "Tennis Court", 150);
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 4, 14, 0);
        when(venueDao.findByVenueName("Tennis Court")).thenReturn(venue);
        when(orderDao.findByOrderID(404)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(404, "Tennis Court", startTime, 3, "userB"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("UT-OR-022 - updateOrder: [BUG-008] 传入 hours=0 时服务层未拦截，仍更新并保存订单")
    void testUpdateOrder_zeroHours() {
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
    @DisplayName("UT-OR-023 - updateOrder: [BUG-011] 传入不存在的 venueName 时直接抛出 NullPointerException")
    void testUpdateOrder_unknownVenue() {
        Order existingOrder = buildOrder(24, 3, 2, "oldUser");
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 4, 14, 0);
        when(venueDao.findByVenueName("Unknown Venue")).thenReturn(null);
        when(orderDao.findByOrderID(24)).thenReturn(existingOrder);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(24, "Unknown Venue", startTime, 2, "userB"));
        verify(orderDao, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("UT-OR-024 - updateOrder: [BUG-008] 传入 hours=-1 时服务层未拦截，仍更新并保存订单")
    void testUpdateOrder_negativeHours() {
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
    @DisplayName("UT-OR-025 - updateOrder: DAO 操作异常时异常向上透传，后续 save 不执行")
    void testUpdateOrder_daoException() {
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

    // =====================================================================
    // delOrder
    // =====================================================================

    @Test
    @DisplayName("UT-OR-026 - delOrder: 传入存在的 orderID 时成功删除")
    void testDelOrder_delegatesToDao() {
        orderService.delOrder(10);

        verify(orderDao).deleteById(10);
    }

    @Test
    @DisplayName("UT-OR-027 - delOrder: 传入不存在的 orderID 时仍调用 DAO（透传）")
    void testDelOrder_notFound() {
        orderService.delOrder(999);

        verify(orderDao).deleteById(999);
    }

    @Test
    @DisplayName("UT-OR-028 - delOrder: [BUG-012] 传入非法 orderID（=0）时服务层未拦截，仍继续调用 DAO 删除")
    void testDelOrder_zeroId() {
        assertThrows(IllegalArgumentException.class, () -> orderService.delOrder(0));
        verify(orderDao, never()).deleteById(eq(0));
    }

    @Test
    @DisplayName("UT-OR-029 - delOrder: DAO 操作异常时异常向上透传")
    void testDelOrder_daoException() {
        EmptyResultDataAccessException expected = new EmptyResultDataAccessException(1);
        doThrow(expected).when(orderDao).deleteById(999);

        EmptyResultDataAccessException thrown =
                assertThrows(EmptyResultDataAccessException.class, () -> orderService.delOrder(999));

        assertSame(expected, thrown);
        verify(orderDao).deleteById(999);
    }

    // =====================================================================
    // confirmOrder
    // =====================================================================

    @Test
    @DisplayName("UT-OR-030 - confirmOrder: 传入 STATE_NO_AUDIT 状态的订单时成功确认（状态改为 STATE_WAIT）")
    void testConfirmOrder_success() {
        Order order = buildOrder(11, 1, 2, "userA");
        when(orderDao.findByOrderID(11)).thenReturn(order);

        orderService.confirmOrder(11);

        verify(orderDao).updateState(OrderService.STATE_WAIT, 11);
    }

    @Test
    @DisplayName("UT-OR-031 - confirmOrder: 传入不存在的 orderID 时抛出异常")
    void testConfirmOrder_notFound() {
        when(orderDao.findByOrderID(12)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.confirmOrder(12));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    @DisplayName("UT-OR-032 - confirmOrder: [BUG-005] STATE_WAIT/STATE_FINISH/STATE_REJECT 时应抛出异常（状态机未校验）")
    void testConfirmOrder_illegalState() {
        assertConfirmInvalidStateRejected(20, OrderService.STATE_WAIT);
    }

    @Test
    @DisplayName("UT-OR-033 - confirmOrder: DAO 操作异常时异常向上透传，后续 updateState 不执行")
    void testConfirmOrder_daoException() {
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

    // =====================================================================
    // finishOrder
    // =====================================================================

    @Test
    @DisplayName("UT-OR-034 - finishOrder: 传入 STATE_WAIT 状态的订单时成功完成（状态改为 STATE_FINISH）")
    void testFinishOrder_success() {
        Order order = buildOrder(13, 2, 2, "userA");
        when(orderDao.findByOrderID(13)).thenReturn(order);

        orderService.finishOrder(13);

        verify(orderDao).updateState(OrderService.STATE_FINISH, 13);
    }

    @Test
    @DisplayName("UT-OR-035 - finishOrder: 传入不存在的 orderID 时抛出异常")
    void testFinishOrder_notFound() {
        when(orderDao.findByOrderID(14)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.finishOrder(14));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    @DisplayName("UT-OR-036 - finishOrder: [BUG-006] STATE_NO_AUDIT/STATE_REJECT 时应抛出异常（状态机未校验）")
    void testFinishOrder_illegalState() {
        assertFinishInvalidStateRejected(21, OrderService.STATE_NO_AUDIT);
    }

    @Test
    @DisplayName("UT-OR-037 - finishOrder: DAO 操作异常时异常向上透传，后续 updateState 不执行")
    void testFinishOrder_daoException() {
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

    // =====================================================================
    // rejectOrder
    // =====================================================================

    @Test
    @DisplayName("UT-OR-038 - rejectOrder: 传入 STATE_WAIT 状态的订单时成功拒绝（状态改为 STATE_REJECT）")
    void testRejectOrder_success() {
        Order order = buildOrder(15, 2, 2, "userA");
        when(orderDao.findByOrderID(15)).thenReturn(order);

        orderService.rejectOrder(15);

        verify(orderDao).updateState(OrderService.STATE_REJECT, 15);
    }

    @Test
    @DisplayName("UT-OR-039 - rejectOrder: 传入不存在的 orderID 时抛出异常")
    void testRejectOrder_notFound() {
        when(orderDao.findByOrderID(16)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> orderService.rejectOrder(16));
        verify(orderDao, never()).updateState(anyInt(), anyInt());
    }

    @Test
    @DisplayName("UT-OR-040 - rejectOrder: [BUG-007] STATE_WAIT/STATE_FINISH 时应抛出异常（状态机未校验）")
    void testRejectOrder_illegalState() {
        assertRejectInvalidStateRejected(22, OrderService.STATE_WAIT);
    }

    @Test
    @DisplayName("UT-OR-041 - rejectOrder: DAO 操作异常时异常向上透传，后续 updateState 不执行")
    void testRejectOrder_daoException() {
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

    // =====================================================================
    // findNoAuditOrder
    // =====================================================================

    @Test
    @DisplayName("UT-OR-042 - findNoAuditOrder: 存在待审核订单时返回对应分页结果")
    void testFindNoAuditOrder_delegatesToDao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Collections.singletonList(buildOrder(17, 5, 1, "userC")));
        when(orderDao.findAllByState(OrderService.STATE_NO_AUDIT, pageable)).thenReturn(page);

        Page<Order> result = orderService.findNoAuditOrder(pageable);

        assertSame(page, result);
        verify(orderDao).findAllByState(OrderService.STATE_NO_AUDIT, pageable);
    }

    @Test
    @DisplayName("UT-OR-043 - findNoAuditOrder: 无待审核订单时返回空分页")
    void testFindNoAuditOrder_empty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Collections.emptyList());
        when(orderDao.findAllByState(OrderService.STATE_NO_AUDIT, pageable)).thenReturn(page);

        Page<Order> result = orderService.findNoAuditOrder(pageable);

        assertSame(page, result);
        assertEquals(0, result.getContent().size());
        verify(orderDao).findAllByState(OrderService.STATE_NO_AUDIT, pageable);
    }

    @Test
    @DisplayName("UT-OR-044 - findNoAuditOrder: [BUG-016] 传入 pageable=null 时服务层未拦截，仍继续调用 DAO 查询")
    void testFindNoAuditOrder_nullPageable() {
        assertThrows(IllegalArgumentException.class, () -> orderService.findNoAuditOrder(null));
        verify(orderDao, never()).findAllByState(eq(OrderService.STATE_NO_AUDIT), eq(null));
    }

    @Test
    @DisplayName("UT-OR-045 - findNoAuditOrder: DAO 操作异常时异常向上透传")
    void testFindNoAuditOrder_daoException() {
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

    // =====================================================================
    // findAuditOrder
    // =====================================================================

    @Test
    @DisplayName("UT-OR-046 - findAuditOrder: 存在已审核订单时返回对应列表")
    void testFindAuditOrder_delegatesToDao() {
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
    @DisplayName("UT-OR-047 - findAuditOrder: 无已审核订单时返回空列表")
    void testFindAuditOrder_empty() {
        when(orderDao.findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH))
                .thenReturn(Collections.emptyList());

        List<Order> result = orderService.findAuditOrder();

        assertEquals(0, result.size());
        verify(orderDao).findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH);
    }

    @Test
    @DisplayName("UT-OR-048 - findAuditOrder: DAO 操作异常时异常向上透传")
    void testFindAuditOrder_daoException() {
        DataAccessResourceFailureException expected = new DataAccessResourceFailureException("db down");
        when(orderDao.findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH)).thenThrow(expected);

        DataAccessResourceFailureException thrown = assertThrows(
                DataAccessResourceFailureException.class,
                () -> orderService.findAuditOrder()
        );

        assertSame(expected, thrown);
        verify(orderDao).findAudit(OrderService.STATE_WAIT, OrderService.STATE_FINISH);
    }

    // =====================================================================
    // 辅助方法
    // =====================================================================

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
