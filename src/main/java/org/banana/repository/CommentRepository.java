package org.banana.repository;

import org.banana.entity.Comment;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends CrudRepository<Comment, UUID> {

    List<Comment> findAllRootCommentsByAdvertisementId(UUID advertisementId, int offset, int limit);

    List<Comment> findAllCommentsInRootIds(List<UUID> rootCommentIds);
}
