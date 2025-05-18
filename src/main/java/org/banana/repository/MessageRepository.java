package org.banana.repository;

import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.entity.Message;
import org.banana.repository.crud.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends CrudRepository<Message, UUID> {

    long countMessagesInChat(UUID secondUserId, UUID currentUserId, UUID advertisementId);

    int markMessagesReadUpTo(UUID senderId, UUID recipientId, UUID advertisementId, LocalDateTime upToDateTime, UUID upToMessageId);

    int markAllMessagesRead(UUID fromUserId, UUID toUserId, UUID advertisementId);

    boolean chatExists(UUID user1, UUID user2, UUID advertisementId);

    long getUnreadMessagesCount(UUID secondUserId, UUID currentUserId, UUID advertisementId);

    List<MessageResponseDto> findAllByFilter(MessageFilterDto filter);
}
