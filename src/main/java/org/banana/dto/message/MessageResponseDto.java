package org.banana.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDto {

    private UUID id;
    private UUID advertisementId;
    private UUID senderId;
    private UUID recipientId;
    private String messageText;
    private LocalDateTime messageDateTime;
    private boolean isRead;
}
