package org.banana.repository;

import org.banana.entity.Message;
import org.banana.repository.crud.CrudRepository;

import java.util.UUID;

public interface MessageRepository extends CrudRepository<Message, UUID> {

}
