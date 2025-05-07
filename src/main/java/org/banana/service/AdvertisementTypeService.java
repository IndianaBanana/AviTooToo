package org.banana.service;

import lombok.RequiredArgsConstructor;
import org.banana.dto.advertisement.type.AdvertisementTypeDto;
import org.banana.dto.advertisement.type.AdvertisementTypeMapper;
import org.banana.entity.AdvertisementType;
import org.banana.repository.AdvertisementTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvertisementTypeService {

    private final AdvertisementTypeRepository advertisementTypeRepository;
    private final AdvertisementTypeMapper advertisementTypeMapper;

    @Transactional(readOnly = true)
    public List<AdvertisementTypeDto> findAll() {
        return advertisementTypeMapper.advertisementTypesToAdvertisementTypeDtos(advertisementTypeRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<AdvertisementTypeDto> findByNameLike(String pattern) {
        return advertisementTypeMapper.advertisementTypesToAdvertisementTypeDtos(advertisementTypeRepository.findByNameLike(pattern));
    }

    @Transactional
    public AdvertisementTypeDto addAdvertisementType(String name) {
        return advertisementTypeMapper.advertisementTypeToAdvertisementTypeDto(advertisementTypeRepository.save(new AdvertisementType(name)));
    }
}
