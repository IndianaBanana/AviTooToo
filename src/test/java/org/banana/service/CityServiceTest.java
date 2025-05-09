package org.banana.service;

import org.banana.dto.city.CityDto;
import org.banana.dto.city.CityMapper;
import org.banana.entity.City;
import org.banana.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CityMapper cityMapper;

    @InjectMocks
    private CityService cityService;

    private List<City> cityList;
    private List<CityDto> cityDtoList;

    @BeforeEach
    void setUp() {
        City city1 = new City("New York");
        City city2 = new City("Los Angeles");
        cityList = List.of(city1, city2);

        CityDto dto1 = new CityDto("New York");
        CityDto dto2 = new CityDto("Los Angeles");
        cityDtoList = List.of(dto1, dto2);
    }

    @Test
    void findAll_whenCalled_thenReturnsAllCities() {
        when(cityRepository.findAll()).thenReturn(cityList);
        when(cityMapper.citiesToCityDtos(cityList)).thenReturn(cityDtoList);

        List<CityDto> result = cityService.findAll();

        assertEquals(cityDtoList, result);
        verify(cityRepository).findAll();
        verify(cityMapper).citiesToCityDtos(cityList);
    }

    @Test
    void findByNameLike_whenCalledWithPattern_thenReturnsMappedDtos() {
        // given
        String inputPattern = "%_test\\";
        String expectedEscapedPattern = "\\%\\_test\\\\";
        List<City> cities = List.of(new City("Test City"));
        List<CityDto> cityDtos = List.of(new CityDto("Test City"));

        when(cityRepository.findByNameLike(expectedEscapedPattern)).thenReturn(cities);
        when(cityMapper.citiesToCityDtos(cities)).thenReturn(cityDtos);

        // when
        List<CityDto> result = cityService.findByNameLike(inputPattern);

        // then
        assertEquals(cityDtos, result);
        verify(cityRepository).findByNameLike(expectedEscapedPattern);
        verify(cityMapper).citiesToCityDtos(cities);
    }

    @Test
    void addCity_whenCalledWithName_thenSavesAndReturnsCityDto() {
        String name = "Chicago";
        City savedCity = new City(name);
        CityDto savedDto = new CityDto(name);

        when(cityRepository.save(any(City.class))).thenReturn(savedCity);
        when(cityMapper.cityToCityDto(savedCity)).thenReturn(savedDto);

        CityDto result = cityService.addCity(name);

        assertEquals(name, result.getName());
        verify(cityRepository).save(any(City.class));
        verify(cityMapper).cityToCityDto(savedCity);
    }
}
