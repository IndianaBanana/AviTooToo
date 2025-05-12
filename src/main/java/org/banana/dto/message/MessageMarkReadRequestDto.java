package org.banana.dto.message;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageMarkReadRequestDto {

    @NotNull
    private UUID secondUserId;
    private UUID advertisementId;
    @NotNull
    private LocalDateTime upToDateTime;
}
