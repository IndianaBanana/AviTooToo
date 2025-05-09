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
import org.banana.exception.UserNotFoundException;
import org.banana.repository.AdvertisementRepository;
import org.banana.repository.AdvertisementTypeRepository;
import org.banana.repository.CityRepository;
import org.banana.repository.UserRepository;
import org.banana.security.UserRole;
import org.banana.security.dto.UserPrincipal;
import org.banana.util.SecurityUtils;
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
    private final UserRepository userRepository;
    private final AdvertisementMapper advertisementMapper;

    @Override
    @Transactional(readOnly = true)
    public AdvertisementResponseDto findById(UUID advertisementId) {
        return advertisementRepository.findDtoById(advertisementId).orElseThrow(
                () -> new AdvertisementNotFoundException(advertisementId));
    }

    @Override
    public List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size) {
        if (filter.getSearchParam() != null) {
            filter.setSearchParam(filter.getSearchParam()
                    .replace("\\", "\\\\")
                    .replace("_", "\\_")
                    .replace("%", "\\%"));
        }
        return advertisementRepository.findAllFiltered(filter, page, size);
    }


    @Override
    @Transactional
    public void deleteById(UUID advertisementId) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        UserPrincipal userPrincipal = SecurityUtils.getCurrentUserPrincipal();
        if (!advertisement.getUser().getId().equals(userPrincipal.getId()) && !userPrincipal.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new AdvertisementUpdateException(NOT_OWNER);
        }
        advertisementRepository.delete(advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto createAdvertisement(AdvertisementRequestDto requestDto) {
        AdvertisementType advertisementType = advertisementTypeRepository.findById(requestDto.getAdvertisementTypeId())
                .orElseThrow(() -> new AdvertisementTypeNotFoundException(requestDto.getAdvertisementTypeId()));
        City city = cityRepository.findById(requestDto.getCityId())
                .orElseThrow(() -> new CityNotFoundException(requestDto.getCityId()));
        UUID currentUserId = SecurityUtils.getCurrentUserPrincipal().getId();
        User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new UserNotFoundException(currentUserId));
        Advertisement advertisement = advertisementMapper.advertisementRequestDtoToAdvertisement(requestDto);
        advertisement.setAdvertisementType(advertisementType);
        advertisement.setUser(currentUser);
        advertisement.setCity(city);
        advertisement.setCreateDate(LocalDateTime.now());
        Advertisement save = advertisementRepository.save(advertisement);
        return advertisementMapper.advertisementToAdvertisementResponseDto(save);
    }


    // !!! fixme переписать запрос чтобы не было миллиона запросов в БД
    @Override
    @Transactional
    public AdvertisementResponseDto updateAdvertisement(AdvertisementUpdateRequestDto requestDto) {
        Advertisement advertisement = findAdvertisementByIdOrThrow(requestDto.getAdvertisementId());
        if (advertisement.getCloseDate() != null) throw new AdvertisementUpdateException(ALREADY_CLOSED);

        if (!advertisement.getUser().getId().equals(SecurityUtils.getCurrentUserPrincipal().getId()))
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

        advertisement = advertisementRepository.save(advertisement);
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto closeAdvertisement(UUID advertisementId) {
//        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        AdvertisementResponseDto advertisement = advertisementRepository.findDtoById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));
        if (advertisement.getCloseDate() != null) {
            throw new AdvertisementUpdateException(ALREADY_CLOSED);
        }
        UserPrincipal currentUserPrincipal = SecurityUtils.getCurrentUserPrincipal();
        boolean isOwner = advertisement.getUserResponseDto().getId().equals(currentUserPrincipal.getId());
        boolean isAdmin = currentUserPrincipal.getRole().equals(UserRole.ROLE_ADMIN);
        if (!isOwner && !isAdmin) {
            throw new AdvertisementUpdateException(NOT_OWNER);
        }
        LocalDateTime closeDate = LocalDateTime.now();
        advertisement.setCloseDate(closeDate);
        advertisementRepository.closeAdvertisement(advertisementId, closeDate);
        return advertisement;
    }

    @Override
    @Transactional
    public AdvertisementResponseDto promoteAdvertisement(UUID advertisementId) {
        AdvertisementResponseDto advertisement = advertisementRepository.findDtoById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));
        if (advertisement.getIsPromoted()) {
            throw new AdvertisementUpdateException(ALREADY_PROMOTED);
        }
        if (advertisement.getCloseDate() != null) {
            throw new AdvertisementUpdateException(ALREADY_CLOSED);
        }
        if (!advertisement.getUserResponseDto().getId().equals(SecurityUtils.getCurrentUserPrincipal().getId())) {
            throw new AdvertisementUpdateException(NOT_OWNER);
        }
        advertisement.setIsPromoted(true);
        advertisementRepository.promoteAdvertisement(advertisementId);
        return advertisement;
    }

    private Advertisement findAdvertisementByIdOrThrow(UUID advertisementId) {
        return advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));
    }
}
