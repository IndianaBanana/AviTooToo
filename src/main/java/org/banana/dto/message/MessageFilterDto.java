package org.banana.dto.message;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by Banana on 07.05.2025
 */
@Data
public class MessageFilterDto {

    @Min(1)
    Integer limit;
    Boolean isBefore;
    @Null
    Boolean isCurrentUserHasUnreadMessages;
    LocalDateTime cursorDateTime;
    UUID cursorMessageId;
    UUID advertisementId;
    @Null
    UUID currentUserId;
    @NotNull
    UUID secondUserId;
}
