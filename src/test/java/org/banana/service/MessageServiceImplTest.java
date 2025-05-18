package org.banana.service;

import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageMapper;
import org.banana.dto.message.MessageMarkReadRequestDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.dto.message.MessageSendRequestDto;
import org.banana.entity.Advertisement;
import org.banana.entity.Message;
import org.banana.entity.User;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.ConversationNotFoundException;
import org.banana.exception.MessageSendException;
import org.banana.exception.UserNotFoundException;
import org.banana.repository.AdvertisementRepository;
import org.banana.repository.MessageRepository;
import org.banana.repository.UserRepository;
import org.banana.security.UserRole;
import org.banana.security.dto.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.banana.exception.MessageSendException.MessageSendExceptionMessage.OWNER_OF_THE_ADVERTISEMENT_CANT_MESSAGE_FIRST;
import static org.banana.exception.MessageSendException.MessageSendExceptionMessage.RECIPIENT_IS_NOT_OWNER_OF_THE_ADVERTISEMENT;
import static org.banana.exception.MessageSendException.MessageSendExceptionMessage.USER_MESSAGES_THE_SAME_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    private final UUID currentUserId = UUID.randomUUID();
    private final UUID recipientId = UUID.randomUUID();
    private final UUID advertisementId = UUID.randomUUID();

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    @BeforeEach
    void setupSecurityContext() {
        var principal = new UserPrincipal(currentUserId, "user", "123", "phone", "username", "password", UserRole.ROLE_USER);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sendMessage_whenSenderEqualsRecipient_thenThrowMessageAddException() {
        MessageSendRequestDto request = new MessageSendRequestDto();
        request.setRecipientId(currentUserId);

        MessageSendException exception = assertThrows(MessageSendException.class,
                () -> messageService.addMessage(request));

        assertThat(exception.getMessage()).contains(USER_MESSAGES_THE_SAME_USER.getDescription());
    }

    @Test
    void addMessage_whenRecipientNotFound_thenThrowUserNotFoundException() {
        MessageSendRequestDto request = new MessageSendRequestDto();
        request.setRecipientId(recipientId);

        when(userRepository.existsById(recipientId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> messageService.addMessage(request));
    }

    @Test
    void addMessage_whenAdvertisementNotExist_thenThrowAdvertisementNotFoundException() {
        MessageSendRequestDto request = new MessageSendRequestDto();
        request.setRecipientId(recipientId);
        request.setAdvertisementId(advertisementId);

        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class, () -> messageService.addMessage(request));
    }

    @Test
    void sendMessage_whenNeitherSenderNorRecipientIsOwner_thenThrowMessageAddException() {
        MessageSendRequestDto request = new MessageSendRequestDto();
        request.setRecipientId(recipientId);
        request.setAdvertisementId(advertisementId);

        User adOwner = new User(UUID.randomUUID(), "owner", "last", "phone", "email", "pass", UserRole.ROLE_USER);
        Advertisement ad = new Advertisement();
        ad.setUser(adOwner);

        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(ad));

        MessageSendException exception = assertThrows(MessageSendException.class,
                () -> messageService.addMessage(request));

        assertThat(exception.getMessage()).contains(RECIPIENT_IS_NOT_OWNER_OF_THE_ADVERTISEMENT.getDescription());
    }

    @Test
    void sendMessage_whenSenderIsOwnerAndChatNotExist_thenThrowMessageAddException() {
        User adOwner = new User(currentUserId, "owner", "last", "phone", "email", "pass", UserRole.ROLE_USER);
        Advertisement ad = new Advertisement();
        ad.setUser(adOwner);

        MessageSendRequestDto request = new MessageSendRequestDto();
        request.setRecipientId(recipientId);
        request.setAdvertisementId(advertisementId);

        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(ad));
        when(messageRepository.chatExists(currentUserId, recipientId, advertisementId)).thenReturn(false);

        MessageSendException exception = assertThrows(MessageSendException.class, () -> messageService.addMessage(request));

        assertTrue(exception.getMessage().contains(OWNER_OF_THE_ADVERTISEMENT_CANT_MESSAGE_FIRST.getDescription()));
    }

    @Test
    void addMessage_whenSenderIsOwnerAndChatExist_thenReturnMessageResponseDto() {
        // Setup sender as ad owner
        User adOwner = new User(currentUserId, "owner", "last", "phone", "email", "pass", UserRole.ROLE_USER);
        Advertisement ad = new Advertisement();
        ad.setId(advertisementId);
        ad.setUser(adOwner);

        MessageSendRequestDto request = new MessageSendRequestDto();
        request.setRecipientId(recipientId);
        request.setAdvertisementId(advertisementId);

        Message savedMessage = new Message();
        MessageResponseDto expectedDto = new MessageResponseDto();

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(ad));
        when(messageRepository.chatExists(currentUserId, recipientId, advertisementId)).thenReturn(true);
        when(messageRepository.save(messageArgumentCaptor.capture())).thenReturn(savedMessage);
        when(messageMapper.messageToMessageResponseDto(savedMessage)).thenReturn(expectedDto);

        MessageResponseDto result = messageService.addMessage(request);
        verify(messageRepository).markAllMessagesRead(recipientId, currentUserId, advertisementId);
        assertEquals(currentUserId, messageArgumentCaptor.getValue().getSenderId());
        assertEquals(recipientId, messageArgumentCaptor.getValue().getRecipientId());
        assertEquals(advertisementId, messageArgumentCaptor.getValue().getAdvertisementId());
        assertNotNull(result);
    }

    @Test
    void addMessage_whenSenderIsNotOwnerAndRecipientIsOwner_thenReturnMessageResponseDto() {
        User adOwner = new User(recipientId, "owner", "last", "phone", "email", "pass", UserRole.ROLE_USER);
        Advertisement ad = new Advertisement();
        ad.setUser(adOwner);

        MessageSendRequestDto request = new MessageSendRequestDto();
        request.setRecipientId(adOwner.getId());
        request.setAdvertisementId(advertisementId);

        Message savedMessage = new Message();
        MessageResponseDto expectedDto = new MessageResponseDto();

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        when(userRepository.existsById(adOwner.getId())).thenReturn(true);
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(ad));
        when(messageRepository.save(messageArgumentCaptor.capture())).thenReturn(savedMessage);
        when(messageMapper.messageToMessageResponseDto(savedMessage)).thenReturn(expectedDto);

        MessageResponseDto result = messageService.addMessage(request);
        verify(messageRepository).markAllMessagesRead(recipientId, currentUserId, advertisementId);
        assertEquals(currentUserId, messageArgumentCaptor.getValue().getSenderId());
        assertEquals(recipientId, messageArgumentCaptor.getValue().getRecipientId());
        assertEquals(advertisementId, messageArgumentCaptor.getValue().getAdvertisementId());
        assertNotNull(result);
    }


    @Test
    void addMessage_whenNoAdId_thenReturnMessageResponseDto() {
        User adOwner = new User(recipientId, "owner", "last", "phone", "email", "pass", UserRole.ROLE_USER);
        Advertisement ad = new Advertisement();
        ad.setUser(adOwner);

        MessageSendRequestDto request = new MessageSendRequestDto();
        request.setRecipientId(adOwner.getId());

        Message savedMessage = new Message();
        MessageResponseDto expectedDto = new MessageResponseDto();

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        when(userRepository.existsById(adOwner.getId())).thenReturn(true);
        when(messageRepository.save(messageArgumentCaptor.capture())).thenReturn(savedMessage);
        when(messageMapper.messageToMessageResponseDto(savedMessage)).thenReturn(expectedDto);

        MessageResponseDto result = messageService.addMessage(request);
        verify(messageRepository, never()).chatExists(any(), any(), any());
        verify(advertisementRepository, never()).findById(any());
        assertEquals(currentUserId, messageArgumentCaptor.getValue().getSenderId());
        assertEquals(recipientId, messageArgumentCaptor.getValue().getRecipientId());
        assertNull(messageArgumentCaptor.getValue().getAdvertisementId());
        assertNotNull(result);
    }


    @Test
    void markReadUpTo_whenSecondUserSameAsSender_thenThrowMessageSendException() {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(currentUserId);

        MessageSendException messageSendException = assertThrows(MessageSendException.class, () -> messageService.markReadUpTo(dto));

        assertTrue(messageSendException.getMessage().contains(USER_MESSAGES_THE_SAME_USER.getDescription()));
    }

    @Test
    void markReadUpTo_whenSecondUserNotFound_thenThrowUserNotFoundException() {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(recipientId);

        when(userRepository.existsById(recipientId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> messageService.markReadUpTo(dto));
    }

    @Test
    void markReadUpTo_whenAdvertisementNotExist_thenThrowAdvertisementNotFoundException() {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(recipientId);
        dto.setAdvertisementId(advertisementId);

        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(advertisementRepository.existsById(advertisementId)).thenReturn(false);

        assertThrows(AdvertisementNotFoundException.class, () -> messageService.markReadUpTo(dto));
    }

    @Test
    void markReadUpTo_whenChatNotExist_thenThrowConversationNotFoundException() {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(recipientId);

        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(messageRepository.chatExists(any(), any(), any())).thenReturn(false);

        assertThrows(ConversationNotFoundException.class, () -> messageService.markReadUpTo(dto));
    }

    @Test
    void markReadUpTo_whenValidRequestAndAdIsNull_thenCallMarkMessagesReadUpTo() {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(recipientId);
        dto.setUpToDateTime(LocalDateTime.now());
        dto.setUpToMessageId(UUID.randomUUID());

        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(messageRepository.chatExists(currentUserId, recipientId, null)).thenReturn(true);

        messageService.markReadUpTo(dto);

        verify(advertisementRepository, never()).existsById(any());
        verify(messageRepository).markMessagesReadUpTo(recipientId, currentUserId, null, dto.getUpToDateTime(), dto.getUpToMessageId());
    }

    @Test
    void markReadUpTo_whenValidRequestAndAdIsNotNull_thenCallMarkMessagesReadUpTo() {
        MessageMarkReadRequestDto dto = new MessageMarkReadRequestDto();
        dto.setSecondUserId(recipientId);
        dto.setAdvertisementId(advertisementId);
        dto.setUpToDateTime(LocalDateTime.now());
        dto.setUpToMessageId(UUID.randomUUID());

        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(messageRepository.chatExists(currentUserId, recipientId, advertisementId)).thenReturn(true);
        when(advertisementRepository.existsById(advertisementId)).thenReturn(true);

        messageService.markReadUpTo(dto);

        verify(messageRepository).markMessagesReadUpTo(recipientId, currentUserId, advertisementId, dto.getUpToDateTime(), dto.getUpToMessageId());
    }

    @Test
    void getListOfMessages_whenSecondUserIsSameAsCurrentUser_thenThrowMessageSendException() {
        MessageFilterDto filter = new MessageFilterDto();
        filter.setSecondUserId(currentUserId);

        assertThrows(MessageSendException.class, () -> messageService.getListOfMessages(filter));
    }

    @Test
    void getListOfMessages_whenSecondUserNotFound_thenThrowUserNotFoundException() {
        MessageFilterDto filter = new MessageFilterDto();
        filter.setSecondUserId(recipientId);

        when(userRepository.existsById(recipientId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> messageService.getListOfMessages(filter));
    }

    @Test
    void getListOfMessages_whenCursorNull_thenSetUnreadCount() {
        MessageFilterDto filter = new MessageFilterDto();
        filter.setSecondUserId(recipientId);

        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(messageRepository.getUnreadMessagesCount(recipientId, currentUserId, null))
                .thenReturn(5L);

        messageService.getListOfMessages(filter);

        assertEquals(5L, filter.getUnreadMessagesCount());
        verify(messageRepository).getUnreadMessagesCount(recipientId, currentUserId, null);
        verify(messageRepository).findAllByFilter(filter);
    }

    @Test
    void getListOfMessages_whenCursorNotNull_thenReturnList() {
        MessageFilterDto filter = new MessageFilterDto();
        filter.setSecondUserId(recipientId);
        filter.setCursorMessageId(UUID.randomUUID());
        filter.setCursorDateTime(LocalDateTime.now());

        when(userRepository.existsById(recipientId)).thenReturn(true);
        when(messageRepository.findAllByFilter(filter)).thenReturn(List.of());

        List<MessageResponseDto> result = messageService.getListOfMessages(filter);

        assertNotNull(result);
        verify(messageRepository, never()).getUnreadMessagesCount(any(), any(), any());
        verify(messageRepository).findAllByFilter(filter);
    }
}