package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.city.CityDto;
import org.banana.dto.city.CityMapper;
import org.banana.entity.City;
import org.banana.exception.CityAlreadyExistsException;
import org.banana.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Override
    public List<CityDto> findAll() {
        log.info("findAll() in {}", getClass().getSimpleName());
        return cityRepository.findAllDto();
    }

    @Override
    public List<CityDto> findByNameLike(String pattern) {
        log.info("findByNameLike({}) in {}", pattern, getClass().getSimpleName());
        return cityRepository.findByNameLike(pattern);
    }

    @Override
    @Transactional
    public CityDto addCity(String name) {
        log.info("addCity({}) in {}", name, getClass().getSimpleName());

        if (cityRepository.existsByName(name))
            throw new CityAlreadyExistsException(name);

        return cityMapper.cityToCityDto(cityRepository.save(new City(name)));
    }
}
