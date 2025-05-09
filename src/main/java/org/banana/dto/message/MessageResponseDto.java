package org.banana.dto.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageResponseDto {

    private final UUID id;
    private final UUID advertisementId;
    private final UUID senderId;
    private final UUID recipientId;
    private final String messageText;
    private final LocalDateTime messageDateTime;
    private final boolean isRead;
}
