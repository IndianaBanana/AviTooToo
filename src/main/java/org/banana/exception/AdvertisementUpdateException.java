package org.banana.exception;

import lombok.Getter;

/**
 * Created by Banana on 29.04.2025
 */
public class AdvertisementUpdateException extends RuntimeException {

    private static final String MESSAGE = "Can't update advertisement: %s.";

    public AdvertisementUpdateException(AdvertisementUpdateExceptionMessage type) {
        super(MESSAGE.formatted(type.getDescription()));
    }

    public enum AdvertisementUpdateExceptionMessage {
        ALREADY_CLOSED("Advertisement is already closed"),
        ALREADY_PROMOTED("Advertisement is already promoted"),
        NOT_OWNER("User is not the owner of the advertisement");

        @Getter
        private final String description;

        AdvertisementUpdateExceptionMessage(String description) {
            this.description = description;
        }
    }
}
