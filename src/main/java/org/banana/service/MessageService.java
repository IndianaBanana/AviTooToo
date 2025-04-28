package org.banana.service;

import lombok.extern.slf4j.Slf4j;
import org.banana.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Banana on 25.04.2025
 */
@Service
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
}
