package org.banana.exception;

/**
 * Created by Banana on 04.05.2025
 */
public class MessageSetToReadException extends RuntimeException {

    private static final String MESSAGE = "You can't set this message to 'read'. You are not the recipient of this message";

    public MessageSetToReadException() {
        super(MESSAGE);
    }
}
