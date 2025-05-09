package org.banana.exception;

import java.util.UUID;

public class SaleHistoryNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Sale history with id <<%s>> does not exist.";

    public SaleHistoryNotFoundException(UUID id) {
        super(MESSAGE.formatted(id));
    }
}
