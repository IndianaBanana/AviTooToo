package org.banana.repository;

import org.banana.entity.City;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class CityRepositoryImpl extends AbstractCrudRepositoryImpl<City, UUID> implements CityRepository {

    public CityRepositoryImpl() {
        super(City.class);
    }
}
