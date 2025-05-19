package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.rating.RatingDto;
import org.banana.entity.rating.Rating;
import org.banana.entity.rating.RatingId;
import org.banana.exception.UserNotFoundException;
import org.banana.exception.UserRatesTheSameUserException;
import org.banana.repository.RatingRepository;
import org.banana.repository.UserRatingViewRepository;
import org.banana.repository.UserRepository;
import org.banana.util.SecurityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private static final String RATE_MESSAGE = "User rating will be updated in 15 minutes.";
    private final RatingRepository ratingRepository;
    private final UserRatingViewRepository userRatingViewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public String addRating(RatingDto dto) {
        log.info("addRating({}) in {}", dto, getClass().getSimpleName());
        UUID currentUserId = SecurityUtils.getCurrentUserPrincipal().getId();
        UUID ratedUser = dto.getRatedUserId();

        if (currentUserId.equals(ratedUser)) throw new UserRatesTheSameUserException();

        if (!userRepository.existsById(ratedUser)) throw new UserNotFoundException(ratedUser);

        Rating rating = ratingRepository.save(new Rating(ratedUser, currentUserId, dto.getRatingValue()));

        log.info("rating rating: {}", rating);
        return RATE_MESSAGE;
    }

    @Override
    @Transactional
    public String deleteRating(UUID ratedUserId) {
        log.info("deleteRating({}) in {}", ratedUserId, getClass().getSimpleName());
        UUID currentUserId = SecurityUtils.getCurrentUserPrincipal().getId();

        if (currentUserId.equals(ratedUserId)) throw new UserRatesTheSameUserException();

        if (!userRepository.existsById(ratedUserId)) throw new UserNotFoundException(ratedUserId);

        ratingRepository.deleteById(new RatingId(ratedUserId, currentUserId));
        log.info("rating deleted");
        return RATE_MESSAGE;
    }

    @Scheduled(initialDelayString = "${rating.view.update.initial-delay:10000}",
            fixedRateString = "${rating.view.update.rate:900000}")
    @Transactional
    @Override
    public void updateActualInformationAboutRating() {
        log.info("Refreshing materialized view user_rating_view...");
        userRatingViewRepository.updateView();
    }
}
