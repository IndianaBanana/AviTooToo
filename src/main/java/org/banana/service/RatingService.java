package org.banana.service;

import org.banana.dto.rating.RatingDto;

import java.util.UUID;

/**
 * Created by Banana on 04.05.2025
 */
public interface RatingService {

    String rateUser(RatingDto dto);

    String removeRating(UUID userId);

    void updateAgregatedDataAboutUserRatings();

//    void updateRating(UUID userId, short ratingValue);
}
