package org.banana.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.banana.dto.rating.RatingDto;
import org.banana.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rating")
@RequiredArgsConstructor
public class RatingControllerImpl implements RatingController {

    private final RatingService ratingService;

    @PostMapping("")
    @Override
    public ResponseEntity<String> rateUser(@Valid @RequestBody RatingDto ratingDto) {
        return ResponseEntity.ok(ratingService.rateUser(ratingDto));
    }

    @DeleteMapping("")
    @Override
    public ResponseEntity<String> removeRating(@Valid @PathVariable UUID userId) {
        return ResponseEntity.ok(ratingService.removeRating(userId));
    }
}
