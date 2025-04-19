package org.banana.dto.rating;

import lombok.Data;

import java.util.UUID;

@Data
public class RatingDto {

    private UUID userId;
    private UUID raterId;
    private short ratingValue;
}
