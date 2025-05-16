package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementMapper;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
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
        log.info("findById({}) in {}", advertisementId, getClass().getSimpleName());

        return advertisementRepository.findDtoById(advertisementId).orElseThrow(
                () -> new AdvertisementNotFoundException(advertisementId));
    }

    @Override
    public List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size) {
        log.info("findAllFiltered({}) in {}", filter, getClass().getSimpleName());

        if (filter.getSearchParam() != null) {
            log.debug("search param before: {}", filter.getSearchParam());

            filter.setSearchParam(filter.getSearchParam()
                    .replace("\\", "\\\\")
                    .replace("_", "\\_")
                    .replace("%", "\\%"));

            log.debug("search param after: {}", filter.getSearchParam());
        }

        return advertisementRepository.findAllFiltered(filter, page, size);
    }


    @Override
    @Transactional
    public void deleteById(UUID advertisementId) {
        log.info("deleteById({}) in {}", advertisementId, getClass().getSimpleName());

        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);

        UserPrincipal userPrincipal = SecurityUtils.getCurrentUserPrincipal();
        boolean isOwner = advertisement.getUser().getId().equals(userPrincipal.getId());
        boolean isAdmin = userPrincipal.getRole().equals(UserRole.ROLE_ADMIN);

        if (!isOwner && !isAdmin)
            throw new AdvertisementUpdateException(NOT_OWNER);

        advertisementRepository.delete(advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto createAdvertisement(AdvertisementRequestDto requestDto) {
        log.info("createAdvertisement({}) in {}", requestDto, getClass().getSimpleName());

        AdvertisementType advertisementType = advertisementTypeRepository.findById(requestDto.getAdvertisementTypeId())
                .orElseThrow(() -> new AdvertisementTypeNotFoundException(requestDto.getAdvertisementTypeId()));

        City city = cityRepository.findById(requestDto.getCityId())
                .orElseThrow(() -> new CityNotFoundException(requestDto.getCityId()));

        UUID currentUserId = SecurityUtils.getCurrentUserPrincipal().getId();
        User currentUser = userRepository.findFetchedById(currentUserId).orElseThrow(() -> new UserNotFoundException(currentUserId));
        Advertisement advertisement = advertisementMapper.advertisementRequestDtoToAdvertisement(requestDto);
        advertisement.setAdvertisementType(advertisementType);
        advertisement.setUser(currentUser);
        advertisement.setCity(city);
        advertisement.setCreateDate(LocalDateTime.now());
        Advertisement saved = advertisementRepository.save(advertisement);

        log.debug("advertisement created: {}", saved);
        return advertisementMapper.advertisementToAdvertisementResponseDto(saved);
    }

    // todo подумать, можно ли сделать меньше запросов на обновление
    @Override
    @Transactional
    public AdvertisementResponseDto updateAdvertisement(UUID advertisementId, AdvertisementRequestDto requestDto) {
        log.info("updateAdvertisement({}) in {}", requestDto, getClass().getSimpleName());

        Advertisement advertisement = findAdvertisementByIdOrThrow(advertisementId);
        UUID userId = SecurityUtils.getCurrentUserPrincipal().getId();

        if (!advertisement.getUser().getId().equals(userId))
            throw new AdvertisementUpdateException(NOT_OWNER);

        if (advertisement.getCloseDate() != null)
            throw new AdvertisementUpdateException(ALREADY_CLOSED);

        City city = cityRepository.findById(requestDto.getCityId())
                .orElseThrow(() -> new CityNotFoundException(requestDto.getCityId()));

        AdvertisementType advertisementType = advertisementTypeRepository.findById(requestDto.getAdvertisementTypeId())
                .orElseThrow(() -> new AdvertisementTypeNotFoundException(requestDto.getAdvertisementTypeId()));

        advertisement.setCity(city);
        advertisement.setAdvertisementType(advertisementType);
        advertisement.setDescription(requestDto.getDescription());
        advertisement.setTitle(requestDto.getTitle());
        advertisement.setPrice(requestDto.getPrice());
        advertisement.setQuantity(requestDto.getQuantity());

        // для меньшего количества запросов в базу подгружаем юзера (не будет RatingView)
        userRepository.findFetchedById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        advertisement = advertisementRepository.save(advertisement);

        log.debug("advertisement updated: {}", advertisement);
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto closeAdvertisement(UUID advertisementId) {
        log.info("closeAdvertisement({}) in {}", advertisementId, getClass().getSimpleName());

        AdvertisementResponseDto advertisementDto = advertisementRepository.findDtoById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        if (advertisementDto.getCloseDate() != null)
            throw new AdvertisementUpdateException(ALREADY_CLOSED);

        UserPrincipal currentUserPrincipal = SecurityUtils.getCurrentUserPrincipal();
        boolean isOwner = advertisementDto.getUserResponseDto().getId().equals(currentUserPrincipal.getId());
        boolean isAdmin = currentUserPrincipal.getRole().equals(UserRole.ROLE_ADMIN);

        if (!isOwner && !isAdmin)
            throw new AdvertisementUpdateException(NOT_OWNER);

        LocalDateTime closeDate = LocalDateTime.now();
        advertisementDto.setCloseDate(closeDate);
        advertisementRepository.closeAdvertisement(advertisementId, closeDate);

        log.debug("advertisement closed: {}", advertisementDto);
        return advertisementDto;
    }

    @Override
    @Transactional
    public AdvertisementResponseDto promoteAdvertisement(UUID advertisementId) {
        log.info("promoteAdvertisement({}) in {}", advertisementId, getClass().getSimpleName());

        AdvertisementResponseDto advertisementDto = advertisementRepository.findDtoById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        if (advertisementDto.isPromoted())
            throw new AdvertisementUpdateException(ALREADY_PROMOTED);

        if (advertisementDto.getCloseDate() != null)
            throw new AdvertisementUpdateException(ALREADY_CLOSED);

        if (!advertisementDto.getUserResponseDto().getId().equals(SecurityUtils.getCurrentUserPrincipal().getId()))
            throw new AdvertisementUpdateException(NOT_OWNER);

        advertisementDto.setPromoted(true);
        advertisementRepository.promoteAdvertisement(advertisementId);

        log.debug("advertisement promoted: {}", advertisementDto);
        return advertisementDto;
    }

    private Advertisement findAdvertisementByIdOrThrow(UUID advertisementId) {
        return advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));
    }
}
