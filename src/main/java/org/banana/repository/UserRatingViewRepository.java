package org.banana.repository;

import org.banana.entity.UserRatingView;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by Banana on 04.05.2025
 */
public interface UserRatingViewRepository {

    Optional<UserRatingView> findById(UUID userId);

    void updateView();
}
