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
import org.banana.security.dto.UserPrincipal;
import org.banana.util.SecurityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by Banana on 25.04.2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private static final String RATE_MESSAGE = "Thanks for rating! User rating will be update in 15 minutes.";
    private final RatingRepository ratingRepository;
    private final UserRatingViewRepository userRatingViewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public String rateUser(RatingDto dto) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();
        if (currentUser.getId().equals(dto.getUserId())) throw new UserRatesTheSameUserException();
        if (!userRepository.existsById(dto.getUserId())) throw new UserNotFoundException(dto.getUserId());
        ratingRepository.save(new Rating(dto.getUserId(), currentUser.getId(), dto.getRatingValue()));
        return RATE_MESSAGE;
    }

    @Override
    @Transactional
    public String removeRating(UUID userId) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();
        if (currentUser.getId().equals(userId)) throw new UserRatesTheSameUserException();
        if (!userRepository.existsById(userId)) throw new UserNotFoundException(userId);
        ratingRepository.deleteById(new RatingId(userId, currentUser.getId()));
        return RATE_MESSAGE;
    }

    @Scheduled(initialDelayString = "${rating.view.update.initial-delay:10000}",
            fixedRateString = "${rating.view.update.rate:900000}")
    @Transactional
    @Override
    public void updateActualInformationAboutRating() {
        log.info("Refreshing materialized view user_rating_view…");
        userRatingViewRepository.updateView();
    }
}
