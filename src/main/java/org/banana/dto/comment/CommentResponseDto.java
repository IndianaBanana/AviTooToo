package org.banana.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.banana.dto.user.UserCommenterResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {

    private UUID id;
    private UUID advertisementId;
    private UserCommenterResponseDto commenter;
    private UUID rootCommentId;
    private UUID parentCommentId;
    private String commentText;
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
