package org.banana.repository;

import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.banana.entity.UserRatingView;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRatingViewRepositoryImpl implements UserRatingViewRepository {

    @PersistenceContext
    private Session session;

    @Override
    public Optional<UserRatingView> findById(UUID userId) {
        return Optional.ofNullable(session.find(UserRatingView.class, userId));
    }

    @Override
    public void updateView() {
        session.createNativeMutationQuery("REFRESH MATERIALIZED VIEW CONCURRENTLY user_rating_view").executeUpdate();
    }
}
