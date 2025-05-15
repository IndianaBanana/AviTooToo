package org.banana.repository;

import org.banana.dto.advertisement.type.AdvertisementTypeDto;
import org.banana.entity.AdvertisementType;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AdvertisementTypeRepository extends CrudRepository<AdvertisementType, UUID> {

    List<AdvertisementTypeDto> findByNameLike(String pattern);

    List<AdvertisementTypeDto> findAllDto();

    boolean existsByName(String name);
}
