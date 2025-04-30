package org.banana.security.exception;

public class UserUsernameAlreadyExistsException extends RuntimeException {

    private static final String MESSAGE = "User with username <<%s>> already exists.";

    public UserUsernameAlreadyExistsException(String email) {
        super(MESSAGE.formatted(email));
    }
}
