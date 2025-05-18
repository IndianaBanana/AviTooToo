package org.banana.service;

import org.banana.dto.city.CityDto;

import java.util.List;


public interface CityService {

    List<CityDto> findAll();

    List<CityDto> findByNameLike(String pattern);

    CityDto addCity(String name);
}
