package org.banana.repository;

import org.banana.entity.Advertisement;
import org.banana.repository.crud.CrudRepository;

import java.util.UUID;

public interface AdvertisementRepository extends CrudRepository<Advertisement, UUID> {

}
