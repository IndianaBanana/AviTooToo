package org.banana.repository;

import org.banana.entity.City;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface CityRepository extends CrudRepository<City, UUID> {

    List<City> findByNameLike(String pattern);
}
