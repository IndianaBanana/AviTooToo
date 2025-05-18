package org.banana.repository;

import org.banana.entity.UserRatingView;

import java.util.Optional;
import java.util.UUID;

public interface UserRatingViewRepository {

    Optional<UserRatingView> findById(UUID userId);

    void updateView();
}
