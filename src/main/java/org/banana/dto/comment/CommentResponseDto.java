package org.banana.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.banana.dto.user.UserCommenterResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для отображения комментария")
public class CommentResponseDto {

    @Schema(description = "UUID комментария", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "UUID объявления", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID advertisementId;

    @Schema(description = "Информация об авторе комментария")
    private UserCommenterResponseDto commenter;

    @Schema(description = "UUID корневого комментария", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID rootCommentId;

    @Schema(description = "UUID родительского комментария", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID parentCommentId;

    @Schema(description = "Текст комментария", example = "А вот был бы гараж...")
    private String commentText;

    @Schema(description = "Дата и время создания комментария", example = "2023-10-05T14:30:00")
    private LocalDateTime commentDate;

    public CommentResponseDto(UUID id, UUID advertisementId, UUID userId, String userFirstName, String userLastName, UUID rootCommentId, UUID parentCommentId, String commentText, LocalDateTime commentDate) {
        this.id = id;
        this.advertisementId = advertisementId;
        this.rootCommentId = rootCommentId;
        this.parentCommentId = parentCommentId;
        this.commentText = commentText;
        this.commentDate = commentDate;
        if (userId != null) this.commenter = new UserCommenterResponseDto(userId, userFirstName, userLastName);
        else this.commenter = null;
    }
}
