package org.banana.repository;

import org.banana.entity.Comment;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class CommentRepositoryImpl extends AbstractCrudRepositoryImpl<Comment, UUID> implements CommentRepository {

    public CommentRepositoryImpl() {
        super(Comment.class);
    }
}
