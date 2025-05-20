package org.banana.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для пометки сообщений как прочитанных")
public class MessageMarkReadRequestDto {

    @Schema(description = "ID собеседника", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull
    private UUID secondUserId;

    @Schema(description = "ID объявления (для сообщений по объявлению)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID advertisementId;

    @Schema(description = "Верхняя граница времени для пометки как прочитано", example = "2023-10-05T14:30:00")
    @NotNull
    private LocalDateTime upToDateTime;

    @Schema(description = "Верхний ID сообщения для пометки как прочитано", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull
    private UUID upToMessageId;
}
