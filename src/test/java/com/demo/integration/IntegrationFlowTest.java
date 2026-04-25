package com.demo.integration;

import com.demo.controller.user.MessageController;
import com.demo.controller.user.NewsController;
import com.demo.controller.user.OrderController;
import com.demo.controller.user.UserController;
import com.demo.controller.user.VenueController;
import com.demo.controller.IndexController;
import com.demo.controller.admin.AdminMessageController;
import com.demo.controller.admin.AdminNewsController;
import com.demo.controller.admin.AdminOrderController;
import com.demo.controller.admin.AdminUserController;
import com.demo.controller.admin.AdminVenueController;
import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.entity.vo.MessageVo;
import com.demo.entity.vo.OrderVo;
import com.demo.exception.LoginException;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.service.NewsService;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.UserService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.util.NestedServletException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {
        IndexController.class,
        UserController.class,
        OrderController.class,
        VenueController.class,
        MessageController.class,
        NewsController.class,
        AdminOrderController.class,
        AdminMessageController.class,
        AdminNewsController.class,
        AdminUserController.class,
        AdminVenueController.class
})
class IntegrationFlowTest {

    @Autowired
    private MockMvc mockMvc;

        private MvcResult performAndReturn(org.springframework.test.web.servlet.RequestBuilder requestBuilder) throws Exception {
                return mockMvc.perform(requestBuilder).andReturn();
        }

    @MockBean
    private UserService userService;
    @MockBean
    private OrderService orderService;
    @MockBean
    private OrderVoService orderVoService;
    @MockBean
    private VenueService venueService;
    @MockBean
    private MessageService messageService;
    @MockBean
    private MessageVoService messageVoService;
    @MockBean
    private NewsService newsService;

