package org.banana.dto.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageResponseDto {

    private UUID id;
    private UUID advertisementId;
    private UUID senderId;
    private UUID recipientId;
    private String messageText;
    private LocalDateTime messageDate;
    private boolean isRead;
}
