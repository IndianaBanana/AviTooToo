package org.banana.exception;

import java.util.UUID;

public class MessageNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Message with id <<%s>> can't be found.";

    public MessageNotFoundException(UUID id) {
        super(MESSAGE.formatted(id));
    }
}
