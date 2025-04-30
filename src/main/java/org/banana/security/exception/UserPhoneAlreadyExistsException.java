package org.banana.security.exception;

public class UserPhoneAlreadyExistsException extends RuntimeException {

    private static final String MESSAGE = "User with phone <<%s>> already exists.";

    public UserPhoneAlreadyExistsException(String phone) {
        super(MESSAGE.formatted(phone));
    }
}
