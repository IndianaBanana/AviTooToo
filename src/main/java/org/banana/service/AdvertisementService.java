package org.banana.service;

import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.dto.advertisement.AdvertisementUpdateRequestDto;

import java.util.List;
import java.util.UUID;

public interface AdvertisementService {

    AdvertisementResponseDto findById(UUID advertisementId);

    List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size);

    void deleteById(UUID advertisementId);

    AdvertisementResponseDto createAdvertisement(AdvertisementRequestDto requestDto);

    AdvertisementResponseDto updateAdvertisement(AdvertisementUpdateRequestDto requestDto);

    AdvertisementResponseDto closeAdvertisement(UUID advertisementId);

    AdvertisementResponseDto promoteAdvertisement(UUID advertisementId);
}
