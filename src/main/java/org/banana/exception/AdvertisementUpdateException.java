package org.banana.exception;

import lombok.Getter;

public class AdvertisementUpdateException extends AbstractConflictException {

    private static final String MESSAGE = "Can't update advertisement: %s.";

    public AdvertisementUpdateException(AdvertisementUpdateExceptionMessage type) {
        super(MESSAGE.formatted(type.getDescription()));
    }

    public enum AdvertisementUpdateExceptionMessage {
        ADVERTISEMENT_CLOSED("Advertisement is closed"),
        ADVERTISEMENT_NOT_CLOSED("Advertisement is not closed"),
        ALREADY_PROMOTED("Advertisement is already promoted"),
        UNEXPECTED_ERROR("unexpected error"),
        NOT_OWNER("User is not the owner of the advertisement");

        @Getter
        private final String description;

        AdvertisementUpdateExceptionMessage(String description) {
            this.description = description;
        }
    }
}
