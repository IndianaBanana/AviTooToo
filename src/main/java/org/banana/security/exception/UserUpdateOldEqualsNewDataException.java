package org.banana.security.exception;

import lombok.Getter;
import org.banana.exception.AbstractConflictException;


public class UserUpdateOldEqualsNewDataException extends AbstractConflictException {

    private static final String MESSAGE = "New %s should be different from the old %s";

    public UserUpdateOldEqualsNewDataException(UserUpdateExceptionMessage type) {
        super(MESSAGE.formatted(type.getDescription(), type.getDescription()));
    }

    public enum UserUpdateExceptionMessage {
        SAME_USERNAME("username"),
        SAME_PHONE("phone"),
        SAME_FIRST_NAME_AND_LAST_NAME("first name and last name");

        @Getter
        private final String description;

        UserUpdateExceptionMessage(String description) {
            this.description = description;
        }
    }
}
