package org.banana.repository;

import org.banana.entity.AdvertisementType;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class AdvertisementTypeRepositoryImpl extends AbstractCrudRepositoryImpl<AdvertisementType, UUID> implements AdvertisementTypeRepository {

    public AdvertisementTypeRepositoryImpl() {
        super(AdvertisementType.class);
    }
}
