package org.banana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.banana.config.SecurityConfig;
import org.banana.dto.rating.RatingDto;
import org.banana.exception.UserNotFoundException;
import org.banana.exception.UserRatesTheSameUserException;
import org.banana.security.service.JwtService;
import org.banana.service.RatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
@Import(SecurityConfig.class)
class RatingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RatingService ratingService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // --- addRating ---

    @Test
    @WithMockUser
    void rateUser_whenValid_thenCreated() throws Exception {
        RatingDto dto = new RatingDto();
        dto.setRatedUserId(UUID.randomUUID());
        dto.setRatingValue((short) 4);

        when(ratingService.addRating(dto)).thenReturn("User rating will be update in 15 minutes.");

        mvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("User rating will be update in 15 minutes.")));
    }

    @Test
    @WithMockUser
    void rateUser_whenInvalidDto_thenBadRequest() throws Exception {
        RatingDto dto = new RatingDto(); // missing userId and ratingValue

        mvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("ratedUserId")))
                .andExpect(content().string(containsString("ratingValue")));
    }

    @Test
    @WithMockUser
    void rateUser_whenSameUser_thenConflict() throws Exception {
        RatingDto dto = new RatingDto(UUID.randomUUID(), (short) 3);

        doThrow(new UserRatesTheSameUserException()).when(ratingService).addRating(dto);

        mvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void rateUser_whenUserNotFound_thenNotFound() throws Exception {
        RatingDto dto = new RatingDto(UUID.randomUUID(), (short) 3);

        doThrow(new UserNotFoundException(dto.getRatedUserId())).when(ratingService).addRating(dto);

        mvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void rateUser_whenAnonymous_thenUnauthorized() throws Exception {
        RatingDto dto = new RatingDto(UUID.randomUUID(), (short) 5);

        mvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // --- deleteRating ---

    @Test
    @WithMockUser
    void removeRating_whenValid_thenOk() throws Exception {
        UUID userId = UUID.randomUUID();
        when(ratingService.deleteRating(userId)).thenReturn("User rating will be update in 15 minutes.");

        mvc.perform(delete("/api/v1/rating/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User rating will be update in 15 minutes.")));
    }

    @Test
    @WithMockUser
    void removeRating_whenSameUser_thenConflict() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new UserRatesTheSameUserException()).when(ratingService).deleteRating(userId);

        mvc.perform(delete("/api/v1/rating/{userId}", userId))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void removeRating_whenUserNotFound_thenNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new UserNotFoundException(userId)).when(ratingService).deleteRating(userId);

        mvc.perform(delete("/api/v1/rating/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void removeRating_whenAnonymous_thenUnauthorized() throws Exception {
        mvc.perform(delete("/api/v1/rating/{userId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
