package org.banana.repository;

import lombok.extern.slf4j.Slf4j;
import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.entity.Message;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
@Slf4j
public class MessageRepositoryImpl extends AbstractCrudRepositoryImpl<Message, UUID> implements MessageRepository {

    public static final String CHAT_EXISTS = """
            select 1
            from Message m
            where
              (
                (m.senderId = :user1 and m.recipientId = :user2)
                or
                (m.senderId = :user2 and m.recipientId = :user1)
              )
            and (m.advertisementId = :advertisementId or (m.advertisementId is null and :advertisementId is null))
            """;
    public static final String SELECT_MESSAGES_BY_AD_FIRST_USER_AND_SECOND_USER = """
            select new org.banana.dto.message.MessageResponseDto(
                m.id,
                m.advertisementId,
                m.senderId,
                m.recipientId,
                m.messageText,
                m.messageDateTime,
                m.isRead
            )
            from Message m
            where
              (
                (m.senderId = :secondUserId and m.recipientId = :currentUserId)
                or
                (m.senderId = :currentUserId and m.recipientId = :secondUserId)
              )
              and (m.advertisementId = :advertisementId or (m.advertisementId is null and :advertisementId is null))
            """;

    public static final String COUNT_MESSAGES = """
            select count(m)
            from Message m
            where m.senderId = :secondUserId
            and m.recipientId = :currentUserId
            and (m.advertisementId = :advertisementId or (m.advertisementId is null and :advertisementId is null))""";

    public static final String COUNT_UNREAD_MESSAGES = COUNT_MESSAGES + """
            and m.isRead = false
            """;
    private static final String UPDATE_MARK_READ = """
            update Message m
            set m.isRead = true
            where m.senderId = :senderId
              and m.recipientId = :recipientId
              and (
                    m.advertisementId = :advertisementId
                    or (m.advertisementId is null and :advertisementId is null)
                  )
              and m.isRead = false
            """;
    // todo возможно переписать надо запрос: and m.messageDateTime < :upToDateTime || (and m.messageDateTime = :upToDateTime && m.id <= :upToMessageId)
    private static final String UPDATE_MARK_READ_UP_TO = UPDATE_MARK_READ + """
               and m.messageDateTime < :upToDateTime || (m.messageDateTime = :upToDateTime and m.id <= :upToMessageId)
            """;

    public MessageRepositoryImpl() {
        super(Message.class);
    }

    @Override
    public long countMessagesInChat(UUID secondUserId, UUID currentUserId, UUID advertisementId) {
        log.info("entering countMessagesInChat({}, {}, {}) in {}", secondUserId, currentUserId, advertisementId, getClass().getSimpleName());
        return getSession().createQuery(COUNT_MESSAGES, Long.class)
                .setParameter("secondUserId", secondUserId)
                .setParameter("currentUserId", currentUserId)
                .setParameter("advertisementId", advertisementId)
                .getSingleResultOrNull();
    }

    @Override
    public int markMessagesReadUpTo(UUID senderId, UUID recipientId, UUID advertisementId, LocalDateTime upToDateTime, UUID upToMessageId) {
        log.info("entering markMessagesReadUpTo({}, {}, {}, {}) in {}", recipientId, senderId, advertisementId, upToDateTime, getClass().getSimpleName());
        return getSession()
                .createMutationQuery(UPDATE_MARK_READ_UP_TO)
                .setParameter("recipientId", recipientId)
                .setParameter("senderId", senderId)
                .setParameter("advertisementId", advertisementId)
                .setParameter("upToDateTime", upToDateTime)
                .setParameter("upToMessageId", upToMessageId)
                .executeUpdate();
    }

    @Override
    public int markAllMessagesRead(UUID senderId, UUID recipientId, UUID advertisementId) {
        log.info("entering markAllMessagesRead({}, {}, {}) in {}", senderId, recipientId, advertisementId, getClass().getSimpleName());
        return getSession()
                .createMutationQuery(UPDATE_MARK_READ)
                .setParameter("senderId", senderId)
                .setParameter("recipientId", recipientId)
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
    public long getUnreadMessagesCount(UUID secondUserId, UUID currentUserId, UUID advertisementId) {
        log.info("entering getUnreadMessagesCount({}, {}, {}) in {}", secondUserId, currentUserId, advertisementId, getClass().getSimpleName());
        return getSession().createQuery(COUNT_UNREAD_MESSAGES, Long.class)
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
        int offset = 0;
        Query<MessageResponseDto> query = getFindAllByFilterQuery(filter, cursorDateTime, cursorMessageId)
                .setParameter("secondUserId", filter.getSecondUserId())
                .setParameter("currentUserId", filter.getCurrentUserId())
                .setParameter("advertisementId", advertisementId);

        if (cursorDateTime != null && cursorMessageId != null) {
            query.setParameter("cursorDateTime", cursorDateTime).setParameter("cursorMessageId", cursorMessageId);
        } else if (filter.getUnreadMessagesCount() != null && filter.getUnreadMessagesCount() > 0) {
            offset = calculateOffset(filter);
            log.debug("setting offset to: {}", offset);
            query.setFirstResult(offset);
        }

        query.setMaxResults(filter.getLimit());

        List<MessageResponseDto> list = query.getResultList();

        if ((cursorDateTime == null || cursorMessageId == null)
            || (filter.getIsBefore() != null && filter.getIsBefore())) {
            Collections.reverse(list);
        }

        return list;
    }

    private int calculateOffset(MessageFilterDto filter) {
        int offset = (int) (filter.getUnreadMessagesCount() - Math.floor(filter.getLimit() / 1.5));
        if (offset > 0) {
            long totalCount = countMessagesInChat(filter.getSecondUserId(), filter.getCurrentUserId(), filter.getAdvertisementId());
            if ((totalCount - offset) < filter.getLimit()) {
                offset = (int) (totalCount - filter.getLimit());
            }
        }
        return Math.max(offset, 0);
    }

    private Query<MessageResponseDto> getFindAllByFilterQuery(MessageFilterDto filter, LocalDateTime cursorDateTime, UUID cursorMessageId) {
        StringBuilder jpql = new StringBuilder(SELECT_MESSAGES_BY_AD_FIRST_USER_AND_SECOND_USER);
        if (cursorDateTime != null && cursorMessageId != null) {
            if (filter.getIsBefore() != null && filter.getIsBefore()) {
                jpql.append("""
                            and (
                                m.messageDateTime < :cursorDateTime
                                or (m.messageDateTime = :cursorDateTime and m.id < :cursorMessageId)
                            )
                            order by m.messageDateTime desc, m.id desc
                        """);
            } else {
                jpql.append("""
                            and (
                                m.messageDateTime > :cursorDateTime
                                or (m.messageDateTime = :cursorDateTime and m.id > :cursorMessageId)
                            )
                            order by m.messageDateTime asc, m.id asc
                        """);
            }
        } else {
            jpql.append(" order by m.messageDateTime desc, m.id desc");
        }
        return getSession().createQuery(jpql.toString(), MessageResponseDto.class);
    }
}
