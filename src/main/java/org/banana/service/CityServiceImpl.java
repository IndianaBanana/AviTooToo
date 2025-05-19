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
        List<CityDto> allDto = cityRepository.findAllDto();
        log.info("cities found quantity: {}", allDto.size());
        return allDto;
    }

    @Override
    public List<CityDto> findByNameLike(String pattern) {
        log.info("findByNameLike({}) in {}", pattern, getClass().getSimpleName());
        List<CityDto> byNameLike = cityRepository.findByNameLike(pattern);
        log.info("cities found by name quantity: {}", byNameLike.size());
        return byNameLike;
    }

    @Override
    @Transactional
    public CityDto addCity(String name) {
        log.info("addCity({}) in {}", name, getClass().getSimpleName());

        if (cityRepository.existsByName(name))
            throw new CityAlreadyExistsException(name);

        City city = cityRepository.save(new City(name));
        log.info("city saved: {}", city);
        return cityMapper.cityToCityDto(city);
    }
}
