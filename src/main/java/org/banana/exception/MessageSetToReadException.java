package org.banana.exception;

public class MessageSetToReadException extends RuntimeException {

    private static final String MESSAGE = "You can't set this message to 'read'. You are not the recipient of this message";

    public MessageSetToReadException() {
        super(MESSAGE);
    }
}
