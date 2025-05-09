package org.banana.exception;

/**
 * Created by Banana on 04.05.2025
 */
public class SaleHistoryAccessDeniedException extends RuntimeException {

    private static final String MESSAGE = "You don't have permission to access sale history for this advertisement";

    public SaleHistoryAccessDeniedException() {
        super(MESSAGE);
    }
}
