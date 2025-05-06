package org.banana.service;

import lombok.RequiredArgsConstructor;
import org.banana.dto.city.CityDto;
import org.banana.dto.city.CityMapper;
import org.banana.entity.City;
import org.banana.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Transactional(readOnly = true)
    public List<CityDto> findAll() {
        return cityMapper.citiesToCityDtos(cityRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<CityDto> findByNameLike(String pattern) {
        return cityMapper.citiesToCityDtos(cityRepository.findByNameLike(pattern));
    }

    @Transactional
    public CityDto addCity(String name) {
        return cityMapper.cityToCityDto(cityRepository.save(new City(name)));
    }
}
