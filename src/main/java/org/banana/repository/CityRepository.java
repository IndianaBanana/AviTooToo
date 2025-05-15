package org.banana.repository;

import org.banana.dto.city.CityDto;
import org.banana.entity.City;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface CityRepository extends CrudRepository<City, UUID> {

    List<CityDto> findAllDto();

    List<CityDto> findByNameLike(String pattern);

    boolean existsByName(String name);
}
