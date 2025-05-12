package org.banana.repository;

import lombok.extern.slf4j.Slf4j;
import org.banana.entity.City;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@Slf4j
public class CityRepositoryImpl extends AbstractCrudRepositoryImpl<City, UUID> implements CityRepository {

    private static final String EXISTS_BY_NAME = "SELECT 1 FROM City c WHERE LOWER(c.name) = LOWER(:name)";


    public CityRepositoryImpl() {
        super(City.class);
    }

    @Override
    public List<City> findByNameLike(String pattern) {
        return getSession().createQuery("SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(:pattern) ESCAPE '\\'", City.class)
                .setParameter("pattern", pattern + "%")
                .getResultList();
    }

    @Override
    public boolean existsByName(String name) {
        log.debug("entering `existsByName` method in {}", this.getClass().getSimpleName());
        Integer result = getSession().createQuery(EXISTS_BY_NAME, Integer.class)
                .setParameter("name", name)
                .getSingleResultOrNull();
        return result != null;
    }
}
