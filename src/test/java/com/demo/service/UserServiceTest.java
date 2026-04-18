package com.demo.service;

import com.demo.dao.UserDao;
import com.demo.entity.User;
import com.demo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试（等价类划分）")
class UserServiceTest {

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
    // EC1: userID 存在 → 返回对应 User

    @Test
    @DisplayName("EC1 - findByUserID: userID 存在时返回对应 User")
    void testFindByUserID_EC1_found() {
        when(userDao.findByUserID("user001")).thenReturn(mockUser);

        User result = userService.findByUserID("user001");

        assertNotNull(result);
        assertEquals("user001", result.getUserID());
    }

    // EC2: userID 不存在 → 返回 null
    @Test
    @DisplayName("EC2 - findByUserID: userID 不存在时返回 null")
    void testFindByUserID_EC2_notFound() {
        when(userDao.findByUserID("ghost")).thenReturn(null);

        User result = userService.findByUserID("ghost");

        assertNull(result);
    }

    // EC3: userID = "" → 返回 null（边界值）
    @Test
    @DisplayName("EC3 - findByUserID: userID 为空字符串时返回 null")
    void testFindByUserID_EC3_emptyString() {
        when(userDao.findByUserID("")).thenReturn(null);

        User result = userService.findByUserID("");

        assertNull(result);
    }

    // EC4: userID = null → 返回 null
    @Test
    @DisplayName("EC4 - findByUserID: userID 为 null 时返回 null")
    void testFindByUserID_EC4_null() {
        when(userDao.findByUserID((String) null)).thenReturn(null);

        User result = userService.findByUserID((String) null);

        assertNull(result);
    }

    // ==================== findById(int) ====================
    // EC5: id > 0 且存在 → 返回 User

    @Test
    @DisplayName("EC5 - findById: id 存在时返回对应 User")
    void testFindById_EC5_found() {
        when(userDao.findById(1)).thenReturn(mockUser);

        User result = userService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    // EC6: id > 0 但不存在 → 返回 null
    @Test
    @DisplayName("EC6 - findById: id 不存在时返回 null")
    void testFindById_EC6_notFound() {
        when(userDao.findById(999)).thenReturn(null);

        User result = userService.findById(999);

        assertNull(result);
    }

    // EC7: id = 0（边界值）→ 返回 null
    @Test
    @DisplayName("EC7 - findById: id = 0（边界值）时返回 null")
    void testFindById_EC7_zero() {
        when(userDao.findById(0)).thenReturn(null);

        User result = userService.findById(0);

        assertNull(result);
    }

    // EC8: id < 0 → 返回 null
    @Test
    @DisplayName("EC8 - findById: id 为负数时返回 null")
    void testFindById_EC8_negative() {
        when(userDao.findById(-1)).thenReturn(null);

        User result = userService.findById(-1);

        assertNull(result);
    }

    // ==================== findByUserID(Pageable) ====================
    // EC9: 存在普通用户 → 返回含数据的分页

    @Test
    @DisplayName("EC9 - findByUserID(Pageable): 存在普通用户时返回分页结果")
    void testFindByUserID_pageable_EC9_hasUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(Collections.singletonList(mockUser), pageable, 1);
        when(userDao.findAllByIsadmin(0, pageable)).thenReturn(mockPage);

        Page<User> result = userService.findByUserID(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getContent().get(0).getIsadmin());
    }

    // EC10: 无普通用户 → 返回空分页
    @Test
    @DisplayName("EC10 - findByUserID(Pageable): 无普通用户时返回空分页")
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
    // EC11: userID 正确 + password 正确 → 返回 User

    @Test
    @DisplayName("EC11 - checkLogin: 账号密码均正确时返回 User")
    void testCheckLogin_EC11_success() {
        when(userDao.findByUserIDAndPassword("user001", "123456")).thenReturn(mockUser);

        User result = userService.checkLogin("user001", "123456");

        assertNotNull(result);
        assertEquals("user001", result.getUserID());
    }

    // EC12: userID 正确 + password 错误 → 返回 null
    @Test
    @DisplayName("EC12 - checkLogin: 密码错误时返回 null")
    void testCheckLogin_EC12_wrongPassword() {
        when(userDao.findByUserIDAndPassword("user001", "wrong")).thenReturn(null);

        User result = userService.checkLogin("user001", "wrong");

        assertNull(result);
    }

    // EC13: userID 不存在 → 返回 null
    @Test
    @DisplayName("EC13 - checkLogin: 账号不存在时返回 null")
    void testCheckLogin_EC13_userNotFound() {
        when(userDao.findByUserIDAndPassword("nobody", "123456")).thenReturn(null);

        User result = userService.checkLogin("nobody", "123456");

        assertNull(result);
    }

    // EC14: password = ""（边界值）→ 返回 null
    @Test
    @DisplayName("EC14 - checkLogin: 密码为空字符串时返回 null")
    void testCheckLogin_EC14_emptyPassword() {
        when(userDao.findByUserIDAndPassword("user001", "")).thenReturn(null);

        User result = userService.checkLogin("user001", "");

        assertNull(result);
    }

