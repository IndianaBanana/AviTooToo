package org.banana.security.exception;

import org.banana.exception.AbstractConflictException;

public class UserPhoneAlreadyExistsException extends AbstractConflictException {

    private static final String MESSAGE = "User with phone <<%s>> already exists.";

    public UserPhoneAlreadyExistsException(String phone) {
        super(MESSAGE.formatted(phone));
    }
}
