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
import org.banana.security.UserRole;
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

    @Override
    @Transactional(readOnly = true)
    public AdvertisementResponseDto findById(UUID advertisementId) {
        return advertisementRepository.findDtoById(advertisementId).orElseThrow(
                () -> new AdvertisementNotFoundException(advertisementId));
    }

    @Override
    public List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size) {
        return advertisementRepository.findAllFiltered(filter, page, size);
    }


    @Override
    @Transactional
    public void deleteById(UUID advertisementId) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        User currentUser = getCurrentUser();
        if (!advertisement.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new AdvertisementUpdateException(NOT_OWNER);
        }
        advertisementRepository.delete(advertisement);
    }

    // fixme переписать запрос чтобы не было миллиона запросов в БД
    @Override
    @Transactional
    public AdvertisementResponseDto createAdvertisement(AdvertisementRequestDto requestDto) {
        AdvertisementType advertisementType = advertisementTypeRepository.findById(requestDto.getAdvertisementTypeId())
                .orElseThrow(() -> new AdvertisementTypeNotFoundException(requestDto.getAdvertisementTypeId()));
        City city = cityRepository.findById(requestDto.getCityId())
                .orElseThrow(() -> new CityNotFoundException(requestDto.getCityId()));
        Advertisement advertisement = advertisementMapper.advertisementRequestDtoToAdvertisement(requestDto);
        advertisement.setAdvertisementType(advertisementType);
        advertisement.setUser(getCurrentUser());
        advertisement.setCity(city);
        advertisement.setCreateDate(LocalDateTime.now());
        Advertisement save = advertisementRepository.save(advertisement);
//        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        return advertisementMapper.advertisementToAdvertisementResponseDto(save);
    }


    // fixme переписать запрос чтобы не было миллиона запросов в БД
    @Override
    @Transactional
    public AdvertisementResponseDto updateAdvertisement(AdvertisementUpdateRequestDto requestDto) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(requestDto.getAdvertisementId());
        if (advertisement.getCloseDate() != null) throw new AdvertisementUpdateException(ALREADY_CLOSED);

        if (!advertisement.getUser().getId().equals(getCurrentUser().getId()))
            throw new AdvertisementUpdateException(NOT_OWNER);

        if (requestDto.getTitle() != null && !requestDto.getTitle().isBlank())
            advertisement.setTitle(requestDto.getTitle());

        if (requestDto.getDescription() != null && !requestDto.getDescription().isBlank())
            advertisement.setDescription(requestDto.getDescription());

        if (requestDto.getCityId() != null && !advertisement.getCity().getId().equals(requestDto.getCityId())) {
            City city = cityRepository.findById(requestDto.getCityId())
                    .orElseThrow(() -> new CityNotFoundException(requestDto.getCityId()));
            advertisement.setCity(city);
        }
        if (requestDto.getAdvertisementTypeId() != null && !advertisement.getAdvertisementType().getId().equals(requestDto.getAdvertisementId())) {
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

    // fixme переписать запрос чтобы не было миллиона запросов в БД
    @Override
    @Transactional
    public AdvertisementResponseDto closeAdvertisement(UUID advertisementId) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        if (advertisement.getCloseDate() != null) {
            throw new AdvertisementUpdateException(ALREADY_CLOSED);
        }
        if (!advertisement.getUser().getId().equals(getCurrentUser().getId())) {
            throw new AdvertisementUpdateException(NOT_OWNER);
        }
        advertisement.setCloseDate(LocalDateTime.now());
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    // fixme переписать запрос чтобы не было миллиона запросов в БД
    @Override
    @Transactional
    public AdvertisementResponseDto promoteAdvertisement(UUID advertisementId) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        if (advertisement.getIsPaid()) {
            throw new AdvertisementUpdateException(ALREADY_PROMOTED);
        }
        if (advertisement.getCloseDate() != null) {
            throw new AdvertisementUpdateException(ALREADY_CLOSED);
        }
        if (!advertisement.getUser().getId().equals(getCurrentUser().getId())) {
            throw new AdvertisementUpdateException(NOT_OWNER);
        }
        advertisement.setIsPaid(true);

        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
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
