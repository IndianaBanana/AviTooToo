package org.banana.service;

import org.banana.dto.advertisement.type.AdvertisementTypeDto;
import org.banana.dto.advertisement.type.AdvertisementTypeMapper;
import org.banana.entity.AdvertisementType;
import org.banana.exception.AdvertisementTypeAlreadyExistsException;
import org.banana.repository.AdvertisementTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvertisementTypeServiceTest {

    @Mock
    private AdvertisementTypeRepository advertisementTypeRepository;

    @Mock
    private AdvertisementTypeMapper advertisementTypeMapper;

    @InjectMocks
    private AdvertisementTypeServiceImpl advertisementTypeService;

    @Test
    void findAll_whenCalled_thenReturnDtos() {
        // given
        List<AdvertisementTypeDto> dtos = List.of(new AdvertisementTypeDto("Sell"));

        when(advertisementTypeRepository.findAllDto()).thenReturn(dtos);

        // when
        List<AdvertisementTypeDto> result = advertisementTypeService.findAll();

        // then
        assertEquals(dtos, result);
        verify(advertisementTypeRepository).findAllDto();
    }

    @Test
    void findByNameLike_whenPatternEscaped_thenReturnMappedDtos() {
        // given
        String inputPattern = "%_type\\";
        List<AdvertisementTypeDto> dtos = List.of(new AdvertisementTypeDto("Rent"));

        when(advertisementTypeRepository.findByNameLike(inputPattern)).thenReturn(dtos);

        // when
        List<AdvertisementTypeDto> result = advertisementTypeService.findByNameLike(inputPattern);

        // then
        assertEquals(dtos, result);
    }

    @Test
    void addAdvertisementType_whenCalled_thenSaveEntityAndReturnMappedDto() {
        // given
        String name = "Exchange";
        AdvertisementType savedEntity = new AdvertisementType(name);
        AdvertisementTypeDto expectedDto = new AdvertisementTypeDto(name);

        when(advertisementTypeRepository.save(any(AdvertisementType.class))).thenReturn(savedEntity);
        when(advertisementTypeMapper.advertisementTypeToAdvertisementTypeDto(savedEntity)).thenReturn(expectedDto);
        when(advertisementTypeRepository.existsByName(name)).thenReturn(false);

        // when
        AdvertisementTypeDto result = advertisementTypeService.addAdvertisementType(name);

        // then
        assertEquals(expectedDto, result);
        verify(advertisementTypeRepository).save(argThat(type -> type.getName().equals(name)));
        verify(advertisementTypeMapper).advertisementTypeToAdvertisementTypeDto(savedEntity);
    }

    @Test
    void addAdvertisementType_whenNameAlreadyExists_thenThrowAdvertisementTypeAlreadyExistsException() {
        // given
        String name = "Exchange";
        when(advertisementTypeRepository.existsByName(name)).thenReturn(true);

        // when and then
        assertThrows(AdvertisementTypeAlreadyExistsException.class, () -> advertisementTypeService.addAdvertisementType(name));
        verify(advertisementTypeRepository).existsByName(name);
        verify(advertisementTypeRepository, never()).save(any(AdvertisementType.class));
    }
}
