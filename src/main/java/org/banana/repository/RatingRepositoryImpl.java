package org.banana.repository;

import org.banana.entity.rating.Rating;
import org.banana.entity.rating.RatingId;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class RatingRepositoryImpl extends AbstractCrudRepositoryImpl<Rating, RatingId> implements RatingRepository {

    public RatingRepositoryImpl() {
        super(Rating.class);
    }
}
