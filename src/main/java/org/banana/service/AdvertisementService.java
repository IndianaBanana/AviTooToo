package org.banana.service;

import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;

import java.util.List;
import java.util.UUID;

public interface AdvertisementService {

    AdvertisementResponseDto findById(UUID advertisementId);

//    List<AdvertisementResponseDto> findAll();

    List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size);

    List<AdvertisementResponseDto> findAllByUserId(UUID userId);

    void deleteById(UUID advertisementId);

    AdvertisementResponseDto createAdvertisement(AdvertisementRequestDto requestDto);

    AdvertisementResponseDto updateAdvertisement(UUID advertisementId, AdvertisementRequestDto requestDto);

    AdvertisementResponseDto closeAdvertisement(UUID advertisementId);

    AdvertisementResponseDto promoteAdvertisement(UUID advertisementId);
}
