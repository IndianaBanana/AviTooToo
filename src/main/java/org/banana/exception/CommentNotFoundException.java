package org.banana.exception;

import java.util.UUID;

public class CommentNotFoundException extends AbstractNotFoundException {

    private static final String MESSAGE = "Comment with id <<%s>> does not exist.";

    public CommentNotFoundException(UUID id) {
        super(MESSAGE.formatted(id));
    }
}
