package org.banana.exception;

public class UserNotFoundException extends RuntimeException {

    private static final String MESSAGE = "User with id <<%s>> does not exist.";

    public UserNotFoundException(String std) {
        super(MESSAGE.formatted(std));
    }
}
