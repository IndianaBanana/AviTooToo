package org.banana.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RatingDto {

    @NotNull
    private UUID userId;
    //    private UUID raterId;
    @Min(1)
    @Max(5)
    @NotNull
    private Short ratingValue;
}
