package org.banana.repository;

import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.entity.Advertisement;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AdvertisementRepository extends CrudRepository<Advertisement, UUID> {


    List<Advertisement> findAllFiltered(AdvertisementFilterDto filter, int page, int size);

    List<Advertisement> findAllByUserId(UUID id);
}
