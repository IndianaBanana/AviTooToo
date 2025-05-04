package org.banana.controller;

import org.banana.dto.rating.RatingDto;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Created by Banana on 04.05.2025
 */
public interface RatingController {

    ResponseEntity<String> rateUser(RatingDto ratingDto);

    ResponseEntity<String> removeRating(UUID userId);
}
