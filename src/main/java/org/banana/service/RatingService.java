package org.banana.service;

import org.banana.dto.rating.RatingDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by Banana on 04.05.2025
 */
public interface RatingService {

    String rateUser(RatingDto dto);

    String removeRating(UUID userId);

    @Scheduled(initialDelayString = "${rating.view.update.initial-delay:10000}",
            fixedRateString = "${rating.view.update.rate:900000}")
    @Transactional
    void updateActualInformationAboutRating();

//    void updateAgregatedDataAboutUserRatings();

//    void updateRating(UUID userId, short ratingValue);
}
