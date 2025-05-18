package org.banana.exception;

import java.util.UUID;

public class AdvertisementNotFoundException extends AbstractNotFoundException {

    private static final String MESSAGE = "Advertisement with id <<%s>> can't be found.";

    public AdvertisementNotFoundException(UUID uuid) {
        super(MESSAGE.formatted(uuid));
    }
}
