package org.banana.repository;

import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.entity.Message;
import org.banana.repository.crud.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends CrudRepository<Message, UUID> {

    /**
     * Возвращает количество сообщений в чате
     *
     * @param secondUserId    идентификатор пользователя с которым переписывается текущий пользователь чате
     * @param currentUserId   идентификатор текущего пользователя который запрашивает количество сообщений в чате
     * @param advertisementId идентификатор объявления. Может быть null, если чат между двумя пользователями без привязки к объявлению
     * @return long - количество сообщений в чате
     */
    long countMessagesInChat(UUID secondUserId, UUID currentUserId, UUID advertisementId);

    /**
     * Отмечает сообщения как прочитанные по определенное сообщение
     *
     * @param senderId        идентификатор отправителя. Тот чьи сообщения мы хотим прочитать
     * @param recipientId     идентификатор получателя. Наш текущий пользователь
     * @param advertisementId идентификатор объявления. Может быть null, если чат между двумя пользователями без привязки к объявлению
     * @param upToDateTime    дата и время, до которого нужно отметить сообщения прочитанными
     * @param upToMessageId   идентификатор сообщения, до которого нужно отметить сообщения прочитанными включительно
     * @return int - количество отмеченных сообщений
     */
    int markMessagesReadUpTo(UUID senderId, UUID recipientId, UUID advertisementId, LocalDateTime upToDateTime, UUID upToMessageId);

    /**
     * Отмечает все сообщения как прочитанные
     *
     * @param fromUserId      идентификатор отправителя. Тот чьи сообщения мы хотим прочитать
     * @param toUserId        идентификатор получателя. Наш текущий пользователь
     * @param advertisementId идентификатор объявления. Может быть null, если чат между двумя пользователями без привязки к объявлению
     * @return int - количество отмеченных сообщений
     */
    int markAllMessagesRead(UUID fromUserId, UUID toUserId, UUID advertisementId);

    /**
     * Проверяет, существует ли чат между двумя пользователями
     *
     * @param user1           идентификатор первого пользователя в чате
     * @param user2           идентификатор второго пользователя в чате
     * @param advertisementId идентификатор объявления. Может быть null, если чат между двумя пользователями без привязки к объявлению
     * @return true, если чат существует, иначе false
     */
    boolean chatExists(UUID user1, UUID user2, UUID advertisementId);

    /**
     * Возвращает количество непрочитанных сообщений для текущего пользователя в чате
     *
     * @param secondUserId    идентификатор пользователя с которым переписывается текущий пользователь
     * @param currentUserId   идентификатор текущего пользователя
     * @param advertisementId идентификатор объявления. Может быть null, если чат между двумя пользователями без привязки к объявлению
     * @return long - количество непрочитанных сообщений
     */
    long getUnreadMessagesCount(UUID secondUserId, UUID currentUserId, UUID advertisementId);

    /**
     * Возвращает список сообщений по фильтру из чата
     *
     * @param filter фильтр MessageFilterDto
     * @return List<MessageResponseDto> - список сообщений по фильтру
     */
    List<MessageResponseDto> findAllByFilter(MessageFilterDto filter);
}
