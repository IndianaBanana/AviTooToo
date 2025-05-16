package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.advertisement.type.AdvertisementTypeDto;
import org.banana.dto.advertisement.type.AdvertisementTypeMapper;
import org.banana.entity.AdvertisementType;
import org.banana.exception.AdvertisementTypeAlreadyExistsException;
import org.banana.repository.AdvertisementTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementTypeService {

    private final AdvertisementTypeRepository advertisementTypeRepository;
    private final AdvertisementTypeMapper advertisementTypeMapper;

    @Transactional(readOnly = true)
    public List<AdvertisementTypeDto> findAll() {
        log.info("findAll() in {}", getClass().getSimpleName());
        return advertisementTypeRepository.findAllDto();
    }

    @Transactional(readOnly = true)
    public List<AdvertisementTypeDto> findByNameLike(String pattern) {
        log.info("findByNameLike({}) in {}", pattern, getClass().getSimpleName());
        log.debug("pattern before: {}", pattern);
        pattern = pattern
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("%", "\\%");

        log.debug("pattern after: {}", pattern);
        return advertisementTypeRepository.findByNameLike(pattern);
    }

    @Transactional
    public AdvertisementTypeDto addAdvertisementType(String name) {
        log.info("addAdvertisementType({}) in {}", name, getClass().getSimpleName());

        if (advertisementTypeRepository.existsByName(name))
            throw new AdvertisementTypeAlreadyExistsException(name);

        return advertisementTypeMapper
                .advertisementTypeToAdvertisementTypeDto(advertisementTypeRepository.save(new AdvertisementType(name)));
    }
}
