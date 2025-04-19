package org.banana.repository;

import org.banana.entity.Comment;
import org.banana.repository.crud.CrudRepository;

import java.util.UUID;

public interface CommentRepository extends CrudRepository<Comment, UUID> {

}
