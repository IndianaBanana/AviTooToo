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
    private UUID parentComment;
    @NotBlank
    private String commentText;
}
