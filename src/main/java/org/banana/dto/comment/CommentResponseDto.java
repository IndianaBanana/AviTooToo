package org.banana.dto.comment;

import lombok.Data;
import org.banana.dto.user.UserDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommentResponseDto {

    private UUID commentId;
    private UUID advertisementId;
    private UserDto commenter;
    private UUID rootCommentId;
    private CommentResponseDto childComment;
    private String commentText;
    private LocalDateTime commentDate;
}
