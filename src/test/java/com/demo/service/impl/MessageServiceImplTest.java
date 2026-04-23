package com.demo.service.impl;

import com.demo.dao.MessageDao;
import com.demo.entity.Message;
import com.demo.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * MessageServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTest {

    @Mock
    private MessageDao messageDao;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Message existingMessage;
    private Pageable defaultPageable;

    @BeforeEach
    void setUp() {
        existingMessage = new Message();
        existingMessage.setMessageID(2);
        existingMessage.setUserID("test");
        existingMessage.setContent("hello");
        existingMessage.setState(MessageService.STATE_NO_AUDIT);
        existingMessage.setTime(LocalDateTime.now());

        defaultPageable = PageRequest.of(0, 10);
    }

    // =====================================================================
    // confirmMessage 测试
    // =====================================================================

    /**
     * UT-MG-001: 传入数据库中存在的留言 ID(2)，该留言状态变为 STATE_PASS(2)
     */
    @Test
    @DisplayName("UT-MG-001: confirmMessage - 传入存在的留言ID")
    void testConfirmMessage_existingId() {
        when(messageDao.findByMessageID(2)).thenReturn(existingMessage);

        messageService.confirmMessage(2);

        verify(messageDao).updateState(MessageService.STATE_PASS, 2);
    }

    /**
     * UT-MG-002: 传入数据库中不存在的留言 ID(9999)，抛出 RuntimeException
     */
    @Test
    @DisplayName("UT-MG-002: confirmMessage - 传入不存在的留言ID")
    void testConfirmMessage_nonExistingId() {
        when(messageDao.findByMessageID(9999)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> messageService.confirmMessage(9999));
        assertThat(ex.getMessage(), containsString("留言不存在"));
    }

    // =====================================================================
    // rejectMessage 测试
    // =====================================================================

    /**
     * UT-MG-003: 传入存在的留言 ID(2)，状态变为 STATE_REJECT(3)
     */
    @Test
    @DisplayName("UT-MG-003: rejectMessage - 传入存在的留言ID")
    void testRejectMessage_existingId() {
        when(messageDao.findByMessageID(2)).thenReturn(existingMessage);

        messageService.rejectMessage(2);

        verify(messageDao).updateState(MessageService.STATE_REJECT, 2);
    }

    /**
     * UT-MG-004: 传入不存在的留言 ID(9999)，抛出 RuntimeException
     */
    @Test
    @DisplayName("UT-MG-004: rejectMessage - 传入不存在的留言ID")
    void testRejectMessage_nonExistingId() {
        when(messageDao.findByMessageID(9999)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> messageService.rejectMessage(9999));
        assertThat(ex.getMessage(), containsString("留言不存在"));
    }

    // =====================================================================
    // create 测试
    // =====================================================================

    /**
     * UT-MG-005: 传入所有字段正常的 Message 对象，返回新增留言的 messageID
     */
    @Test
    @DisplayName("UT-MG-005: create - 传入正常Message对象")
    void testCreate_validMessage() {
        when(messageDao.save(existingMessage)).thenReturn(existingMessage);

        int id = messageService.create(existingMessage);

        assertEquals(2, id);
        verify(messageDao).save(existingMessage);
    }

    /**
     * UT-MG-006: 传入 null，应抛出异常
     */
    @Test
    @DisplayName("UT-MG-006: create - 传入null")
    void testCreate_nullMessage() {
        when(messageDao.save(null)).thenThrow(new IllegalArgumentException("Entity must not be null"));

        assertThrows(IllegalArgumentException.class,
                () -> messageService.create(null));
    }

    /**
     * UT-MG-007: 传入 userID 为 null 的 Message，应抛出异常（数据库 NOT NULL 约束）
     */
    @Test
    @DisplayName("UT-MG-007: create - userID为null")
    void testCreate_nullUserID() {
        Message msg = new Message();
        msg.setUserID(null);
        msg.setContent("test content");
        msg.setState(1);
        msg.setTime(LocalDateTime.now());

        when(messageDao.save(msg)).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException("NOT NULL constraint"));

        assertThrows(Exception.class, () -> messageService.create(msg));
    }

    /**
     * UT-MG-008: 传入 content 为 null 的 Message
     * 预期：应拒绝创建，抛出异常。当前代码未校验，此测试将失败（BUG-022）。
     */
    @Test
    @DisplayName("UT-MG-008: create - content为null应被拒绝")
    void testCreate_nullContent() {
        Message msg = new Message();
        msg.setMessageID(10);
        msg.setUserID("test");
        msg.setContent(null);
        msg.setState(1);
        msg.setTime(LocalDateTime.now());

        when(messageDao.save(msg)).thenReturn(msg);

        assertThrows(IllegalArgumentException.class, () -> messageService.create(msg));
    }

    /**
     * UT-MG-009: 传入 content 为空字符串的 Message
     * 预期：应拒绝创建，抛出异常。当前代码未校验，此测试将失败（BUG-023）。
     */
    @Test
    @DisplayName("UT-MG-009: create - content为空字符串应被拒绝")
    void testCreate_emptyContent() {
        Message msg = new Message();
        msg.setMessageID(11);
        msg.setUserID("test");
        msg.setContent("");
        msg.setState(1);
        msg.setTime(LocalDateTime.now());

        when(messageDao.save(msg)).thenReturn(msg);

        assertThrows(IllegalArgumentException.class, () -> messageService.create(msg));
    }

    // =====================================================================
    // findById 测试
    // =====================================================================

    /**
     * UT-MG-010: 传入存在的留言 ID(2)，返回对应 Message 对象
     */
    @Test
    @DisplayName("UT-MG-010: findById - 传入存在的留言ID")
    void testFindById_existingId() {
        when(messageDao.getOne(2)).thenReturn(existingMessage);

        Message result = messageService.findById(2);

        assertNotNull(result);
        assertEquals(2, result.getMessageID());
        assertEquals("test", result.getUserID());
    }

    /**
     * UT-MG-011: 传入不存在的留言 ID(9999)，抛出 EntityNotFoundException
     */
    @Test
    @DisplayName("UT-MG-011: findById - 传入不存在的留言ID")
    void testFindById_nonExistingId() {
        when(messageDao.getOne(9999)).thenThrow(new EntityNotFoundException("Message not found"));

        assertThrows(EntityNotFoundException.class,
                () -> messageService.findById(9999));
    }

    // =====================================================================
    // delById 测试
    // =====================================================================

    /**
     * UT-MG-012: 传入存在的留言 ID(2)，删除成功无异常
     */
    @Test
    @DisplayName("UT-MG-012: delById - 传入存在的留言ID")
    void testDelById_existingId() {
        doNothing().when(messageDao).deleteById(2);

        assertDoesNotThrow(() -> messageService.delById(2));
        verify(messageDao).deleteById(2);
    }

    /**
     * UT-MG-013: 传入不存在的留言 ID(9999)，抛出 EmptyResultDataAccessException
     */
    @Test
    @DisplayName("UT-MG-013: delById - 传入不存在的留言ID")
    void testDelById_nonExistingId() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(messageDao).deleteById(9999);

        assertThrows(EmptyResultDataAccessException.class,
                () -> messageService.delById(9999));
    }

    // =====================================================================
    // update 测试
    // =====================================================================

    /**
     * UT-MG-014: 传入已存在的留言，修改 content 为新内容，更新成功
     */
    @Test
    @DisplayName("UT-MG-014: update - 修改已存在留言的content")
    void testUpdate_existingMessage() {
        existingMessage.setContent("modified content");
        when(messageDao.save(existingMessage)).thenReturn(existingMessage);

        assertDoesNotThrow(() -> messageService.update(existingMessage));
        verify(messageDao).save(existingMessage);
    }

    /**
     * UT-MG-015: 传入 null，应抛出异常
     */
    @Test
    @DisplayName("UT-MG-015: update - 传入null")
    void testUpdate_nullMessage() {
        when(messageDao.save(null)).thenThrow(new IllegalArgumentException("Entity must not be null"));

        assertThrows(IllegalArgumentException.class,
                () -> messageService.update(null));
    }

    /**
     * UT-MG-016: 传入不存在的留言（messageID=9999）
     * 预期：应拒绝更新，抛出异常。当前代码未校验存在性，此测试将失败（BUG-024）。
     */
    @Test
    @DisplayName("UT-MG-016: update - 不存在的留言应被拒绝")
    void testUpdate_nonExistingMessage() {
        Message nonExisting = new Message();
        nonExisting.setMessageID(9999);
        nonExisting.setUserID("ghost");
        nonExisting.setContent("should not be saved");
        nonExisting.setState(1);
        nonExisting.setTime(LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> messageService.update(nonExisting));
    }

    // =====================================================================
    // findByUser 测试
    // =====================================================================

    /**
     * UT-MG-017: 传入有留言记录的用户 ID("test")，返回非空分页
     */
    @Test
    @DisplayName("UT-MG-017: findByUser - 有留言记录的用户")
    void testFindByUser_existingUser() {
        Page<Message> page = new PageImpl<>(Arrays.asList(existingMessage));
        when(messageDao.findAllByUserID("test", defaultPageable)).thenReturn(page);

        Page<Message> result = messageService.findByUser("test", defaultPageable);

        assertThat(result.getContent(), hasSize(1));
        assertEquals("test", result.getContent().get(0).getUserID());
    }

    /**
     * UT-MG-018: 传入无留言记录的用户 ID("nobody")，返回空分页
     */
    @Test
    @DisplayName("UT-MG-018: findByUser - 无留言记录的用户")
    void testFindByUser_noMessages() {
        Page<Message> emptyPage = new PageImpl<>(Collections.emptyList());
        when(messageDao.findAllByUserID("nobody", defaultPageable)).thenReturn(emptyPage);

        Page<Message> result = messageService.findByUser("nobody", defaultPageable);

        assertTrue(result.getContent().isEmpty());
    }

    /**
     * UT-MG-019: 传入 userID 为 null
     */
    @Test
    @DisplayName("UT-MG-019: findByUser - userID为null")
    void testFindByUser_nullUserID() {
        Page<Message> emptyPage = new PageImpl<>(Collections.emptyList());
        when(messageDao.findAllByUserID(null, defaultPageable)).thenReturn(emptyPage);

        Page<Message> result = messageService.findByUser(null, defaultPageable);

        assertTrue(result.getContent().isEmpty());
    }

    /**
     * UT-MG-020: 传入 userID 为空字符串，返回空分页
     */
    @Test
    @DisplayName("UT-MG-020: findByUser - userID为空字符串")
    void testFindByUser_emptyUserID() {
        Page<Message> emptyPage = new PageImpl<>(Collections.emptyList());
        when(messageDao.findAllByUserID("", defaultPageable)).thenReturn(emptyPage);

        Page<Message> result = messageService.findByUser("", defaultPageable);

        assertTrue(result.getContent().isEmpty());
    }

    /**
     * UT-MG-021: 传入 pageable 为 null，应抛出异常
     */
    @Test
    @DisplayName("UT-MG-021: findByUser - pageable为null")
    void testFindByUser_nullPageable() {
        when(messageDao.findAllByUserID("test", null))
                .thenThrow(new IllegalArgumentException("Pageable must not be null"));

        assertThrows(IllegalArgumentException.class,
                () -> messageService.findByUser("test", null));
    }

    // =====================================================================
    // findWaitState 测试
    // =====================================================================

    /**
     * UT-MG-022: 数据库中存在待审核留言(state=1)，返回非空分页
     */
    @Test
    @DisplayName("UT-MG-022: findWaitState - 存在待审核留言")
    void testFindWaitState_hasData() {
        Message waitMsg = new Message();
        waitMsg.setMessageID(5);
        waitMsg.setState(MessageService.STATE_NO_AUDIT);
        Page<Message> page = new PageImpl<>(Arrays.asList(waitMsg));

        when(messageDao.findAllByState(MessageService.STATE_NO_AUDIT, defaultPageable))
                .thenReturn(page);

        Page<Message> result = messageService.findWaitState(defaultPageable);

        assertThat(result.getContent(), hasSize(1));
        assertEquals(MessageService.STATE_NO_AUDIT, result.getContent().get(0).getState());
    }

    /**
     * UT-MG-023: 数据库中无待审核留言，返回空分页
     */
    @Test
    @DisplayName("UT-MG-023: findWaitState - 无待审核留言")
    void testFindWaitState_noData() {
        Page<Message> emptyPage = new PageImpl<>(Collections.emptyList());
        when(messageDao.findAllByState(MessageService.STATE_NO_AUDIT, defaultPageable))
                .thenReturn(emptyPage);

        Page<Message> result = messageService.findWaitState(defaultPageable);

        assertTrue(result.getContent().isEmpty());
    }

    /**
     * UT-MG-024: 传入 pageable 为 null，应抛出异常
     */
    @Test
    @DisplayName("UT-MG-024: findWaitState - pageable为null")
    void testFindWaitState_nullPageable() {
        when(messageDao.findAllByState(MessageService.STATE_NO_AUDIT, null))
                .thenThrow(new IllegalArgumentException("Pageable must not be null"));

        assertThrows(IllegalArgumentException.class,
                () -> messageService.findWaitState(null));
    }

    // =====================================================================
    // findPassState 测试
    // =====================================================================

    /**
     * UT-MG-025: 数据库中存在已通过留言(state=2)，返回非空分页
     */
    @Test
    @DisplayName("UT-MG-025: findPassState - 存在已通过留言")
    void testFindPassState_hasData() {
        Message passMsg = new Message();
        passMsg.setMessageID(3);
        passMsg.setState(MessageService.STATE_PASS);
        Page<Message> page = new PageImpl<>(Arrays.asList(passMsg));

        when(messageDao.findAllByState(MessageService.STATE_PASS, defaultPageable))
                .thenReturn(page);

        Page<Message> result = messageService.findPassState(defaultPageable);

        assertThat(result.getContent(), hasSize(1));
        assertEquals(MessageService.STATE_PASS, result.getContent().get(0).getState());
    }

    /**
     * UT-MG-026: 数据库中无已通过留言，返回空分页
     */
    @Test
    @DisplayName("UT-MG-026: findPassState - 无已通过留言")
    void testFindPassState_noData() {
        Page<Message> emptyPage = new PageImpl<>(Collections.emptyList());
        when(messageDao.findAllByState(MessageService.STATE_PASS, defaultPageable))
                .thenReturn(emptyPage);

        Page<Message> result = messageService.findPassState(defaultPageable);

        assertTrue(result.getContent().isEmpty());
    }

    /**
     * UT-MG-027: 传入 pageable 为 null，应抛出异常
     */
    @Test
    @DisplayName("UT-MG-027: findPassState - pageable为null")
    void testFindPassState_nullPageable() {
        when(messageDao.findAllByState(MessageService.STATE_PASS, null))
                .thenThrow(new IllegalArgumentException("Pageable must not be null"));

        assertThrows(IllegalArgumentException.class,
                () -> messageService.findPassState(null));
    }

    // =====================================================================
    //  边界值分析测试用例UT-MG-028 ~ UT-MG-040
    // =====================================================================

    // =====================================================================
    // messageID 边界值
    // =====================================================================

    /**
     * UT-MG-028: confirmMessage 传入 messageID=0（下界-1）
     */
    @Test
    @DisplayName("UT-MG-028: confirmMessage - messageID=0（下界-1）")
    void testConfirmMessage_messageIdZero() {
        when(messageDao.findByMessageID(0)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> messageService.confirmMessage(0));
        assertThat(ex.getMessage(), containsString("留言不存在"));
    }

    /**
     * UT-MG-029: confirmMessage 传入 messageID=1（下界）
     */
    @Test
    @DisplayName("UT-MG-029: confirmMessage - messageID=1（下界）")
    void testConfirmMessage_messageIdOne() {
        Message msg = new Message();
        msg.setMessageID(1);
        msg.setUserID("test");
        msg.setState(MessageService.STATE_NO_AUDIT);
        when(messageDao.findByMessageID(1)).thenReturn(msg);

        messageService.confirmMessage(1);

        verify(messageDao).updateState(MessageService.STATE_PASS, 1);
    }

    /**
     * UT-MG-030: confirmMessage 传入 messageID=-1（负数）
     */
    @Test
    @DisplayName("UT-MG-030: confirmMessage - messageID=-1（负数）")
    void testConfirmMessage_messageIdNegative() {
        when(messageDao.findByMessageID(-1)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> messageService.confirmMessage(-1));
        assertThat(ex.getMessage(), containsString("留言不存在"));
    }

    /**
     * UT-MG-031: findById 传入 messageID=0
     */
    @Test
    @DisplayName("UT-MG-031: findById - messageID=0（下界-1）")
    void testFindById_messageIdZero() {
        when(messageDao.getOne(0)).thenThrow(new EntityNotFoundException("Message not found"));

        assertThrows(EntityNotFoundException.class,
                () -> messageService.findById(0));
    }

    /**
     * UT-MG-032: delById 传入 messageID=-1
     */
    @Test
    @DisplayName("UT-MG-032: delById - messageID=-1（负数）")
    void testDelById_messageIdNegative() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(messageDao).deleteById(-1);

        assertThrows(EmptyResultDataAccessException.class,
                () -> messageService.delById(-1));
    }

    // =====================================================================
    // userID 长度边界值
    // =====================================================================

    /**
     * UT-MG-033: create 传入 userID 长度=1（下界）
     */
    @Test
    @DisplayName("UT-MG-033: create - userID长度=1（下界）")
    void testCreate_userIdLengthMin() {
        Message msg = new Message();
        msg.setMessageID(30);
        msg.setUserID("a");
        msg.setContent("test content");
        msg.setState(1);
        msg.setTime(LocalDateTime.now());

        when(messageDao.save(msg)).thenReturn(msg);

        int id = messageService.create(msg);
        assertEquals(30, id);
    }

    /**
     * UT-MG-034: create 传入 userID 长度=25（上界）
     */
    @Test
    @DisplayName("UT-MG-034: create - userID长度=25（上界）")
    void testCreate_userIdLengthMax() {
        String userId25 = "aaaaaaaaaaaaaaaaaaaaaaaaa"; // 25个a
        Message msg = new Message();
        msg.setMessageID(31);
        msg.setUserID(userId25);
        msg.setContent("test content");
        msg.setState(1);
        msg.setTime(LocalDateTime.now());

        when(messageDao.save(msg)).thenReturn(msg);

        int id = messageService.create(msg);
        assertEquals(31, id);
    }

    /**
     * UT-MG-035: create 传入 userID 长度=26（上界+1）
     */
    @Test
    @DisplayName("UT-MG-035: create - userID长度=26（上界+1，超长）")
    void testCreate_userIdLengthOverMax() {
        String userId26 = "aaaaaaaaaaaaaaaaaaaaaaaaaa"; // 26个a
        Message msg = new Message();
        msg.setMessageID(32);
        msg.setUserID(userId26);
        msg.setContent("test content");
        msg.setState(1);
        msg.setTime(LocalDateTime.now());

        when(messageDao.save(msg)).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException("Data too long for column 'userID'"));

        assertThrows(Exception.class, () -> messageService.create(msg));
    }

    // =====================================================================
    // state 边界值
    // =====================================================================

    /**
     * UT-MG-036: create 传入 state=0（下界-1，非法状态）
     * 预期：应拒绝创建，抛出异常。当前代码未校验 state 合法性，此测试将失败（BUG-025）。
     */
    @Test
    @DisplayName("UT-MG-036: create - state=0（下界-1，非法）应被拒绝")
    void testCreate_stateZero() {
        Message msg = new Message();
        msg.setMessageID(33);
        msg.setUserID("test");
        msg.setContent("test");
        msg.setState(0);
        msg.setTime(LocalDateTime.now());

        when(messageDao.save(msg)).thenReturn(msg);

        assertThrows(IllegalArgumentException.class, () -> messageService.create(msg));
    }

    /**
     * UT-MG-037: create 传入 state=1
     */
    @Test
    @DisplayName("UT-MG-037: create - state=1（下界，合法）")
    void testCreate_stateMin() {
        Message msg = new Message();
        msg.setMessageID(34);
        msg.setUserID("test");
        msg.setContent("test");
        msg.setState(MessageService.STATE_NO_AUDIT); // 1
        msg.setTime(LocalDateTime.now());

        when(messageDao.save(msg)).thenReturn(msg);

        int id = messageService.create(msg);
        assertEquals(34, id);
    }

    /**
     * UT-MG-038: create 传入 state=3（上界）
     */
    @Test
    @DisplayName("UT-MG-038: create - state=3（上界，合法）")
    void testCreate_stateMax() {
        Message msg = new Message();
        msg.setMessageID(35);
        msg.setUserID("test");
        msg.setContent("test");
        msg.setState(MessageService.STATE_REJECT); // 3
        msg.setTime(LocalDateTime.now());

        when(messageDao.save(msg)).thenReturn(msg);

        int id = messageService.create(msg);
        assertEquals(35, id);
    }

    /**
     * UT-MG-039: create 传入 state=4（上界+1，非法状态）
     * 预期：应拒绝创建，抛出异常。当前代码未校验 state 合法性，此测试将失败（BUG-026）。
     */
    @Test
    @DisplayName("UT-MG-039: create - state=4（上界+1，非法）应被拒绝")
    void testCreate_stateOverMax() {
        Message msg = new Message();
        msg.setMessageID(36);
        msg.setUserID("test");
        msg.setContent("test");
        msg.setState(4);
        msg.setTime(LocalDateTime.now());

        when(messageDao.save(msg)).thenReturn(msg);

        assertThrows(IllegalArgumentException.class, () -> messageService.create(msg));
    }

    // =====================================================================
    // Pageable 分页边界值
    // =====================================================================

    /**
     * UT-MG-040: findByUser 传入 page=0, size=1（最小合法分页）
     */
    @Test
    @DisplayName("UT-MG-040: findByUser - page=0,size=1（最小合法分页）")
    void testFindByUser_pageableMinBoundary() {
        Pageable minPageable = PageRequest.of(0, 1);
        Page<Message> page = new PageImpl<>(Arrays.asList(existingMessage));
        when(messageDao.findAllByUserID("test", minPageable)).thenReturn(page);

        Page<Message> result = messageService.findByUser("test", minPageable);

        assertThat(result.getContent(), hasSize(1));
    }

    /**
     * UT-MG-041: 传入 page=-1（负数页码）
     */
    @Test
    @DisplayName("UT-MG-041: findByUser - page=-1（负数页码）")
    void testFindByUser_negativePageNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> PageRequest.of(-1, 10));
    }

    /**
     * UT-MG-042: 传入 size=0（每页0条）
     */
    @Test
    @DisplayName("UT-MG-042: findByUser - size=0（每页0条）")
    void testFindByUser_zeroPageSize() {
        assertThrows(IllegalArgumentException.class,
                () -> PageRequest.of(0, 0));
    }
}
