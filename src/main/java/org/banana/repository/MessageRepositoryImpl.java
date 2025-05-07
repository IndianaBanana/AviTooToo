package org.banana.repository;

import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.entity.Message;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class MessageRepositoryImpl extends AbstractCrudRepositoryImpl<Message, UUID> implements MessageRepository {

    public static final String CHAT_EXISTS_BY_AD_FIRST_USER_AND_SECOND_USER = """
            SELECT 1
            FROM Message m
            WHERE
              (
                (m.senderId = :user1 AND m.recipientId = :user2)
                OR
                (m.senderId = :user2 AND m.recipientId = :user1)
              )
            AND (m.advertisementId = :advertisementId OR (m.advertisementId IS NULL AND :advertisementId IS NULL))
            """;
    public static final String SELECT_MESSAGES_BY_AD_FIRST_USER_AND_SECOND_USER = """
            SELECT new org.banana.dto.message.MessageResponseDto(
                m.id,
                m.advertisementId,
                m.senderId,
                m.recipientId,
                m.messageText,
                m.messageDate,
                m.isRead
            )
            FROM Message m
            WHERE
              (
                (m.senderId = :secondUserId AND m.recipientId = :currentUserId)
                OR
                (m.senderId = :currentUserId AND m.recipientId = :secondUserId)
              )
              AND (m.advertisementId = :advertisementId OR (m.advertisementId IS NULL AND :advertisementId IS NULL))
            """;
    public static final String EXISTS_BY_UNREAD_MESSAGE = """
            SELECT 1 FROM Message m
            WHERE m.senderId = :secondUserId
            AND m.recipientId = :currentUserId
            AND (m.advertisementId = :advertisementId OR (m.advertisementId IS NULL AND :advertisementId IS NULL))
            AND m.isRead = false
            """;
    private static final String UPDATE_MARK_READ = """
            update Message m
            set m.isRead = true
            where m.senderId     = :fromUserId
              and m.recipientId  = :toUserId
              and (
                    m.advertisementId = :advertisementId
                    or (m.advertisementId is null and :advertisementId is null)
                  )
              and m.isRead = false
            """;

    public MessageRepositoryImpl() {
        super(Message.class);
    }

    @Override
    public int markMessagesRead(UUID fromUserId, UUID toUserId, UUID advertisementId) {
        var q = getSession()
                .createMutationQuery(UPDATE_MARK_READ)
                .setParameter("fromUserId", fromUserId)
                .setParameter("toUserId", toUserId)
                .setParameter("advertisementId", advertisementId);
        return q.executeUpdate();
    }

    @Override
    public boolean existsByFirstUserIdAndSecondUserIdAndAdvertisementId(UUID user1, UUID user2, UUID advertisementId) {
        Integer result = getSession().createQuery(CHAT_EXISTS_BY_AD_FIRST_USER_AND_SECOND_USER, Integer.class)
                .setParameter("user1", user1)
                .setParameter("user2", user2)
                .setParameter("advertisementId", advertisementId)
                .setMaxResults(1)
                .getSingleResultOrNull();
        return result != null;
    }

    @Override
    public boolean existsBySenderIdAndRecipientIdAndIsReadFalse(UUID secondUserId, UUID currentUserId, UUID advertisementId) {
        Integer result = getSession().createQuery(EXISTS_BY_UNREAD_MESSAGE, Integer.class)
                .setParameter("secondUserId", secondUserId)
                .setParameter("currentUserId", currentUserId)
                .setParameter("advertisementId", advertisementId)
                .setMaxResults(1)
                .getSingleResultOrNull();
        return result != null;
    }

    @Override
    public List<MessageResponseDto> findAllByFilter(MessageFilterDto filter) {
        StringBuilder jpql = new StringBuilder(SELECT_MESSAGES_BY_AD_FIRST_USER_AND_SECOND_USER);

        // 2) Фильтр по advertisementId
        UUID advertisementId = filter.getAdvertisementId();
//        jpql.append(advertisementId != null ? " AND m.advertisementId = :advertisementId" : " AND m.advertisementId IS NULL");

        // 3) Логика курсора или начальной выдачи
        LocalDateTime cursorDateTime = filter.getCursorDateTime();
        UUID cursorMessageId = filter.getCursorMessageId();
        if (cursorDateTime != null && cursorMessageId != null) {
            if (filter.getIsBefore()) {
                jpql.append("""
                          AND  m.messageDate < :cursorDateTime and m.id < :cursorMessageId
                          ORDER BY m.messageDate DESC, m.id DESC
                        """);
            } else {
                jpql.append("""
                        
                          AND  m.messageDate > :cursorDateTime and m.id > :cursorMessageId
                          ORDER BY m.messageDate ASC, m.id ASC
                        """);
            }
        } else {
            if (filter.getIsCurrentUserHasUnreadMessages()) {
                // если у текущего пользователя есть непрочитанные в этой переписке сообщения,
                // то у его собеседника непрочитанных сообщений быть не может
                jpql.append("""
                          ORDER BY m.isRead ASC, m.messageDate ASC, m.id ASC
                        """);
            } else {
                jpql.append("""
                          ORDER BY m.messageDate DESC, m.id DESC
                        """);
            }
        }

        Query<MessageResponseDto> query = getSession()
                .createQuery(jpql.toString(), MessageResponseDto.class)
                .setParameter("secondUserId", filter.getSecondUserId())
                .setParameter("currentUserId", filter.getCurrentUserId());

        if (advertisementId != null) {
            query.setParameter("advertisementId", advertisementId);
        }

        if (cursorDateTime != null && cursorMessageId != null) {
            query.setParameter("cursorDateTime", cursorDateTime).setParameter("cursorMessageId", cursorMessageId);
        }

        query.setMaxResults(filter.getLimit());

        return query.getResultList();
    }
}
