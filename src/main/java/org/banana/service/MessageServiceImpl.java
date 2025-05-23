package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageMapper;
import org.banana.dto.message.MessageMarkReadRequestDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.dto.message.MessageSendRequestDto;
import org.banana.entity.Advertisement;
import org.banana.entity.Message;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.ConversationNotFoundException;
import org.banana.exception.MessageSendException;
import org.banana.exception.UserNotFoundException;
import org.banana.repository.AdvertisementRepository;
import org.banana.repository.MessageRepository;
import org.banana.repository.UserRepository;
import org.banana.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.banana.exception.MessageSendException.MessageSendExceptionMessage.OWNER_OF_THE_ADVERTISEMENT_CANT_MESSAGE_FIRST;
import static org.banana.exception.MessageSendException.MessageSendExceptionMessage.RECIPIENT_IS_NOT_OWNER_OF_THE_ADVERTISEMENT;
import static org.banana.exception.MessageSendException.MessageSendExceptionMessage.USER_MESSAGES_THE_SAME_USER;


@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public MessageResponseDto addMessage(MessageSendRequestDto requestDto) {
        log.info("addMessage({}) in {}", requestDto, getClass().getSimpleName());
        UUID senderId = SecurityUtils.getCurrentUserPrincipal().getId();
        UUID recipientId = requestDto.getRecipientId();
        UUID advertisementId = requestDto.getAdvertisementId();

        validateRecipient(senderId, recipientId);
        validateAdvertisementRules(senderId, recipientId, advertisementId);

        // Помечаем все непрочитанные текущим пользователем сообщения как прочитанные. Отправил - значит прочитал.
        messageRepository.markAllMessagesRead(recipientId, senderId, advertisementId);

        Message message = new Message(
                advertisementId,
                senderId,
                recipientId,
                requestDto.getMessageText(),
                LocalDateTime.now()
        );
        message = messageRepository.save(message);

        log.info("message saved: {}", message);
        return messageMapper.messageToMessageResponseDto(message);
    }

    @Override
    @Transactional
    public void markReadUpTo(MessageMarkReadRequestDto dto) {
        log.info("markReadUpTo({}) in {}", dto, getClass().getSimpleName());

        UUID currentUserId = SecurityUtils.getCurrentUserPrincipal().getId();
        UUID secondUserId = dto.getSecondUserId();
        UUID advertisementId = dto.getAdvertisementId();

        validateRecipient(currentUserId, secondUserId);

        if (advertisementId != null && !advertisementRepository.existsById(advertisementId))
            throw new AdvertisementNotFoundException(advertisementId);

        if (!messageRepository.chatExists(currentUserId, secondUserId, advertisementId))
            throw new ConversationNotFoundException(secondUserId, advertisementId);

        int count = messageRepository.markMessagesReadUpTo(secondUserId, currentUserId, advertisementId, dto.getUpToDateTime(), dto.getUpToMessageId());
        log.info("messages marked as read: {}", count);
    }


    /**
     * Возвращает список сообщений, где первый элемент самое старое сообщение из выборки
     */
    @Override
    @Transactional(readOnly = true)
    public List<MessageResponseDto> getListOfMessages(MessageFilterDto filter) {
        log.info("getListOfMessages({}) in {}", filter, getClass().getSimpleName());

        filter.setCurrentUserId(SecurityUtils.getCurrentUserPrincipal().getId());

        validateRecipient(filter.getCurrentUserId(), filter.getSecondUserId());
        // если не задали курсоры, то предполагаем, что чат еще не смотрели и надо проверить есть ли непрочитанные сообщения
        if (filter.getCursorMessageId() == null && filter.getCursorDateTime() == null) {
            long unreadMessagesCount = messageRepository
                    .getUnreadMessagesCount(filter.getSecondUserId(), filter.getCurrentUserId(), filter.getAdvertisementId());
            filter.setUnreadMessagesCount(unreadMessagesCount);
        }

        List<MessageResponseDto> allByFilter = messageRepository.findAllByFilter(filter);
        log.info("messages found quantity: {}", allByFilter.size());
        return allByFilter;
    }

    private void validateRecipient(UUID senderId, UUID recipientId) {
        if (senderId.equals(recipientId))
            throw new MessageSendException(USER_MESSAGES_THE_SAME_USER);

        if (!userRepository.existsById(recipientId))
            throw new UserNotFoundException(recipientId);
    }

    private void validateAdvertisementRules(UUID senderId, UUID recipientId, UUID advertisementId) {
        if (advertisementId == null) return;

        Advertisement ad = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        UUID ownerId = ad.getUser().getId();

        // если ни получатель, ни мы не владелец — запрещено переписываться насчет этого объявления
        boolean isSenderOwner = senderId.equals(ownerId);
        boolean isRecipientOwner = recipientId.equals(ownerId);
        if (!isRecipientOwner && !isSenderOwner)
            throw new MessageSendException(RECIPIENT_IS_NOT_OWNER_OF_THE_ADVERTISEMENT);

        // если мы — владелец и переписка ещё не начиналась — запрещено писать первым
        if (isSenderOwner && !messageRepository.chatExists(senderId, recipientId, advertisementId))
            throw new MessageSendException(OWNER_OF_THE_ADVERTISEMENT_CANT_MESSAGE_FIRST);
    }
}
