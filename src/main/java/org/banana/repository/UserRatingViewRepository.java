package org.banana.repository;

import org.banana.entity.UserRatingView;

import java.util.Optional;
import java.util.UUID;

public interface UserRatingViewRepository {

    /**
     * @param userId идентификатор юзера для которого нужен рейтинг
     * @return Optional<UserRatingView> если у юзера нет рейтинга, то Optional пустой
     */
    Optional<UserRatingView> findById(UUID userId);

    /**
     * Обновляет материализованное представление рейтинга
     */
    void updateView();
}
