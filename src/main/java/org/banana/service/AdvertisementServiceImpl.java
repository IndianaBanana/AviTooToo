package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementMapper;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.dto.advertisement.AdvertisementUpdateRequestDto;
import org.banana.entity.Advertisement;
import org.banana.entity.AdvertisementType;
import org.banana.entity.City;
import org.banana.entity.User;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.AdvertisementTypeNotFoundException;
import org.banana.exception.AdvertisementUpdateException;
import org.banana.exception.CityNotFoundException;
import org.banana.repository.AdvertisementRepository;
import org.banana.repository.AdvertisementTypeRepository;
import org.banana.repository.CityRepository;
import org.banana.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.ALREADY_CLOSED;
import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.ALREADY_PROMOTED;
import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.NOT_OWNER;

/**
 * Created by Banana on 25.04.2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final CityRepository cityRepository;
    private final AdvertisementTypeRepository advertisementTypeRepository;
    private final AdvertisementMapper advertisementMapper;

    // fixme переписать запрос чтобы не было миллиона запросов в БД
    @Override
    @Transactional(readOnly = true)
    public AdvertisementResponseDto findById(UUID advertisementId) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    @Override
    public List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size) {
        return advertisementRepository.findAllFiltered(filter,page,size);
    }


    @Override
    @Transactional
    public void deleteById(UUID advertisementId) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        advertisementRepository.delete(advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto createAdvertisement(AdvertisementRequestDto requestDto) {
        City city = cityRepository.findById(requestDto.getCityId())
                .orElseThrow(() -> new CityNotFoundException(requestDto.getCityId()));
        AdvertisementType advertisementType = advertisementTypeRepository.findById(requestDto.getAdvertisementTypeId())
                .orElseThrow(() -> new AdvertisementTypeNotFoundException(requestDto.getAdvertisementTypeId()));
        Advertisement advertisement = advertisementMapper.advertisementRequestDtoToAdvertisement(requestDto);
        advertisement.setAdvertisementType(advertisementType);
        advertisement.setUser(getCurrentUser());
        advertisement.setCity(city);
        advertisement.setCreateDate(LocalDateTime.now());
        advertisement.setAdvertisementId(UUID.randomUUID());
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisementRepository.save(advertisement));
    }

    @Override
    @Transactional
    public AdvertisementResponseDto updateAdvertisement(UUID advertisementId, AdvertisementUpdateRequestDto requestDto) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        if (advertisement.getCloseDate() != null) {
            throw new AdvertisementUpdateException(ALREADY_CLOSED);
        }
        if (!advertisement.getUser().getUserId().equals(getCurrentUser().getUserId())) {
            throw new AdvertisementUpdateException(NOT_OWNER);
        }
        advertisement.setTitle(requestDto.getTitle());
        advertisement.setDescription(requestDto.getDescription());
        if (requestDto.getCityId()!= null) {
            City city = cityRepository.findById(requestDto.getCityId())
                    .orElseThrow(() -> new CityNotFoundException(requestDto.getCityId()));
            advertisement.setCity(city);
        }
        if (requestDto.getAdvertisementTypeId() != null) {
            AdvertisementType advertisementType = advertisementTypeRepository.findById(requestDto.getAdvertisementTypeId())
                    .orElseThrow(() -> new AdvertisementTypeNotFoundException(requestDto.getAdvertisementTypeId()));
            advertisement.setAdvertisementType(advertisementType);
        }
        if (requestDto.getPrice() != null) {
            advertisement.setPrice(requestDto.getPrice());
        }
        if (requestDto.getQuantity() != null) {
            advertisement.setQuantity(requestDto.getQuantity());
        }
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto closeAdvertisement(UUID advertisementId) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        if (advertisement.getCloseDate() != null) {
            throw new AdvertisementUpdateException(ALREADY_CLOSED);
        }
        if (!advertisement.getUser().getUserId().equals(getCurrentUser().getUserId())){
            throw new AdvertisementUpdateException(NOT_OWNER);
        }
        advertisement.setCloseDate(LocalDateTime.now());
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    @Override
    @Transactional
    public void promoteAdvertisement(UUID advertisementId) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        if (advertisement.getIsPaid()) {
            throw new AdvertisementUpdateException(ALREADY_PROMOTED);
        }
        if (advertisement.getCloseDate() != null) {
            throw new AdvertisementUpdateException(ALREADY_CLOSED);
        }
        if (!advertisement.getUser().getUserId().equals(getCurrentUser().getUserId())){
            throw new AdvertisementUpdateException(NOT_OWNER);
        }
        advertisement.setIsPaid(true);
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUser();
    }

    private Advertisement findAdvertisementByIdOrThrow(UUID advertisementId) {
        return advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));
    }
}
