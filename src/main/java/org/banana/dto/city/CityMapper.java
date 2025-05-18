package org.banana.dto.city;

import org.banana.entity.City;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface CityMapper {

    CityDto cityToCityDto(City city);

    List<CityDto> citiesToCityDtos(List<City> cities);
}
