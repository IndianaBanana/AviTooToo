package org.banana.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Информация о сообщении")
public class MessageResponseDto {

    @Schema(description = "ID сообщения", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID связанного объявления", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID advertisementId;

    @Schema(description = "ID отправителя", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID senderId;

    @Schema(description = "ID получателя", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID recipientId;

    @Schema(description = "Текст сообщения", example = "Добрый день, гараж хочу...")
    private String messageText;

    @Schema(description = "Дата и время отправки", example = "2023-10-05T14:30:00")
    private LocalDateTime messageDateTime;

    @Schema(description = "Флаг прочтения сообщения", example = "true")
    private boolean isRead;
}
