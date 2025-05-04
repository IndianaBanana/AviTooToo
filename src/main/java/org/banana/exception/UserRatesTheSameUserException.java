package org.banana.exception;

/**
 * Created by Banana on 04.05.2025
 */
public class UserRatesTheSameUserException extends RuntimeException {

    private static final String MESSAGE = "Rating yourself is prohibited";

    public UserRatesTheSameUserException() {
        super(MESSAGE);
    }
}
