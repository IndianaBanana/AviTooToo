package org.banana.repository;

import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.entity.Message;
import org.banana.repository.crud.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends CrudRepository<Message, UUID> {

    int markMessagesReadUpTo(UUID recipientId, UUID secondUserId, UUID advertisementId, LocalDateTime upToDateTime);

    int markAllMessagesRead(UUID fromUserId, UUID toUserId, UUID advertisementId);

    boolean chatExists(UUID user1, UUID user2, UUID advertisementId);

    long hasUnreadMessages(UUID secondUserId, UUID currentUserId, UUID advertisementId);

    List<MessageResponseDto> findAllByFilter(MessageFilterDto filter);
}
