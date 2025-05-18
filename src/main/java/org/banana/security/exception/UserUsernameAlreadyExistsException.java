package org.banana.security.exception;

import org.banana.exception.AbstractConflictException;

public class UserUsernameAlreadyExistsException extends AbstractConflictException {

    private static final String MESSAGE = "User with username <<%s>> already exists.";

    public UserUsernameAlreadyExistsException(String email) {
        super(MESSAGE.formatted(email));
    }
}
