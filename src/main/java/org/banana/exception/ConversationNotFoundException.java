package org.banana.exception;

import java.util.UUID;

public class ConversationNotFoundException extends AbstractNotFoundException {

    private static final String MESSAGE = "Conversation with user {%s} and advertisement {%s} can't be found.";

    public ConversationNotFoundException(UUID recipientId, UUID advertisementId) {
        super(MESSAGE.formatted(recipientId, advertisementId == null ? "without advertisement" : advertisementId));
    }
}
