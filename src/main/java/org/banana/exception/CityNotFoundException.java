package org.banana.exception;

import java.util.UUID;

public class CityNotFoundException extends AbstractNotFoundException {

    private static final String MESSAGE = "City with id <<%s>> does not exist.";

    public CityNotFoundException(UUID id) {
        super(MESSAGE.formatted(id));
    }
}
