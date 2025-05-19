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

import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.ADVERTISEMENT_CLOSED;
import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.ADVERTISEMENT_NOT_CLOSED;
import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.ALREADY_PROMOTED;
import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.NOT_OWNER;


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
    public AdvertisementResponseDto findById(UUID advertisementId) {
        log.info("findById({}) in {}", advertisementId, getClass().getSimpleName());

        AdvertisementResponseDto advertisementResponseDto = advertisementRepository.findDtoById(advertisementId).orElseThrow(
                () -> new AdvertisementNotFoundException(advertisementId));
        log.info("advertisement found: {}", advertisementResponseDto);
        return advertisementResponseDto;
    }

    @Override
    public List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size) {
        log.info("findAllFiltered({}) in {}", filter, getClass().getSimpleName());
        List<AdvertisementResponseDto> allFiltered = advertisementRepository.findAllFiltered(filter, page, size);
        log.info("advertisements found quantity: {}", allFiltered.size());
        return allFiltered;
    }


    @Override
    @Transactional
    public void deleteAdvertisement(UUID advertisementId) {
        log.info("deleteAdvertisement({}) in {}", advertisementId, getClass().getSimpleName());

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        UserPrincipal userPrincipal = SecurityUtils.getCurrentUserPrincipal();
        boolean isOwner = advertisement.getUser().getId().equals(userPrincipal.getId());
        boolean isAdmin = UserRole.ROLE_ADMIN.equals(userPrincipal.getRole());

        if (!isOwner && !isAdmin)
            throw new AdvertisementUpdateException(NOT_OWNER);

        advertisementRepository.delete(advertisement);
        log.info("advertisement deleted: {}", advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto addAdvertisement(AdvertisementRequestDto requestDto) {
        log.info("addAdvertisement({}) in {}", requestDto, getClass().getSimpleName());

        AdvertisementType advertisementType = advertisementTypeRepository.findById(requestDto.getAdvertisementTypeId())
                .orElseThrow(() -> new AdvertisementTypeNotFoundException(requestDto.getAdvertisementTypeId()));

        City city = cityRepository.findById(requestDto.getCityId())
                .orElseThrow(() -> new CityNotFoundException(requestDto.getCityId()));

        UUID currentUserId = SecurityUtils.getCurrentUserPrincipal().getId();

        User currentUser = userRepository.findFetchedById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));

        Advertisement advertisement = advertisementMapper.advertisementRequestDtoToAdvertisement(requestDto);
        advertisement.setAdvertisementType(advertisementType);
        advertisement.setUser(currentUser);
        advertisement.setCity(city);
        advertisement.setCreateDate(LocalDateTime.now());

        advertisement = advertisementRepository.save(advertisement);

        log.info("advertisement created: {}", advertisement);
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto updateAdvertisement(UUID advertisementId, AdvertisementRequestDto requestDto) {
        log.info("updateAdvertisement({}) in {}", requestDto, getClass().getSimpleName());

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        UUID userId = SecurityUtils.getCurrentUserPrincipal().getId();

        if (!advertisement.getUser().getId().equals(userId))
            throw new AdvertisementUpdateException(NOT_OWNER);

        if (advertisement.getCloseDate() != null)
            throw new AdvertisementUpdateException(ADVERTISEMENT_CLOSED);

        AdvertisementType advertisementType = advertisementTypeRepository.findById(requestDto.getAdvertisementTypeId())
                .orElseThrow(() -> new AdvertisementTypeNotFoundException(requestDto.getAdvertisementTypeId()));

        City city = cityRepository.findById(requestDto.getCityId())
                .orElseThrow(() -> new CityNotFoundException(requestDto.getCityId()));

        advertisement.setCity(city);
        advertisement.setAdvertisementType(advertisementType);
        advertisement.setDescription(requestDto.getDescription());
        advertisement.setTitle(requestDto.getTitle());
        advertisement.setPrice(requestDto.getPrice());
        advertisement.setQuantity(requestDto.getQuantity());

        // для меньшего количества запросов в базу подгружаем юзера (не будет RatingView)
        userRepository.findFetchedById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        advertisement = advertisementRepository.save(advertisement);

        log.info("advertisement updated: {}", advertisement);
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto closeAdvertisement(UUID advertisementId) {
        log.info("closeAdvertisement({}) in {}", advertisementId, getClass().getSimpleName());

        Advertisement advertisement = advertisementRepository.findFetchedById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        UserPrincipal currentUserPrincipal = SecurityUtils.getCurrentUserPrincipal();

        boolean isOwner = advertisement.getUser().getId().equals(currentUserPrincipal.getId());
        if (!isOwner)
            throw new AdvertisementUpdateException(NOT_OWNER);

        if (advertisement.getCloseDate() != null)
            throw new AdvertisementUpdateException(ADVERTISEMENT_CLOSED);

        advertisement.setCloseDate(LocalDateTime.now());
        advertisement = advertisementRepository.save(advertisement);

        log.info("advertisement closed: {}", advertisement);
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto reopenAdvertisement(UUID advertisementId) {
        log.info("reopenAdvertisement({}) in {}", advertisementId, getClass().getSimpleName());

        Advertisement advertisement = advertisementRepository.findFetchedById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        UserPrincipal currentUserPrincipal = SecurityUtils.getCurrentUserPrincipal();

        boolean isOwner = advertisement.getUser().getId().equals(currentUserPrincipal.getId());
        if (!isOwner)
            throw new AdvertisementUpdateException(NOT_OWNER);

        if (advertisement.getCloseDate() == null)
            throw new AdvertisementUpdateException(ADVERTISEMENT_NOT_CLOSED);

        advertisement.setCloseDate(null);
        advertisement = advertisementRepository.save(advertisement);

        log.info("advertisement reopened: {}", advertisement);
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }

    @Override
    @Transactional
    public AdvertisementResponseDto promoteAdvertisement(UUID advertisementId) {
        log.info("promoteAdvertisement({}) in {}", advertisementId, getClass().getSimpleName());

        Advertisement advertisement = advertisementRepository.findFetchedById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        UserPrincipal currentUserPrincipal = SecurityUtils.getCurrentUserPrincipal();
        boolean isOwner = advertisement.getUser().getId().equals(currentUserPrincipal.getId());
        if (!isOwner)
            throw new AdvertisementUpdateException(NOT_OWNER);

        if (advertisement.getIsPromoted() != null && advertisement.getIsPromoted())
            throw new AdvertisementUpdateException(ALREADY_PROMOTED);

        if (advertisement.getCloseDate() != null)
            throw new AdvertisementUpdateException(ADVERTISEMENT_CLOSED);

        advertisement.setIsPromoted(true);
        advertisement = advertisementRepository.save(advertisement);

        log.info("advertisement promoted: {}", advertisement);
        return advertisementMapper.advertisementToAdvertisementResponseDto(advertisement);
    }
}
