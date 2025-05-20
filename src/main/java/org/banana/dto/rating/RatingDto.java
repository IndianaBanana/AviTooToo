package org.banana.dto.rating;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для оценки пользователя")
public class RatingDto {

    @Schema(description = "UUID оцениваемого пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull
    private UUID ratedUserId;

    @Schema(description = "Значение оценки (1-5)", minimum = "1", maximum = "5", example = "5")
    @Min(1)
    @Max(5)
    @NotNull
    private Short ratingValue;
}
