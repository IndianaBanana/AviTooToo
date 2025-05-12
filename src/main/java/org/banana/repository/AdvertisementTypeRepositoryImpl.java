package org.banana.repository;

import lombok.extern.slf4j.Slf4j;
import org.banana.entity.AdvertisementType;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@Slf4j
public class AdvertisementTypeRepositoryImpl extends AbstractCrudRepositoryImpl<AdvertisementType, UUID> implements AdvertisementTypeRepository {


    private static final String EXISTS_BY_NAME = "SELECT 1 FROM AdvertisementType a WHERE LOWER(a.name) = LOWER(:name)";

    public AdvertisementTypeRepositoryImpl() {
        super(AdvertisementType.class);
    }

    @Override
    public List<AdvertisementType> findByNameLike(String pattern) {
        return getSession().createQuery("SELECT a FROM AdvertisementType a WHERE LOWER(a.name) LIKE LOWER(:pattern) ESCAPE '\\'", AdvertisementType.class)
                .setParameter("pattern", "%" + pattern + "%")
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
