package org.banana.repository;

import org.banana.entity.Comment;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class CommentRepositoryImpl extends AbstractCrudRepositoryImpl<Comment, UUID> implements CommentRepository {

    private static final String FIND_BY_ROOT_COMMENT_ID_IS_NULL = """
            select c
            from Comment c
            join fetch c.commenter
            where c.rootCommentId is null and c.advertisementId = :advertisementId""";
    private static final String FIND_ALL_COMMENTS_IN_ROOT_IDS = """
            select c
            from Comment c
            join fetch c.commenter
            where c.rootCommentId in :rootCommentIds""";


    public CommentRepositoryImpl() {
        super(Comment.class);
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
