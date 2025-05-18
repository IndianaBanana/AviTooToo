package org.banana.service;

import org.banana.dto.advertisement.type.AdvertisementTypeDto;

import java.util.List;


public interface AdvertisementTypeService {

    List<AdvertisementTypeDto> findAll();

    List<AdvertisementTypeDto> findByNameLike(String pattern);

    AdvertisementTypeDto addAdvertisementType(String name);
}
