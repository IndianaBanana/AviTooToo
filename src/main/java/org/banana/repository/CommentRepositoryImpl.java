package org.banana.repository;

import lombok.extern.slf4j.Slf4j;
import org.banana.dto.comment.CommentResponseDto;
import org.banana.entity.Comment;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class CommentRepositoryImpl extends AbstractCrudRepositoryImpl<Comment, UUID> implements CommentRepository {

    private static final String FIND_ALL_DTO = """
            select new org.banana.dto.comment.CommentResponseDto(
                c.id, c.advertisementId, c.commenter.id, c.commenter.firstName, c.commenter.lastName,
                c.rootCommentId, c.parentCommentId, c.commentText, c.commentDate)
            from Comment c
            left join c.commenter
            """;
    private static final String FIND_DTO_BY_ROOT_COMMENT_ID_IS_NULL = FIND_ALL_DTO + """
            where c.rootCommentId is null and c.advertisementId = :advertisementId
            order by c.commentDate desc, c.id desc""";

    private static final String FIND_DTO_ALL_COMMENTS_IN_ROOT_IDS = FIND_ALL_DTO + """
            where c.rootCommentId in :rootCommentIds
            order by c.rootCommentId desc, c.parentCommentId desc, c.commentDate desc""";

    private static final String FIND_BY_ID = "select c from Comment c left join fetch c.commenter where c.id = :id";

    public CommentRepositoryImpl() {
        super(Comment.class);
    }

    @Override
    public Optional<Comment> findFetchedById(UUID id) {
        log.info("findFetchedById({})", id);
        return getSession()
                .createQuery(FIND_BY_ID, Comment.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<CommentResponseDto> findAllRootCommentsByAdvertisementId(UUID advertisementId, int offset, int limit) {
        log.info("findAllRootCommentsByAdvertisementId({}, {}, {})", advertisementId, offset, limit);
        return getSession().createQuery(FIND_DTO_BY_ROOT_COMMENT_ID_IS_NULL, CommentResponseDto.class)
                .setParameter("advertisementId", advertisementId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<CommentResponseDto> findAllCommentsInRootIds(List<UUID> rootCommentIds) {
        log.info("findAllCommentsInRootIds({})", rootCommentIds);
        return getSession().createQuery(FIND_DTO_ALL_COMMENTS_IN_ROOT_IDS, CommentResponseDto.class)
                .setParameter("rootCommentIds", rootCommentIds).getResultList();
    }
}