    // EC15: userID = null → 返回 null
    @Test
    @DisplayName("EC15 - checkLogin: userID 为 null 时返回 null")
    void testCheckLogin_EC15_nullUserID() {
        when(userDao.findByUserIDAndPassword(null, "123456")).thenReturn(null);

        User result = userService.checkLogin(null, "123456");

        assertNull(result);
    }

    // EC16: password = null → 返回 null
    @Test
    @DisplayName("EC16 - checkLogin: password 为 null 时返回 null")
    void testCheckLogin_EC16_nullPassword() {
        when(userDao.findByUserIDAndPassword("user001", null)).thenReturn(null);

        User result = userService.checkLogin("user001", null);

        assertNull(result);
    }

    // ==================== create ====================
    // EC17: 有效 User，表中已有数据 → 返回全表总数

    @Test
    @DisplayName("EC17 - create: 表中已有数据时返回正确总数")
    void testCreate_EC17_multipleUsers() {
        List<User> allUsers = Arrays.asList(mockUser, new User());
        when(userDao.findAll()).thenReturn(allUsers);

        int result = userService.create(mockUser);

        verify(userDao, times(1)).save(mockUser);
        assertEquals(2, result);
    }

    // EC18: 有效 User，首个用户（边界值）→ 返回 1
    @Test
    @DisplayName("EC18 - create: 首个用户时返回总数 1（边界值）")
    void testCreate_EC18_firstUser() {
        when(userDao.findAll()).thenReturn(Collections.singletonList(mockUser));

        int result = userService.create(mockUser);

        verify(userDao, times(1)).save(mockUser);
        assertEquals(1, result);
    }

    // EC19: user = null → 抛出 IllegalArgumentException
    @Test
    @DisplayName("EC19 - create: user 为 null 时抛出 IllegalArgumentException")
    void testCreate_EC19_nullUser() {
        doThrow(new IllegalArgumentException("Entity must not be null"))
                .when(userDao).save(null);

        assertThrows(IllegalArgumentException.class, () -> userService.create(null));
    }

    // ==================== delByID ====================
    // EC20: id 存在 → 正常删除

    @Test
    @DisplayName("EC20 - delByID: id 存在时正常删除")
    void testDelByID_EC20_success() {
        doNothing().when(userDao).deleteById(1);

        userService.delByID(1);

        verify(userDao, times(1)).deleteById(1);
    }

    // EC21: id 不存在 → 抛出 EmptyResultDataAccessException
    @Test
    @DisplayName("EC21 - delByID: id 不存在时抛出 EmptyResultDataAccessException")
    void testDelByID_EC21_notFound() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(userDao).deleteById(999);

        assertThrows(EmptyResultDataAccessException.class, () -> userService.delByID(999));
    }

    // EC22: id < 0 → 抛出 EmptyResultDataAccessException
    @Test
    @DisplayName("EC22 - delByID: id 为负数时抛出 EmptyResultDataAccessException")
    void testDelByID_EC22_negativeId() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(userDao).deleteById(-1);

        assertThrows(EmptyResultDataAccessException.class, () -> userService.delByID(-1));
    }

    // ==================== updateUser ====================
    // EC23: 有效 User → 调用 save 保存

    @Test
    @DisplayName("EC23 - updateUser: 有效 User 时调用 save")
    void testUpdateUser_EC23_valid() {
        mockUser.setUserName("李四");

        userService.updateUser(mockUser);

        verify(userDao, times(1)).save(mockUser);
    }

    // EC24: user = null → 抛出 IllegalArgumentException
    @Test
    @DisplayName("EC24 - updateUser: user 为 null 时抛出 IllegalArgumentException")
    void testUpdateUser_EC24_nullUser() {
        doThrow(new IllegalArgumentException("Entity must not be null"))
                .when(userDao).save(null);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(null));
    }

    // ==================== countUserID ====================
    // EC25: userID 已存在 → 返回 1

    @Test
    @DisplayName("EC25 - countUserID: userID 已存在时返回 1")
    void testCountUserID_EC25_exists() {
        when(userDao.countByUserID("user001")).thenReturn(1);

        int result = userService.countUserID("user001");

        assertEquals(1, result);
    }

    // EC26: userID 不存在 → 返回 0
    @Test
    @DisplayName("EC26 - countUserID: userID 不存在时返回 0")
    void testCountUserID_EC26_notExists() {
        when(userDao.countByUserID("newUser")).thenReturn(0);

        int result = userService.countUserID("newUser");

        assertEquals(0, result);
    }

    // EC27: userID = null → 返回 0
    @Test
    @DisplayName("EC27 - countUserID: userID 为 null 时返回 0")
    void testCountUserID_EC27_null() {
        when(userDao.countByUserID(null)).thenReturn(0);

        int result = userService.countUserID(null);

        assertEquals(0, result);
    }
}
