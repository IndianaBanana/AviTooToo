package org.banana.exception;

public class UserDeleteCommentException extends RuntimeException {

    private static final String MESSAGE = "You are not allowed to delete this comment";

    public UserDeleteCommentException() {
        super(MESSAGE);
    }
}
