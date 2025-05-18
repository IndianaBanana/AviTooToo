package org.banana.exception;

public class AdvertisementTypeAlreadyExistsException extends AbstractConflictException {

    private static final String MESSAGE = "Advertisement type with name <<%s>> already exists.";

    public AdvertisementTypeAlreadyExistsException(String name) {
        super(MESSAGE.formatted(name));
    }
}
