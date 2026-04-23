package com.demo.service.impl;

import com.demo.dao.UserDao;
import com.demo.entity.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.demo.exception.LoginException;
import org.springframework.dao.EmptyResultDataAccessException;
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
@DisplayName("UserService 单元测试")
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUserID("user001");
        mockUser.setUserName("张三");
        mockUser.setPassword("123456");
        mockUser.setEmail("zhangsan@example.com");
        mockUser.setPhone("13800000000");
        mockUser.setIsadmin(0);
    }

    // ==================== findByUserID(String) ====================

    @Test
    @DisplayName("UT-US-001 - findByUserID: userID 存在时返回对应 User")
    void testFindByUserID_EC1_found() {
        when(userDao.findByUserID("user001")).thenReturn(mockUser);

        User result = userService.findByUserID("user001");

        assertNotNull(result);
        assertEquals("user001", result.getUserID());
    }

    @Test
    @DisplayName("UT-US-002 - findByUserID: userID 不存在时返回 null")
    void testFindByUserID_EC2_notFound() {
        when(userDao.findByUserID("ghost")).thenReturn(null);

        User result = userService.findByUserID("ghost");

        assertNull(result);
    }

    @Test
    @DisplayName("UT-US-003 - findByUserID: userID 为空字符串时返回 null")
    void testFindByUserID_EC3_emptyString() {
        when(userDao.findByUserID("")).thenReturn(null);

        User result = userService.findByUserID("");

        assertNull(result);
    }

    @Test
    @DisplayName("UT-US-004 - findByUserID: userID 为 null 时返回 null")
    void testFindByUserID_EC4_null() {
        when(userDao.findByUserID((String) null)).thenReturn(null);

        User result = userService.findByUserID((String) null);

        assertNull(result);
    }

    // ==================== findById(int) ====================

    @Test
    @DisplayName("UT-US-005 - findById: id 存在时返回对应 User")
    void testFindById_EC5_found() {
        when(userDao.findById(1)).thenReturn(mockUser);

        User result = userService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    @DisplayName("UT-US-006 - findById: id 不存在时返回 null")
    void testFindById_EC6_notFound() {
        when(userDao.findById(999)).thenReturn(null);

        User result = userService.findById(999);

        assertNull(result);
    }

    @Test
    @DisplayName("UT-US-007 - findById: id = 0（边界值）时返回 null")
    void testFindById_EC7_zero() {
        when(userDao.findById(0)).thenReturn(null);

        User result = userService.findById(0);

        assertNull(result);
    }

    @Test
    @DisplayName("UT-US-008 - findById: id 为负数时返回 null")
    void testFindById_EC8_negative() {
        when(userDao.findById(-1)).thenReturn(null);

        User result = userService.findById(-1);

        assertNull(result);
    }

    // ==================== findByUserID(Pageable) ====================

    @Test
    @DisplayName("UT-US-009 - findByUserID(Pageable): 存在普通用户时返回分页结果")
    void testFindByUserID_pageable_EC9_hasUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(Collections.singletonList(mockUser), pageable, 1);
        when(userDao.findAllByIsadmin(0, pageable)).thenReturn(mockPage);

        Page<User> result = userService.findByUserID(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getContent().get(0).getIsadmin());
    }

    @Test
    @DisplayName("UT-US-010 - findByUserID(Pageable): 无普通用户时返回空分页")
    void testFindByUserID_pageable_EC10_empty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(userDao.findAllByIsadmin(0, pageable)).thenReturn(emptyPage);

        Page<User> result = userService.findByUserID(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== checkLogin ====================

    @Test
    @DisplayName("UT-US-011 - checkLogin: 账号密码均正确时返回 User")
    void testCheckLogin_EC11_success() {
        when(userDao.findByUserIDAndPassword("user001", "123456")).thenReturn(mockUser);

        User result = userService.checkLogin("user001", "123456");

        assertNotNull(result);
        assertEquals("user001", result.getUserID());
    }

    @Test
    @DisplayName("UT-US-012 - checkLogin: 密码错误时返回 null")
    void testCheckLogin_EC12_wrongPassword() {
        when(userDao.findByUserIDAndPassword("user001", "wrong")).thenReturn(null);

        User result = userService.checkLogin("user001", "wrong");

        assertNull(result);
    }

    @Test
    @DisplayName("UT-US-013 - checkLogin: 账号不存在时返回 null")
    void testCheckLogin_EC13_userNotFound() {
        when(userDao.findByUserIDAndPassword("nobody", "123456")).thenReturn(null);

        User result = userService.checkLogin("nobody", "123456");

        assertNull(result);
    }

    @Test
    @DisplayName("UT-US-014 - checkLogin: 密码为空字符串时返回 null")
    void testCheckLogin_EC14_emptyPassword() {
        when(userDao.findByUserIDAndPassword("user001", "")).thenReturn(null);

        User result = userService.checkLogin("user001", "");

        assertNull(result);
    }

    @Test
    @DisplayName("UT-US-015 - checkLogin: userID 为 null 时服务层应抛出 LoginException 且不调用 DAO")
    void testCheckLogin_EC15_nullUserID() {
        assertThrows(LoginException.class, () -> userService.checkLogin(null, "123456"));
        verify(userDao, never()).findByUserIDAndPassword(any(), any());
    }

    @Test
    @DisplayName("UT-US-016 - checkLogin: password 为 null 时服务层应抛出 LoginException 且不调用 DAO")
    void testCheckLogin_EC16_nullPassword() {
        assertThrows(LoginException.class, () -> userService.checkLogin("user001", null));
        verify(userDao, never()).findByUserIDAndPassword(any(), any());
    }

    // ==================== create ====================

    @Test
    @DisplayName("UT-US-017 - create: 表中已有数据时返回正确总数")
    void testCreate_EC17_multipleUsers() {
        List<User> allUsers = Arrays.asList(mockUser, new User());
        when(userDao.findAll()).thenReturn(allUsers);

        int result = userService.create(mockUser);

        verify(userDao, times(1)).save(mockUser);
        assertEquals(2, result);
    }

    @Test
    @DisplayName("UT-US-018 - create: 首个用户时返回总数 1（边界值）")
    void testCreate_EC18_firstUser() {
        when(userDao.findAll()).thenReturn(Collections.singletonList(mockUser));

        int result = userService.create(mockUser);

        verify(userDao, times(1)).save(mockUser);
        assertEquals(1, result);
    }

    @Test
    @DisplayName("UT-US-019 - create: user 为 null 时抛出 IllegalArgumentException")
    void testCreate_EC19_nullUser() {
        doThrow(new IllegalArgumentException("Entity must not be null"))
                .when(userDao).save(null);

        assertThrows(IllegalArgumentException.class, () -> userService.create(null));
    }

    // ==================== delByID ====================

    @Test
    @DisplayName("UT-US-020 - delByID: id 存在时正常删除")
    void testDelByID_EC20_success() {
        doNothing().when(userDao).deleteById(1);

        userService.delByID(1);

        verify(userDao, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("UT-US-021 - delByID: id 不存在时抛出 EmptyResultDataAccessException")
    void testDelByID_EC21_notFound() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(userDao).deleteById(999);

        assertThrows(EmptyResultDataAccessException.class, () -> userService.delByID(999));
    }

    @Test
    @DisplayName("UT-US-022 - delByID: id 为负数时抛出 EmptyResultDataAccessException")
    void testDelByID_EC22_negativeId() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(userDao).deleteById(-1);

        assertThrows(EmptyResultDataAccessException.class, () -> userService.delByID(-1));
    }

    // ==================== updateUser ====================

    @Test
    @DisplayName("UT-US-023 - updateUser: 有效 User 时调用 save")
    void testUpdateUser_EC23_valid() {
        mockUser.setUserName("李四");

        userService.updateUser(mockUser);

        verify(userDao, times(1)).save(mockUser);
    }

    @Test
    @DisplayName("UT-US-024 - updateUser: user 为 null 时抛出 IllegalArgumentException")
    void testUpdateUser_EC24_nullUser() {
        doThrow(new IllegalArgumentException("Entity must not be null"))
                .when(userDao).save(null);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(null));
    }

    // ==================== countUserID ====================

    @Test
    @DisplayName("UT-US-025 - countUserID: userID 已存在时返回 1")
    void testCountUserID_EC25_exists() {
        when(userDao.countByUserID("user001")).thenReturn(1);

        int result = userService.countUserID("user001");

        assertEquals(1, result);
    }

    @Test
    @DisplayName("UT-US-026 - countUserID: userID 不存在时返回 0")
    void testCountUserID_EC26_notExists() {
        when(userDao.countByUserID("newUser")).thenReturn(0);

        int result = userService.countUserID("newUser");

        assertEquals(0, result);
    }

    @Test
    @DisplayName("UT-US-027 - countUserID: userID 为 null 时返回 0")
    void testCountUserID_EC27_null() {
        when(userDao.countByUserID(null)).thenReturn(0);

        int result = userService.countUserID(null);

        assertEquals(0, result);
    }

    // ==================== create（黑盒补充：输入校验缺陷）====================

    @Test
    @Disabled("未实现：create 应拒绝 password 为空的 User")
    @DisplayName("UT-US-028 - create: password 为空时应抛出异常")
    void testCreate_EC28_emptyPassword() {
        User user = new User();
        user.setUserID("u002");
        user.setUserName("李四");
        user.setPassword("");

        assertThrows(Exception.class, () -> userService.create(user));
        verify(userDao, never()).save(any());
    }

    @Test
    @Disabled("未实现：create 应拒绝 userID 为 null 的 User")
    @DisplayName("UT-US-029 - create: userID 字段为 null 时应抛出异常")
    void testCreate_EC29_nullUserID() {
        User user = new User();
        user.setUserID(null);
        user.setUserName("李四");
        user.setPassword("pass123");

        assertThrows(Exception.class, () -> userService.create(user));
        verify(userDao, never()).save(any());
    }

    @Test
    @Disabled("未实现：create 应调用 countUserID 检查唯一性，而非依赖数据库约束兜底")
    @DisplayName("UT-US-030 - create: userID 重复时服务层应拒绝并不调用 save")
    void testCreate_EC30_duplicateUserID() {
        when(userDao.countByUserID("user001")).thenReturn(1);

        assertThrows(Exception.class, () -> userService.create(mockUser));
        verify(userDao, never()).save(any());
    }
}
