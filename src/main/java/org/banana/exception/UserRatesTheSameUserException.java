package org.banana.exception;

public class UserRatesTheSameUserException extends AbstractConflictException {

    private static final String MESSAGE = "Rating yourself is prohibited";

    public UserRatesTheSameUserException() {
        super(MESSAGE);
    }
}
