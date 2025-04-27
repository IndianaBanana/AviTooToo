package org.banana.dto.message;

import lombok.Data;
import org.banana.dto.user.UserDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageResponseDto {

    private UUID messageId;
    private UUID advertisementId;
    private UserDto senderId;
    private UserDto recipientId;
    private String messageText;
    private LocalDateTime messageDate;
    private boolean isRead;
}
