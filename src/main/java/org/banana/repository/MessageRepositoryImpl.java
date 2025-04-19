package org.banana.repository;

import org.banana.entity.Message;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MessageRepositoryImpl extends AbstractCrudRepositoryImpl<Message, UUID> implements MessageRepository {

    public MessageRepositoryImpl() {
        super(Message.class);
    }
}
