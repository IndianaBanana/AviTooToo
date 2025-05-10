package org.banana.repository;

import org.banana.entity.Comment;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CommentRepositoryImpl extends AbstractCrudRepositoryImpl<Comment, UUID> implements CommentRepository {

    // todo переделать запрос для пагинации на нормальный
    private static final String FIND_BY_ROOT_COMMENT_ID_IS_NULL = """
            select c
            from Comment c
            left join fetch c.commenter cc
            where c.rootCommentId is null and c.advertisementId = :advertisementId
            order by c.commentDate desc, c.id desc""";
    private static final String FIND_ALL_COMMENTS_IN_ROOT_IDS = """
            select c
            from Comment c
            left join c.commenter
            where c.rootCommentId in :rootCommentIds
            order by c.rootCommentId DESC, c.parentCommentId DESC, c.commentDate DESC""";
    private static final String FIND_BY_ID = "SELECT c FROM Comment c left join fetch c.commenter WHERE c.id = :id";

    public CommentRepositoryImpl() {
        super(Comment.class);
    }

    @Override
    public Optional<Comment> findFetchedById(UUID id) {
        return getSession()
                .createQuery(FIND_BY_ID, Comment.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Comment> findAllRootCommentsByAdvertisementId(UUID advertisementId, int offset, int limit) {
        return getSession().createQuery(FIND_BY_ROOT_COMMENT_ID_IS_NULL, Comment.class)
                .setParameter("advertisementId", advertisementId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Comment> findAllCommentsInRootIds(List<UUID> rootCommentIds) {
        return getSession().createQuery(FIND_ALL_COMMENTS_IN_ROOT_IDS, Comment.class)
                .setParameter("rootCommentIds", rootCommentIds).getResultList();
    }
}
