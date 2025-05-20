package org.banana.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Фильтр для получения сообщений из чата")
public class MessageFilterDto {

    @Schema(description = "Лимит сообщений на странице (10-100)", defaultValue = "10")
    @Min(10)
    @Max(100)
    private int limit = 10;

    @Schema(description = "Фильтрация сообщений до/после курсора", example = "true")
    private Boolean isBefore;

    @Schema(hidden = true)
    @Null
    private Long unreadMessagesCount;

    @Schema(description = "Дата и время для курсора пагинации", example = "2023-10-05T14:30:00")
    private LocalDateTime cursorDateTime;

    @Schema(description = "ID сообщения для курсора пагинации", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID cursorMessageId;

    @Schema(description = "ID объявления для фильтрации", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID advertisementId;

    @Schema(hidden = true)
    @Null
    private UUID currentUserId;

    @Schema(description = "ID собеседника", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull
    private UUID secondUserId;
}
