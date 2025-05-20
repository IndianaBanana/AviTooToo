package org.banana.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для создания/обновления комментария")
public class CommentRequestDto {

    @Schema(description = "UUID объявления", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull
    private UUID advertisementId;

    @Schema(description = "UUID родительского комментария (для ответов)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID parentCommentId;

    @Schema(description = "Текст комментария", example = "А вот был бы гараж...")
    @NotBlank
    private String commentText;
}
