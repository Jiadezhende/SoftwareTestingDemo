package com.demo.integration;

import com.demo.controller.user.MessageController;
import com.demo.controller.user.OrderController;
import com.demo.controller.user.UserController;
import com.demo.controller.user.VenueController;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.exception.LoginException;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.UserService;
import com.demo.service.VenueService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        MessageController.class
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
        if (!(ex.getCause() instanceof LoginException)) {
            throw new AssertionError("Expected LoginException");
        }
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
        if (saved.getState() != 1) {
            throw new AssertionError("Expected default state 1");
        }
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
        if (!"u9999".equals(saved.getUserID())) {
            throw new AssertionError("Expected forged userID to be persisted");
        }
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
        if (!"u2002".equals(saved.getUserID())) {
            throw new AssertionError("Expected request userID to be trusted by current implementation");
        }
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
        if (!(ex.getCause() instanceof LoginException)) {
            throw new AssertionError("Expected LoginException");
        }
    }
}
