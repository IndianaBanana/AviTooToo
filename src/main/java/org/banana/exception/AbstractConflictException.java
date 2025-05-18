package org.banana.exception;

public abstract class AbstractConflictException extends RuntimeException {

    public AbstractConflictException(String message) {
        super(message);
    }
}
