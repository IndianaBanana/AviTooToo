package org.banana.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequestDto {

    @NotNull
    private UUID advertisementId;

    private UUID parentCommentId;

    @NotBlank
    private String commentText;
}
