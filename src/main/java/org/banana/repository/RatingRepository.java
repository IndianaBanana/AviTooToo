package org.banana.repository;

import org.banana.entity.Rating;
import org.banana.entity.RatingId;
import org.banana.repository.crud.CrudRepository;

public interface RatingRepository extends CrudRepository<Rating, RatingId> {

}
