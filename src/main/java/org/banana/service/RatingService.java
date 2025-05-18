package org.banana.service;

import org.banana.dto.rating.RatingDto;

import java.util.UUID;


public interface RatingService {

    String addRating(RatingDto dto);

    String deleteRating(UUID ratedUserId);

    void updateActualInformationAboutRating();
}
