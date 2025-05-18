package org.banana.dto.message;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MessageFilterValidation
public class MessageFilterDto {

    @Min(10)
    @Max(100)
    private int limit = 10;
    private Boolean isBefore;

    @Null
    private Long unreadMessagesCount;
    private LocalDateTime cursorDateTime;
    private UUID cursorMessageId;
    private UUID advertisementId;

    @Null
    private UUID currentUserId;

    @NotNull
    private UUID secondUserId;
}
