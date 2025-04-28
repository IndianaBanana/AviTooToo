package org.banana.dto.message;

import lombok.Data;
import org.banana.dto.user.UserResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageResponseDto {

    private UUID messageId;
    private UUID advertisementId;
    private UserResponseDto senderId;
    private UserResponseDto recipientId;
    private String messageText;
    private LocalDateTime messageDate;
    private boolean isRead;
}
