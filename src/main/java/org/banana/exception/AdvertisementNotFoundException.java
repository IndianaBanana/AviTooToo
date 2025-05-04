package org.banana.exception;

import java.util.UUID;

/**
 * Created by Banana on 04.05.2025
 */
public class AdvertisementNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Advertisement with id <<%s>> can't be found.";

    public AdvertisementNotFoundException(UUID uuid) {
        super(MESSAGE.formatted(uuid));
    }
}
