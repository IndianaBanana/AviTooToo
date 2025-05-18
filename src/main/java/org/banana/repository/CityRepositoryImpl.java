package org.banana.repository;

import lombok.extern.slf4j.Slf4j;
import org.banana.dto.city.CityDto;
import org.banana.entity.City;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@Slf4j
public class CityRepositoryImpl extends AbstractCrudRepositoryImpl<City, UUID> implements CityRepository {

    private static final String EXISTS_BY_NAME = "select 1 from City c where lower(c.name) = lower(:name)";
    private static final String FIND_ALL_DTO = "select new org.banana.dto.city.CityDto(c.id, c.name) from City c";
    private static final String FIND_ALL_DTO_BY_NAME = FIND_ALL_DTO + " where lower(c.name) like lower(:pattern) escape '\\'";

    public CityRepositoryImpl() {
        super(City.class);
    }

    @Override
    public List<CityDto> findAllDto() {
        log.info("findAllDto() in {}", this.getClass().getSimpleName());
        return getSession().createQuery(FIND_ALL_DTO, CityDto.class).getResultList();
    }


    @Override
    public List<CityDto> findByNameLike(String pattern) {
        log.info("findByNameLike() in {}", this.getClass().getSimpleName());
        return getSession().createQuery(FIND_ALL_DTO_BY_NAME, CityDto.class)
                .setParameter("pattern", pattern + "%")
                .getResultList();
    }

    @Override
    public boolean existsByName(String name) {
        log.info("entering `existsByName` method in {}", this.getClass().getSimpleName());
        Integer result = getSession().createQuery(EXISTS_BY_NAME, Integer.class)
                .setParameter("name", name)
                .getSingleResultOrNull();
        return result != null;
    }
}
