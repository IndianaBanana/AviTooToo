package org.banana.service;

import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;

import java.util.List;
import java.util.UUID;

public interface AdvertisementService {

    AdvertisementResponseDto findById(UUID advertisementId);

    List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size);

    void deleteAdvertisement(UUID advertisementId);

    AdvertisementResponseDto addAdvertisement(AdvertisementRequestDto requestDto);

    AdvertisementResponseDto updateAdvertisement(UUID advertisementId, AdvertisementRequestDto requestDto);

    AdvertisementResponseDto closeAdvertisement(UUID advertisementId);

    AdvertisementResponseDto reopenAdvertisement(UUID advertisementId);

    AdvertisementResponseDto promoteAdvertisement(UUID advertisementId);
}
