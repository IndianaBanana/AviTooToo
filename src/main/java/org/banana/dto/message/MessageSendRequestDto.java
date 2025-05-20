package org.banana.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "DTO для отправки сообщения")
public class MessageSendRequestDto {

    @Schema(description = "ID объявления (для сообщений по объявлению)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID advertisementId;

    @Schema(description = "ID получателя сообщения", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull
    private UUID recipientId;

    @Schema(description = "Текст сообщения", example = "Здравствуйте, интересует гараж...")
    @NotBlank
    private String messageText;
}
