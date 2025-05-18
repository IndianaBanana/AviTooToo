package org.banana.controller;

import org.banana.dto.rating.RatingDto;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface RatingController {

    ResponseEntity<String> rateUser(RatingDto ratingDto);

    ResponseEntity<String> removeRating(UUID userId);
}
