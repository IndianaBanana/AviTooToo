package org.banana.repository;

import org.banana.entity.City;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class CityRepositoryImpl extends AbstractCrudRepositoryImpl<City, UUID> implements CityRepository {

    public CityRepositoryImpl() {
        super(City.class);
    }

    @Override
    public List<City> findByNameLike(String pattern) {
        return getSession().createQuery("SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(:pattern) ESCAPE '\\'", City.class)
                .setParameter("pattern", pattern + "%")
                .getResultList();
    }
}
