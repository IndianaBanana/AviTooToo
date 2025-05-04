package org.banana.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    private static final String MESSAGE = "User with id <<%s>> does not exist.";

    public UserNotFoundException(UUID id) {
        super(MESSAGE.formatted(id));
    }
}
