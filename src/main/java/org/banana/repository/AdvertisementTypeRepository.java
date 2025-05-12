package org.banana.repository;

import org.banana.entity.AdvertisementType;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AdvertisementTypeRepository extends CrudRepository<AdvertisementType, UUID> {

    List<AdvertisementType> findByNameLike(String pattern);

    boolean existsByName(String name);
}
