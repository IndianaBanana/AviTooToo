package org.banana.exception;

public class SaleHistoryAccessDeniedException extends RuntimeException {

    private static final String MESSAGE = "You don't have permission to access sale history for this advertisement";

    public SaleHistoryAccessDeniedException() {
        super(MESSAGE);
    }
}
