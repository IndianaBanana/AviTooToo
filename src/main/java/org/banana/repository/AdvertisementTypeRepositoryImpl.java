package org.banana.repository;

import lombok.extern.slf4j.Slf4j;
import org.banana.dto.advertisement.type.AdvertisementTypeDto;
import org.banana.entity.AdvertisementType;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@Slf4j
public class AdvertisementTypeRepositoryImpl extends AbstractCrudRepositoryImpl<AdvertisementType, UUID> implements AdvertisementTypeRepository {


    private static final String EXISTS_BY_NAME = "select 1 from AdvertisementType a where lower(a.name) = lower(:name)";
    private static final String FIND_ALL_DTO = "select new org.banana.dto.advertisement.type.AdvertisementTypeDto(a.id, a.name) from AdvertisementType a";
    private static final String FIND_ALL_DTO_BY_NAME = FIND_ALL_DTO + " where lower(a.name) like lower(:pattern) escape '\\'";

    public AdvertisementTypeRepositoryImpl() {
        super(AdvertisementType.class);
    }

    @Override
    public List<AdvertisementTypeDto> findByNameLike(String pattern) {
        log.info("findByNameLike({}) in {}", pattern, getClass().getSimpleName());
        return getSession().createQuery(FIND_ALL_DTO_BY_NAME, AdvertisementTypeDto.class)
                .setParameter("pattern", "%" + pattern + "%")
                .getResultList();
    }

    @Override
    public List<AdvertisementTypeDto> findAllDto() {
        log.info("findAllDto() in {}", getClass().getSimpleName());
        return getSession().createQuery(FIND_ALL_DTO, AdvertisementTypeDto.class).getResultList();
    }

    @Override
    public boolean existsByName(String name) {
        log.info("existsByName({}) in {}", name, getClass().getSimpleName());
        Integer result = getSession().createQuery(EXISTS_BY_NAME, Integer.class)
                .setParameter("name", name)
                .getSingleResultOrNull();
        return result != null;
    }
}
