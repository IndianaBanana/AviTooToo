package org.banana.repository;

import org.banana.entity.rating.Rating;
import org.banana.entity.rating.RatingId;
import org.banana.repository.crud.CrudRepository;

public interface RatingRepository extends CrudRepository<Rating, RatingId> {

}
