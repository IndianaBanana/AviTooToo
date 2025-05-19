package org.banana.service;

import org.banana.dto.city.CityDto;
import org.banana.dto.city.CityMapper;
import org.banana.entity.City;
import org.banana.exception.CityAlreadyExistsException;
import org.banana.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CityMapper cityMapper;

    @InjectMocks
    private CityServiceImpl cityService;

    private List<CityDto> cityDtoList;

    @BeforeEach
    void setUp() {
        CityDto dto1 = new CityDto("New York");
        CityDto dto2 = new CityDto("Los Angeles");
        cityDtoList = List.of(dto1, dto2);
    }

    @Test
    void findAll_whenCalled_thenReturnsAllCities() {
        when(cityRepository.findAllDto()).thenReturn(cityDtoList);

        List<CityDto> result = cityService.findAll();

        assertEquals(cityDtoList, result);
        verify(cityRepository).findAllDto();
    }

    @Test
    void findByNameLike_whenCalledWithPattern_thenReturnsMappedDtos() {
        String inputPattern = "%_test\\";
        List<CityDto> cityDtos = List.of(new CityDto("Test City"));

        when(cityRepository.findByNameLike(inputPattern)).thenReturn(cityDtos);

        List<CityDto> result = cityService.findByNameLike(inputPattern);

        assertEquals(cityDtos, result);
    }

    @Test
    void addCity_whenCalledWithName_thenSavesAndReturnsCityDto() {
        String name = "Chicago";
        City savedCity = new City(name);
        CityDto savedDto = new CityDto(name);

        when(cityRepository.save(any(City.class))).thenReturn(savedCity);
        when(cityMapper.cityToCityDto(savedCity)).thenReturn(savedDto);
        when(cityRepository.existsByName(name)).thenReturn(false);

        CityDto result = cityService.addCity(name);

        assertEquals(name, result.getName());
        verify(cityRepository).save(any(City.class));
        verify(cityMapper).cityToCityDto(savedCity);
    }


    @Test
    void addCity_whenCalledWithNameThatAlreadyExists_thenThrowsException() {
        String name = "Chicago";

        when(cityRepository.existsByName(name)).thenReturn(true);

        assertThrows(CityAlreadyExistsException.class, () -> cityService.addCity(name));

        verify(cityRepository).existsByName(name);
        verify(cityRepository, never()).save(any(City.class));
    }
}
