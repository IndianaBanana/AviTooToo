package org.banana.exception;

import java.util.UUID;

public class CommentInAdvertisementNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Comment with id <<%s>> does not exist in Advertisement with id <<%s>>.";

    public CommentInAdvertisementNotFoundException(UUID commentId, UUID advertisementId) {
        super(MESSAGE.formatted(commentId, advertisementId));
    }
}
