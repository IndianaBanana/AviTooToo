package org.banana.service;

import org.banana.dto.rating.RatingDto;
import org.banana.entity.rating.Rating;
import org.banana.entity.rating.RatingId;
import org.banana.exception.UserNotFoundException;
import org.banana.exception.UserRatesTheSameUserException;
import org.banana.repository.RatingRepository;
import org.banana.repository.UserRatingViewRepository;
import org.banana.repository.UserRepository;
import org.banana.security.UserRole;
import org.banana.security.dto.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID raterId = UUID.randomUUID();
    private final short ratingValue = 4;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserRatingViewRepository userRatingViewRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    @BeforeEach
    void setupSecurityContext() {
        var principal = new UserPrincipal(raterId, "user", "123", "phone", "username", "password", UserRole.ROLE_USER);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addRating_whenGivenValidRatingDto_thenShouldSaveRating() {
        when(userRepository.existsById(userId)).thenReturn(true);
        RatingDto dto = new RatingDto(userId, ratingValue);

        String result = ratingService.addRating(dto);

        verify(ratingRepository).save(new Rating(userId, raterId, ratingValue));
        assertEquals("User rating will be updated in 15 minutes.", result);
    }

    @Test
    void addRatingException() {
        RatingDto dto = new RatingDto(raterId, ratingValue);

        assertThrows(UserRatesTheSameUserException.class, () -> ratingService.addRating(dto));
        verifyNoInteractions(ratingRepository);
    }

    @Test
    void addRatingNotFoundException() {
        when(userRepository.existsById(userId)).thenReturn(false);
        RatingDto dto = new RatingDto(userId, ratingValue);

        assertThrows(UserNotFoundException.class, () -> ratingService.addRating(dto));
        verifyNoInteractions(ratingRepository);
    }

    @Test
    void removeRating_whenGivenValidUserId_thenShouldDeleteRating() {
        when(userRepository.existsById(userId)).thenReturn(true);

        String result = ratingService.deleteRating(userId);

        verify(ratingRepository).deleteById(new RatingId(userId, raterId));
        assertEquals("User rating will be updated in 15 minutes.", result);
    }

    @Test
    void deleteRating_whenGivenRemovingYourself_thenShouldThrowUserRatesTheSameUserException() {
        assertThrows(UserRatesTheSameUserException.class, () -> ratingService.deleteRating(raterId));
        verifyNoInteractions(ratingRepository);
    }

    @Test
    void deleteRating_whenGivenNonExistentUser_thenShouldThrowUserNotFoundException() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> ratingService.deleteRating(userId));
        verifyNoInteractions(ratingRepository);
    }

    @Test
    void updateActualInformationAboutRating_whenGivenScheduledTask_thenShouldCallRepositoryMethod() {
        ratingService.updateActualInformationAboutRating();
        verify(userRatingViewRepository).updateView();
    }
}
