package org.banana.dto.comment;

import lombok.Data;
import org.banana.dto.user.UserResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommentResponseDto {

    private UUID commentId;
    private UUID advertisementId;
    private UserResponseDto commenter;
    private UUID rootCommentId;
    private CommentResponseDto childComment;
    private String commentText;
    private LocalDateTime commentDate;
}
