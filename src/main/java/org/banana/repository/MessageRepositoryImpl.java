package org.banana.repository;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MessageRepositoryImpl extends AbstractCrudRepositoryImpl<Message, UUID> implements MessageRepository {

    public static final String CHAT_EXISTS = """
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
                m.messageDateTime,
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
            select count(m)
            FROM Message m
            WHERE m.senderId = :secondUserId
            AND m.recipientId = :currentUserId
            AND (m.advertisementId = :advertisementId OR (m.advertisementId IS NULL AND :advertisementId IS NULL))
            AND m.isRead = false
            """;
    private static final String UPDATE_MARK_READ = """
            update Message m
            set m.isRead = true
            where m.senderId = :fromUserId
              and m.recipientId = :toUserId
              and (
                    m.advertisementId = :advertisementId
                    or (m.advertisementId is null and :advertisementId is null)
                  )
              and m.isRead = false
            """;

    private static final String UPDATE_MARK_READ_UPTO = """
            UPDATE Message m
               SET m.isRead = true
             WHERE m.recipientId = :recipientId
               AND m.senderId = :secondUserId
               AND (m.advertisementId = :advertisementId
                    OR (m.advertisementId IS NULL AND :advertisementId IS NULL))
               AND m.messageDateTime <= :upToDateTime
               AND m.isRead = false
            """;

    public MessageRepositoryImpl() {
        super(Message.class);
    }

    @Override
    public int markMessagesReadUpTo(UUID recipientId, UUID secondUserId, UUID advertisementId, LocalDateTime upToDateTime) {
        return getSession()
                .createMutationQuery(UPDATE_MARK_READ_UPTO)
                .setParameter("recipientId", recipientId)
                .setParameter("secondUserId", secondUserId)
                .setParameter("advertisementId", advertisementId)
                .setParameter("upToDateTime", upToDateTime)
                .executeUpdate();
    }

    @Override
    public int markAllMessagesRead(UUID fromUserId, UUID toUserId, UUID advertisementId) {
        log.info("entering markAllMessagesRead({}, {}, {}) in {}", fromUserId, toUserId, advertisementId, getClass().getSimpleName());
        return getSession()
                .createMutationQuery(UPDATE_MARK_READ)
                .setParameter("fromUserId", fromUserId)
                .setParameter("toUserId", toUserId)
                .setParameter("advertisementId", advertisementId)
                .executeUpdate();
    }

    @Override
    public boolean chatExists(UUID user1, UUID user2, UUID advertisementId) {
        log.info("entering chatExists({}, {}, {}) in {}", user1, user2, advertisementId, getClass().getSimpleName());
        Integer result = getSession().createQuery(CHAT_EXISTS, Integer.class)
                .setParameter("user1", user1)
                .setParameter("user2", user2)
                .setParameter("advertisementId", advertisementId)
                .setMaxResults(1)
                .getSingleResultOrNull();
        return result != null;
    }

    @Override
    public long hasUnreadMessages(UUID secondUserId, UUID currentUserId, UUID advertisementId) {
        log.info("entering hasUnreadMessages({}, {}, {}) in {}", secondUserId, currentUserId, advertisementId, getClass().getSimpleName());
        return getSession().createQuery(EXISTS_BY_UNREAD_MESSAGE, Long.class)
                .setParameter("secondUserId", secondUserId)
                .setParameter("currentUserId", currentUserId)
                .setParameter("advertisementId", advertisementId)
                .getSingleResultOrNull();
    }

    @Override
    public List<MessageResponseDto> findAllByFilter(MessageFilterDto filter) {
        log.info("entering findAllByFilter({}) in {}", filter, getClass().getSimpleName());
        UUID advertisementId = filter.getAdvertisementId();
        LocalDateTime cursorDateTime = filter.getCursorDateTime();
        UUID cursorMessageId = filter.getCursorMessageId();

        String jpql = getJpql(filter, cursorDateTime, cursorMessageId);

        Query<MessageResponseDto> query = getSession()
                .createQuery(jpql, MessageResponseDto.class)
                .setParameter("secondUserId", filter.getSecondUserId())
                .setParameter("currentUserId", filter.getCurrentUserId())
                .setParameter("advertisementId", advertisementId);

//        if (advertisementId != null) {
//            query.setParameter("advertisementId", advertisementId);
//        }

        if (cursorDateTime != null && cursorMessageId != null) {
            query.setParameter("cursorDateTime", cursorDateTime).setParameter("cursorMessageId", cursorMessageId);
        }
        if (filter.getUnreadMessagesCount() != null && filter.getUnreadMessagesCount() > 0) {
            double floor = Math.floor(filter.getLimit() / 1.5);
            int offset = (int) (filter.getUnreadMessagesCount() - floor);
            if (offset < 0) offset = 0;
            log.info("setting offset to: {}", offset);
            query.setFirstResult(offset);
        }
        query.setMaxResults(filter.getLimit());

        return query.getResultList();
    }

    private String getJpql(MessageFilterDto filter, LocalDateTime cursorDateTime, UUID cursorMessageId) {
        StringBuilder jpql = new StringBuilder(SELECT_MESSAGES_BY_AD_FIRST_USER_AND_SECOND_USER);
        if (cursorDateTime != null && cursorMessageId != null) {
            if (filter.getIsBefore() != null && filter.getIsBefore()) {
                jpql.append("""
                            AND (
                                m.messageDateTime < :cursorDateTime
                                OR (m.messageDateTime = :cursorDateTime AND m.id < :cursorMessageId)
                            )
                            ORDER BY m.messageDateTime DESC, m.id DESC
                        """);
            } else {
                jpql.append("""
                            AND (
                                m.messageDateTime > :cursorDateTime
                                OR (m.messageDateTime = :cursorDateTime AND m.id > :cursorMessageId)
                            )
                            ORDER BY m.messageDateTime ASC, m.id ASC
                        """);
            }
        } else {
//            if (filter.getUnreadMessagesCount() != null && filter.getUnreadMessagesCount() > 0) {
//                // если у текущего пользователя есть непрочитанные в этой переписке сообщения,
//                // то у его собеседника непрочитанных сообщений быть не может
//                jpql.append(" And m.isRead = false ORDER BY m.messageDateTime ASC, m.id ASC\n");
//            } else {
//                jpql.append(" ORDER BY m.messageDateTime DESC, m.id DESC\n");
//            }
            jpql.append(" ORDER BY m.messageDateTime DESC, m.id DESC\n");
        }
        return jpql.toString();
    }
}
