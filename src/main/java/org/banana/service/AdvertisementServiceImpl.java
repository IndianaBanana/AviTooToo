package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementMapper;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.entity.Advertisement;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.repository.AdvertisementRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Created by Banana on 25.04.2025
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementMapper advertisementMapper;

    @Override
//    @Transactional(readOnly = true)
    public AdvertisementResponseDto findById(UUID advertisementId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    @Override
    public List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size) {
        return advertisementMapper.advertisementListToAdvertisementResponseDtoList(advertisementRepository.findAllFiltered(filter,page,size));
    }

    @Override
    public List<AdvertisementResponseDto> findAllByUserId(UUID userId) {
        return List.of();
    }

    @Override
    public void deleteById(UUID advertisementId) {

    }

    @Override
    public AdvertisementResponseDto createAdvertisement(AdvertisementRequestDto requestDto) {
        return null;
    }

    @Override
    public AdvertisementResponseDto updateAdvertisement(UUID advertisementId, AdvertisementRequestDto requestDto) {
        return null;
    }

    @Override
    public AdvertisementResponseDto closeAdvertisement(UUID advertisementId) {
        return null;
    }

    @Override
    public AdvertisementResponseDto promoteAdvertisement(UUID advertisementId) {
        return null;
    }
}
