package org.banana.dto.message;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageMarkReadRequestDto {

    @NotNull
    private UUID secondUserId;
    private UUID advertisementId;
    @NotNull
    private LocalDateTime upToDateTime;
}
