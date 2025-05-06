package org.banana.dto.city;

import org.banana.entity.City;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Created by Banana on 25.04.2025
 */
@Mapper(componentModel = "spring")
public interface CityMapper {

    CityDto cityToCityDto(City city);

    List<CityDto> citiesToCityDtos(List<City> cities);
}
