package org.banana.repository;

import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.entity.Message;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends CrudRepository<Message, UUID> {

    int markMessagesRead(UUID fromUserId, UUID toUserId, UUID advertisementId);

    /**
     * Checks if a message exists by sender ID and advertisement ID.
     *
     * @param user1           the ID of the sender
     * @param advertisementId the ID of the advertisement
     * @return true if the message exists, false otherwise
     */
    boolean existsByFirstUserIdAndSecondUserIdAndAdvertisementId(UUID user1, UUID user2, UUID advertisementId);

    boolean existsBySenderIdAndRecipientIdAndIsReadFalse(UUID secondUserId, UUID currentUserId, UUID advertisementId);

    List<MessageResponseDto> findAllByFilter(MessageFilterDto filter);
}
