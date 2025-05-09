package org.banana.repository;

import org.banana.entity.AdvertisementType;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AdvertisementTypeRepositoryImpl extends AbstractCrudRepositoryImpl<AdvertisementType, UUID> implements AdvertisementTypeRepository {

    public AdvertisementTypeRepositoryImpl() {
        super(AdvertisementType.class);
    }

    @Override
    public List<AdvertisementType> findByNameLike(String pattern) {
        return getSession().createQuery("SELECT a FROM AdvertisementType a WHERE LOWER(a.name) LIKE LOWER(:pattern) ESCAPE '\\'", AdvertisementType.class)
                .setParameter("pattern", "%" + pattern + "%")
                .getResultList();
    }
}
