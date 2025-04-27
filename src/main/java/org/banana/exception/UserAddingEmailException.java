package org.banana.exception;

public class UserAddingEmailException extends RuntimeException {

    private static final String MESSAGE = "User with email <<%s>> already exists.";

    public UserAddingEmailException(String email) {
        super(MESSAGE.formatted(email));
    }
}
