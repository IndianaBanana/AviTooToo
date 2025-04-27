package org.banana.exception;

public class UserAddingPhoneException extends RuntimeException {

    private static final String MESSAGE = "User with phone <<%s>> already exists.";

    public UserAddingPhoneException(String phone) {
        super(MESSAGE.formatted(phone));
    }
}
