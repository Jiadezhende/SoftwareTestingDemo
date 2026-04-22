package com.demo.integration;

import com.demo.controller.user.MessageController;
import com.demo.controller.user.NewsController;
import com.demo.controller.user.OrderController;
import com.demo.controller.user.UserController;
import com.demo.controller.user.VenueController;
import com.demo.controller.admin.AdminMessageController;
import com.demo.controller.admin.AdminNewsController;
import com.demo.controller.admin.AdminOrderController;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.Venue;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.util.NestedServletException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {
        UserController.class,
        OrderController.class,
        VenueController.class,
        MessageController.class,
        NewsController.class,
        AdminOrderController.class,
        AdminMessageController.class,
        AdminNewsController.class
})
class IntegrationFlowTest {

    @Autowired
    private MockMvc mockMvc;

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
    void testLoginSuccessShouldWriteUserSession() throws Exception {
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
    void testLoginWrongPasswordShouldReturnFalse() throws Exception {
        when(userService.checkLogin("u1001", "bad")).thenReturn(null);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "u1001")
                        .param("password", "bad"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testOrderManageWithoutLoginShouldThrowLoginException() throws Exception {
        NestedServletException ex = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/order_manage")));
                assertTrue(ex.getCause() instanceof LoginException);
    }

    @Test
    void testAddOrderWithLoginShouldRedirectAndInvokeSubmit() throws Exception {
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
    void testDeleteOrderWithoutLoginShouldStillInvokeDelete_Vulnerability() throws Exception {
        mockMvc.perform(post("/delOrder.do")
                        .param("orderID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService, times(1)).delOrder(1);
    }

    @Test
    void testVenueListShouldReturnPagedData() throws Exception {
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
    void testVenueDetailShouldRenderVenuePage() throws Exception {
        Venue venue = new Venue();
        venue.setVenueID(1);
        venue.setVenueName("羽毛球馆A");
        when(venueService.findByVenueID(1)).thenReturn(venue);

        mockMvc.perform(get("/venue").param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("羽毛球馆A")));
    }

    @Test
    void testSendMessageShouldRedirectAndPersistDefaultState() throws Exception {
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
    void testSendMessageWithoutLoginShouldStillPersist_Vulnerability() throws Exception {
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
    void testSendMessageWithForgedUserIdShouldPersistForgedIdentity_Vulnerability() throws Exception {
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
    void testDeleteMessageWithoutLoginShouldStillInvokeDelete_Vulnerability() throws Exception {
        mockMvc.perform(post("/delMessage.do")
                        .param("messageID", "11"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService, times(1)).delById(11);
    }

    @Test
    void testFindUserMessageWithoutLoginShouldThrowLoginException() throws Exception {
        NestedServletException ex = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/message/findUserList").param("page", "1")));
                assertTrue(ex.getCause() instanceof LoginException);
    }

    @Test
    @Disabled("未实现：submit 应拒绝 hours<=0")
    @DisplayName("IT-BB-01 - addOrder: hours<=0 时应抛出业务异常")
    void testAddOrder_NonPositiveHours_ShouldBeRejected() {
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
    @Disabled("未实现：confirmOrder 应拒绝已通过(STATE_WAIT/FINISH/REJECT)订单")
    @DisplayName("IT-BB-02 - passOrder: 非法前置状态时应抛出业务异常")
    void testPassOrder_IllegalState_ShouldBeRejected() {
        assertThrows(Exception.class, () -> mockMvc.perform(post("/passOrder.do")
                .param("orderID", "1")));

        verify(orderService, times(0)).confirmOrder(1);
    }

    @Test
    @Disabled("未实现：rejectOrder 应拒绝已通过(STATE_WAIT/STATE_FINISH)订单")
    @DisplayName("IT-BB-03 - rejectOrder: 非法前置状态时应抛出业务异常")
    void testRejectOrder_IllegalState_ShouldBeRejected() {
        assertThrows(Exception.class, () -> mockMvc.perform(post("/rejectOrder.do")
                .param("orderID", "1")));

        verify(orderService, times(0)).rejectOrder(1);
    }

    @Test
    @Disabled("未实现：confirmMessage/rejectMessage 应拒绝已处理(STATE_PASS/STATE_REJECT)留言")
    @DisplayName("IT-BB-04 - passMessage/rejectMessage: 非法前置状态时应抛出业务异常")
    void testPassOrRejectMessage_IllegalState_ShouldBeRejected() {
        assertThrows(Exception.class, () -> mockMvc.perform(post("/passMessage.do")
                .param("messageID", "2")));
        assertThrows(Exception.class, () -> mockMvc.perform(post("/rejectMessage.do")
                .param("messageID", "2")));

        verify(messageService, times(0)).confirmMessage(2);
        verify(messageService, times(0)).rejectMessage(2);
    }

    @Test
    @Disabled("未实现：create(content) 应拒绝空内容")
    @DisplayName("IT-BB-05 - sendMessage: content 为空时应抛出业务异常")
    void testSendMessage_EmptyContent_ShouldBeRejected() {
        assertThrows(Exception.class, () -> mockMvc.perform(post("/sendMessage")
                .param("userID", "u1001")
                .param("content", "")));

        verify(messageService, times(0)).create(any(Message.class));
    }

    @Test
    @Disabled("未实现：news.create(title/content) 应拒绝空值")
    @DisplayName("IT-BB-06 - addNews: title 或 content 为空时应抛出业务异常")
    void testAddNews_EmptyTitleOrContent_ShouldBeRejected() {
        assertThrows(Exception.class, () -> mockMvc.perform(post("/addNews.do")
                .param("title", "")
                .param("content", "有效内容")));

        assertThrows(Exception.class, () -> mockMvc.perform(post("/addNews.do")
                .param("title", "有效标题")
                .param("content", "")));

        verify(newsService, times(0)).create(any());
    }
}
