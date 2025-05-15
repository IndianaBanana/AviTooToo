package org.banana.repository;

import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.entity.UserRatingView;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRatingViewRepositoryImpl implements UserRatingViewRepository {

    @PersistenceContext
    private Session session;

    @Override
    public Optional<UserRatingView> findById(UUID userId) {
        log.info("findById({})", userId);
        return Optional.ofNullable(session.find(UserRatingView.class, userId));
    }

    @Override
    public void updateView() {
        log.info("updateView()");
        session.createNativeMutationQuery("REFRESH MATERIALIZED VIEW CONCURRENTLY user_rating_view").executeUpdate();
    }
}
