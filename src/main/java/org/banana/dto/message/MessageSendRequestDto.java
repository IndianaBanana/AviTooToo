package org.banana.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageSendRequestDto {

    private UUID advertisementId;

    @NotNull
    private UUID recipientId;

    @NotBlank
    private String messageText;
}
