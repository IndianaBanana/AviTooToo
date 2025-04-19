package org.banana.repository;

import org.banana.entity.AdvertisementType;
import org.banana.repository.crud.CrudRepository;

import java.util.UUID;

public interface AdvertisementTypeRepository extends CrudRepository<AdvertisementType, UUID> {

}
