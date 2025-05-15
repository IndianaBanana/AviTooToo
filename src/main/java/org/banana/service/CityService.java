package org.banana.service;

import lombok.RequiredArgsConstructor;
import org.banana.dto.city.CityDto;
import org.banana.dto.city.CityMapper;
import org.banana.entity.City;
import org.banana.exception.CityAlreadyExistsException;
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
        return cityRepository.findAllDto();
    }

    @Transactional(readOnly = true)
    public List<CityDto> findByNameLike(String pattern) {
        String safePattern = pattern.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("%", "\\%");

        return cityRepository.findByNameLike(safePattern);
    }

    @Transactional
    public CityDto addCity(String name) {
        if (cityRepository.existsByName(name)) {
            throw new CityAlreadyExistsException(name);
        }
        return cityMapper.cityToCityDto(cityRepository.save(new City(name)));
    }
}
