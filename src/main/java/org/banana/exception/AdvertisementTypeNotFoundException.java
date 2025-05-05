package org.banana.exception;

import java.util.UUID;

public class AdvertisementTypeNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Advertisement type with id <<%s>> does not exist.";

    public AdvertisementTypeNotFoundException(UUID id) {
        super(MESSAGE.formatted(id));
    }
}
