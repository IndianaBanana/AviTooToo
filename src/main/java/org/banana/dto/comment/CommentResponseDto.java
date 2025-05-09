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
}
