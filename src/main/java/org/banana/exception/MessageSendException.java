package org.banana.exception;

import lombok.Getter;

/**
 * Created by Banana on 29.04.2025
 */
public class MessageSendException extends RuntimeException {

    private static final String MESSAGE = "You can't send message to that user: %s.";

    public MessageSendException(MessageSendExceptionMessage type) {
        super(MESSAGE.formatted(type.getDescription()));
    }

    public enum MessageSendExceptionMessage {
        USER_MESSAGES_THE_SAME_USER("Messages to yourself is prohibited"),
        RECIPIENT_IS_NOT_OWNER_OF_THE_ADVERTISEMENT("User is not the owner of the advertisement"),
        OWNER_OF_THE_ADVERTISEMENT_CANT_MESSAGE_FIRST("Owner of advertisement can't send first message");

        @Getter
        private final String description;

        MessageSendExceptionMessage(String description) {
            this.description = description;
        }
    }
}