    @Test
    @DisplayName("IT-INT-001 - loginCheck: 用户名密码正确时登录成功并写入 Session")
    void testLogin_success() throws Exception {
        User user = new User();
        user.setUserID("u1001");
        user.setIsadmin(0);
        when(userService.checkLogin("u1001", "pw123")).thenReturn(user);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "u1001")
                        .param("password", "pw123"))
                .andExpect(status().isOk())
                .andExpect(content().string("/index"))
                .andExpect(request().sessionAttribute("user", user));
    }

    @Test
    @DisplayName("IT-INT-002 - loginCheck: 密码错误时返回 false")
    void testLogin_wrongPassword() throws Exception {
        when(userService.checkLogin("u1001", "bad")).thenReturn(null);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "u1001")
                        .param("password", "bad"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("IT-INT-003 - order_manage: 未登录访问时抛出 LoginException")
    void testOrderManage_noLogin() throws Exception {
        NestedServletException ex = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/order_manage")));
                assertTrue(ex.getCause() instanceof LoginException);
    }

    @Test
    @DisplayName("IT-INT-004 - addOrder: 已登录用户提交合法预约时重定向并调用 submit")
    void testAddOrder_success() throws Exception {
        User loginUser = new User();
        loginUser.setUserID("u1001");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", loginUser);

        mockMvc.perform(post("/addOrder.do")
                        .session(session)
                        .param("venueName", "羽毛球馆A")
                        .param("date", "2026-04-08")
                .param("startTime", "2026-04-08 10:00")
                        .param("hours", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        verify(orderService, times(1)).submit(anyString(), any(LocalDateTime.class), anyInt(), anyString());
    }

    @Test
    @DisplayName("IT-INT-005 - delOrder: [BUG-017] 未登录可直接删除订单（鉴权缺失漏洞）")
    void testDelOrder_noLogin_vulnerability() throws Exception {
        mockMvc.perform(post("/delOrder.do")
                        .param("orderID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService, times(1)).delOrder(1);
    }

    @Test
    @DisplayName("IT-INT-006 - venuelist/getVenueList: 返回分页场馆列表 JSON")
    void testVenueList_success() throws Exception {
        Venue venue = new Venue();
        venue.setVenueID(1);
        venue.setVenueName("羽毛球馆A");
        when(venueService.findAll(any())).thenReturn(new PageImpl<>(Collections.singletonList(venue)));

        mockMvc.perform(get("/venuelist/getVenueList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].venueName").value("羽毛球馆A"));
    }

    @Test
    @DisplayName("IT-INT-007 - venue: 场馆详情页渲染正确场馆信息")
    void testVenueDetail_success() throws Exception {
        Venue venue = new Venue();
        venue.setVenueID(1);
        venue.setVenueName("羽毛球馆A");
        when(venueService.findByVenueID(1)).thenReturn(venue);

        mockMvc.perform(get("/venue").param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("羽毛球馆A")));
    }

    @Test
    @DisplayName("IT-INT-008 - index: 首页聚合渲染场馆、新闻、留言数据")
    void testIndexPage_success() throws Exception {
                Venue venue = new Venue();
                venue.setVenueID(1);
                venue.setVenueName("羽毛球馆A");

                News news = new News();
                news.setNewsID(10);
                news.setTitle("系统公告");

                Message message = new Message();
                message.setMessageID(7);
                message.setUserID("u1001");

                MessageVo messageVo = new MessageVo();
                when(venueService.findAll(any())).thenReturn(new PageImpl<>(Collections.singletonList(venue)));
                when(newsService.findAll(any())).thenReturn(new PageImpl<>(Collections.singletonList(news)));
                when(messageService.findPassState(any())).thenReturn(new PageImpl<>(Collections.singletonList(message)));
                when(messageVoService.returnVo(any())).thenReturn(Collections.singletonList(messageVo));

                MvcResult result = performAndReturn(get("/index"));
                String content = result.getResponse().getContentAsString();
                assertEquals(200, result.getResponse().getStatus());
                assertTrue(content.contains("羽毛球馆A"));
                assertTrue(content.contains("系统公告"));
    }

    @Test
    @DisplayName("IT-INT-009 - admin_index: 管理员首页正常渲染")
    void testAdminIndex_success() throws Exception {
                        MvcResult result = performAndReturn(get("/admin_index"));
                        String content = result.getResponse().getContentAsString();
                        assertEquals(200, result.getResponse().getStatus());
                        assertTrue(content.contains("管理系统"));
    }

    @Test
    @DisplayName("IT-INT-010 - reservation_manage: 管理员预约管理页正常渲染")
    void testAdminReservationManage_success() throws Exception {
                Order order = new Order();
                OrderVo orderVo = new OrderVo();
                when(orderService.findAuditOrder()).thenReturn(Collections.singletonList(order));
                when(orderVoService.returnVo(any())).thenReturn(Collections.singletonList(orderVo));
                when(orderService.findNoAuditOrder(any())).thenReturn(new PageImpl<>(Collections.singletonList(order)));

                MvcResult result = performAndReturn(get("/reservation_manage"));
                String content = result.getResponse().getContentAsString();
                assertEquals(200, result.getResponse().getStatus());
                assertTrue(content.contains("reservation"));
    }

    @Test
    @DisplayName("IT-INT-011 - admin/getOrderList.do: 返回待审核订单 JSON 列表")
    void testAdminGetOrderList_success() throws Exception {
                Order order = new Order();
                OrderVo orderVo = new OrderVo();
                when(orderService.findNoAuditOrder(any())).thenReturn(new PageImpl<>(Collections.singletonList(order)));
                when(orderVoService.returnVo(any())).thenReturn(Collections.singletonList(orderVo));

                mockMvc.perform(get("/admin/getOrderList.do").param("page", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("IT-INT-012 - message_manage: 管理员留言管理页正常渲染")
    void testAdminMessageManage_success() throws Exception {
                Message message = new Message();
                when(messageService.findWaitState(any())).thenReturn(new PageImpl<>(Collections.singletonList(message)));

                MvcResult result = performAndReturn(get("/message_manage"));
                String content = result.getResponse().getContentAsString();
                assertEquals(200, result.getResponse().getStatus());
                assertTrue(content.contains("message"));
    }

    @Test
    @DisplayName("IT-INT-013 - messageList.do: 返回待审核留言 JSON 列表")
    void testAdminMessageList_success() throws Exception {
                Message message = new Message();
                MessageVo messageVo = new MessageVo();
                when(messageService.findWaitState(any())).thenReturn(new PageImpl<>(Collections.singletonList(message)));
                when(messageVoService.returnVo(any())).thenReturn(Collections.singletonList(messageVo));

                mockMvc.perform(get("/messageList.do").param("page", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("IT-INT-014 - news_manage: 管理员新闻管理页正常渲染")
    void testAdminNewsManage_success() throws Exception {
                News news = new News();
                when(newsService.findAll(any())).thenReturn(new PageImpl<>(Collections.singletonList(news)));

                MvcResult result = performAndReturn(get("/news_manage"));
                String content = result.getResponse().getContentAsString();
                assertEquals(200, result.getResponse().getStatus());
                assertTrue(content.contains("news"));
    }

    @Test
    @DisplayName("IT-INT-015 - userList.do: 返回用户列表 JSON")
    void testAdminUserList_success() throws Exception {
                User user = new User();
                user.setUserID("u1001");
                when(userService.findByUserID(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(user)));

                mockMvc.perform(get("/userList.do").param("page", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].userID").value("u1001"));
    }

    @Test
    @DisplayName("IT-INT-016 - checkUserID.do: 未注册 userID 时返回 true（可用）")
    void testAdminCheckUserId_success() throws Exception {
                when(userService.countUserID("u1001")).thenReturn(0);

                MvcResult result = performAndReturn(post("/checkUserID.do").param("userID", "u1001"));
                assertEquals(200, result.getResponse().getStatus());
                assertEquals("true", result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("IT-INT-017 - venueList.do: 返回场馆列表 JSON")
    void testAdminVenueList_success() throws Exception {
                Venue v1 = new Venue();
                v1.setVenueID(1);
                v1.setVenueName("A馆");
                Venue v2 = new Venue();
                v2.setVenueID(2);
                v2.setVenueName("B馆");
                when(venueService.findAll(any())).thenReturn(new PageImpl<>(Arrays.asList(v1, v2)));

                mockMvc.perform(get("/venueList.do").param("page", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[0].venueID").value(1))
                                .andExpect(jsonPath("$[1].venueID").value(2));
    }

    @Test
    @DisplayName("IT-INT-018 - checkVenueName.do: 未注册 venueName 时返回 true（可用）")
    void testAdminCheckVenueName_success() throws Exception {
                when(venueService.countVenueName("羽毛球馆A")).thenReturn(0);

                MvcResult result = performAndReturn(post("/checkVenueName.do").param("venueName", "羽毛球馆A"));
                assertEquals(200, result.getResponse().getStatus());
                assertEquals("true", result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("IT-INT-019 - sendMessage: 已登录用户发送留言时重定向并持久化默认状态")
    void testSendMessage_success() throws Exception {
        mockMvc.perform(post("/sendMessage")
                        .param("userID", "u1001")
                        .param("content", "测试留言"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageService, times(1)).create(captor.capture());
        Message saved = captor.getValue();
        assertEquals(1, saved.getState(), "Expected default state 1");
    }

    @Test
    @DisplayName("IT-INT-020 - sendMessage: [BUG-018] 未登录可提交留言并入库（鉴权缺失漏洞）")
    void testSendMessage_noLogin_vulnerability() throws Exception {
        mockMvc.perform(post("/sendMessage")
                        .param("userID", "u9999")
                        .param("content", "匿名越权留言"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageService, times(1)).create(captor.capture());
        Message saved = captor.getValue();
        assertEquals("u9999", saved.getUserID(), "Expected forged userID to be persisted");
    }

    @Test
    @DisplayName("IT-INT-021 - sendMessage: [BUG-019] 已登录用户可伪造他人 userID 提交留言（身份校验缺失漏洞）")
    void testSendMessage_forgedUserId_vulnerability() throws Exception {
        User loginUser = new User();
        loginUser.setUserID("u1001");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", loginUser);

        mockMvc.perform(post("/sendMessage")
                        .session(session)
                        .param("userID", "u2002")
                        .param("content", "伪造身份留言"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageService, times(1)).create(captor.capture());
        Message saved = captor.getValue();
        assertEquals("u2002", saved.getUserID(), "Expected request userID to be trusted by current implementation");
    }

    @Test
    @DisplayName("IT-INT-022 - delMessage.do: [BUG-020] 未登录可直接删除留言（鉴权缺失漏洞）")
    void testDelMessage_noLogin_vulnerability() throws Exception {
        mockMvc.perform(post("/delMessage.do")
                        .param("messageID", "11"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService, times(1)).delById(11);
    }

    @Test
    @DisplayName("IT-INT-023 - message/findUserList: 未登录访问时抛出 LoginException")
    void testFindUserMessage_noLogin() throws Exception {
        NestedServletException ex = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/message/findUserList").param("page", "1")));
                assertTrue(ex.getCause() instanceof LoginException);
    }

    @Test
    @DisplayName("IT-INT-024 - addOrder: [BUG-028] hours<=0 时应抛出业务异常")
    void testAddOrder_nonPositiveHours() {
        User loginUser = new User();
        loginUser.setUserID("u1001");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", loginUser);

        assertThrows(Exception.class, () -> mockMvc.perform(post("/addOrder.do")
                .session(session)
                .param("venueName", "羽毛球馆A")
                .param("date", "2026-04-21")
                .param("startTime", "2026-04-21 10:00")
                .param("hours", "0")));

        verify(orderService, times(0)).submit(anyString(), any(LocalDateTime.class), anyInt(), anyString());
    }

    @Test
    @DisplayName("IT-INT-025 - passOrder: [BUG-029] 非法前置状态时应抛出业务异常")
    void testPassOrder_illegalState() {
        assertThrows(Exception.class, () -> mockMvc.perform(post("/passOrder.do")
                .param("orderID", "1")));

        verify(orderService, times(0)).confirmOrder(1);
    }

    @Test
    @DisplayName("IT-INT-026 - rejectOrder: [BUG-030] 非法前置状态时应抛出业务异常")
    void testRejectOrder_illegalState() {
        assertThrows(Exception.class, () -> mockMvc.perform(post("/rejectOrder.do")
                .param("orderID", "1")));

        verify(orderService, times(0)).rejectOrder(1);
    }

    @Test
    @Disabled("未实现：confirmMessage/rejectMessage 应拒绝已处理(STATE_PASS/STATE_REJECT)留言")
    @DisplayName("IT-INT-027 - passMessage/rejectMessage: 非法前置状态时应抛出业务异常")
    void testPassOrRejectMessage_illegalState() {
        assertThrows(Exception.class, () -> mockMvc.perform(post("/passMessage.do")
                .param("messageID", "2")));
        assertThrows(Exception.class, () -> mockMvc.perform(post("/rejectMessage.do")
                .param("messageID", "2")));

        verify(messageService, times(0)).confirmMessage(2);
        verify(messageService, times(0)).rejectMessage(2);
    }

    @Test
    @Disabled("BUG-031/BUG-032: 留言 content 为 null 或空字符串可被持久化，待修复后启用")
    @DisplayName("IT-INT-028 - sendMessage: content 为空时应抛出业务异常")
    void testSendMessage_emptyContent() {
        assertThrows(Exception.class, () -> mockMvc.perform(post("/sendMessage")
                .param("userID", "u1001")
                .param("content", "")));

        verify(messageService, times(0)).create(any(Message.class));
    }

    @Test
    @Disabled("BUG-021/BUG-022: 新闻空 title/content 可被持久化，待修复后启用")
    @DisplayName("IT-INT-029 - addNews: title 或 content 为空时应抛出业务异常")
    void testAddNews_emptyTitleOrContent() {
        assertThrows(Exception.class, () -> mockMvc.perform(post("/addNews.do")
                .param("title", "")
                .param("content", "有效内容")));

        assertThrows(Exception.class, () -> mockMvc.perform(post("/addNews.do")
                .param("title", "有效标题")
                .param("content", "")));

        verify(newsService, times(0)).create(any());
    }
}
