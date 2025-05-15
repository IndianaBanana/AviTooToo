package org.banana.dto.rating;

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
public class RatingDto {

    @NotNull
    private UUID ratedUserId;

    @Min(1)
    @Max(5)
    @NotNull
    private Short ratingValue;
}
