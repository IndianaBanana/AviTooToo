package org.banana.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequestDto {

    @NotNull
    private UUID advertisementId;

    @NotNull
    private UUID commenterId;

    @NotNull
    private UUID parentCommentId;
    //    @NotNull
//    private UUID rootCommentId;
    @NotBlank
    private String commentText;
}
