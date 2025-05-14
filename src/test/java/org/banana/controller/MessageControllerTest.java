package org.banana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.banana.config.SecurityConfig;
import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageMarkReadRequestDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.dto.message.MessageSendRequestDto;
import org.banana.exception.ConversationNotFoundException;
import org.banana.exception.MessageSendException;
import org.banana.exception.UserNotFoundException;
import org.banana.security.service.JwtService;
import org.banana.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.banana.exception.MessageSendException.MessageSendExceptionMessage.USER_MESSAGES_THE_SAME_USER;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@Import(SecurityConfig.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private MessageService messageService;

    // --- sendMessage ---

    @Test
    @WithMockUser
    void sendMessage_whenValid_thenCreated() throws Exception {
        MessageSendRequestDto req = new MessageSendRequestDto();
        req.setAdvertisementId(UUID.randomUUID());
        req.setRecipientId(UUID.randomUUID());
        req.setMessageText("Hello");

        MessageResponseDto resp = new MessageResponseDto();
        resp.setId(UUID.randomUUID());
        when(messageService.sendMessage(req)).thenReturn(resp);

        mvc.perform(post("/api/v1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));
    }

    @Test
    @WithMockUser
    void sendMessage_whenInvalidDto_thenBadRequest() throws Exception {
        // missing recipientId and messageText
        MessageSendRequestDto req = new MessageSendRequestDto();

        mvc.perform(post("/api/v1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("recipientId")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("messageText")));
    }

    @Test
    @WithMockUser
    void sendMessage_whenServiceThrowsMessageSendException_thenConflict() throws Exception {
        MessageSendRequestDto req = new MessageSendRequestDto();
        req.setRecipientId(UUID.randomUUID());
        req.setMessageText("Hi");

        when(messageService.sendMessage(req))
                .thenThrow(new MessageSendException(USER_MESSAGES_THE_SAME_USER));

        mvc.perform(post("/api/v1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void sendMessage_whenServiceThrowsNotFoundException_thenNotFound() throws Exception {
        MessageSendRequestDto req = new MessageSendRequestDto();
        req.setRecipientId(UUID.randomUUID());
        req.setMessageText("Hello");

        when(messageService.sendMessage(req)).thenThrow(new UserNotFoundException(req.getRecipientId()));

        mvc.perform(post("/api/v1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void sendMessage_whenAnonymous_thenUnauthorized() throws Exception {
        MessageSendRequestDto req = new MessageSendRequestDto();
        req.setRecipientId(UUID.randomUUID());
        req.setMessageText("Test");

        mvc.perform(post("/api/v1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // --- getMessages ---

    @Test
    @WithMockUser
    void getMessages_whenValid_thenOk() throws Exception {
        MessageFilterDto filter = new MessageFilterDto();
        filter.setSecondUserId(UUID.randomUUID());
        when(messageService.getListOfMessages(filter)).thenReturn(List.of(new MessageResponseDto()));

        mvc.perform(post("/api/v1/message/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(new MessageResponseDto()))));
    }

    @Test
    @WithMockUser
    void getMessages_whenInvalidCursorPair_thenBadRequest() throws Exception {
        MessageFilterDto filter = new MessageFilterDto();
        filter.setSecondUserId(UUID.randomUUID());
        filter.setCursorDateTime(LocalDateTime.now());
        // cursorMessageId null -> invalid

        mvc.perform(post("/api/v1/message/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void getMessages_whenAnonymous_thenUnauthorized() throws Exception {
        MessageFilterDto filter = new MessageFilterDto();
        filter.setSecondUserId(UUID.randomUUID());

        mvc.perform(post("/api/v1/message/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isUnauthorized());
    }

    // --- markReadUpTo ---

    @Test
    @WithMockUser
    void markReadUpTo_whenValid_thenNoContent() throws Exception {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(UUID.randomUUID());
        dto.setUpToDateTime(LocalDateTime.now());

        mvc.perform(patch("/api/v1/message/mark-read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }


    @Test
    @WithMockUser
    void markReadUpTo_whenServiceThrowsMessageSendException_thenConflict() throws Exception {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(UUID.randomUUID());
        dto.setUpToDateTime(LocalDateTime.now());

        doThrow(new MessageSendException(USER_MESSAGES_THE_SAME_USER)).when(messageService).markReadUpTo(dto);

        mvc.perform(patch("/api/v1/message/mark-read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void markReadUpTo_whenMissingUpToDateTime_thenBadRequest() throws Exception {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(UUID.randomUUID());
        // missing upToDateTime

        mvc.perform(patch("/api/v1/message/mark-read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("upToDateTime")));
    }

    @Test
    @WithMockUser
    void markReadUpTo_whenConversationNotFound_thenNotFound() throws Exception {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(UUID.randomUUID());
        dto.setUpToDateTime(LocalDateTime.now());

        doThrow(new ConversationNotFoundException(dto.getSecondUserId(), dto.getAdvertisementId()))
                .when(messageService).markReadUpTo(dto);

        mvc.perform(patch("/api/v1/message/mark-read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void markReadUpTo_whenAnonymous_thenUnauthorized() throws Exception {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(UUID.randomUUID());
        dto.setUpToDateTime(LocalDateTime.now());

        mvc.perform(patch("/api/v1/message/mark-read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}