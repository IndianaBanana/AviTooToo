package org.banana.repository;

import org.banana.entity.Advertisement;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class AdvertisementRepositoryImpl extends AbstractCrudRepositoryImpl<Advertisement, UUID> implements AdvertisementRepository {

    public AdvertisementRepositoryImpl() {
        super(Advertisement.class);
    }
}
