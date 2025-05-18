package org.banana.exception;

public class SaleHistoryUnexpectedException extends RuntimeException {

    private static final String MESSAGE = "Something went wrong, please try again";

    public SaleHistoryUnexpectedException() {
        super(MESSAGE);
    }
}
