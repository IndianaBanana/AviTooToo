package org.banana.exception;

public class CityAlreadyExistsException extends AbstractConflictException {

    private static final String MESSAGE = "City with name <<%s>> already exists.";

    public CityAlreadyExistsException(String name) {
        super(MESSAGE.formatted(name));
    }
}
