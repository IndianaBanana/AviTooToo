package org.banana.repository;

import org.banana.entity.Rating;
import org.banana.entity.RatingId;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class RatingRepositoryImpl extends AbstractCrudRepositoryImpl<Rating, RatingId> implements RatingRepository {

    public RatingRepositoryImpl() {
        super(Rating.class);
    }
}
