package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.history.SaleHistoryAddRequestDto;
import org.banana.dto.history.SaleHistoryMapper;
import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;
import org.banana.entity.Advertisement;
import org.banana.entity.SaleHistory;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.SaleHistoryAccessDeniedException;
import org.banana.exception.SaleHistoryAdvertisementQuantityIsLowerThanExpectedException;
import org.banana.exception.SaleHistoryNotFoundException;
import org.banana.repository.AdvertisementRepository;
import org.banana.repository.SaleHistoryRepository;
import org.banana.security.UserRole;
import org.banana.security.dto.UserPrincipal;
import org.banana.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Created by Banana on 25.04.2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SaleHistoryServiceImpl implements SaleHistoryService {

    private final SaleHistoryRepository saleHistoryRepository;
    private final AdvertisementRepository advertisementRepository;
    private final SaleHistoryMapper saleHistoryMapper;

    // ?todo надо ли менять статус объявления на закрытое если количество = 0?
    @Override
    @Transactional
    public SaleHistoryResponseDto addSale(SaleHistoryAddRequestDto requestDto) {
        log.info("addSale({}) in {}", requestDto, getClass().getSimpleName());
        UUID currentUserId = SecurityUtils.getCurrentUserPrincipal().getId();

        Advertisement advertisement = advertisementRepository.findById(requestDto.getAdvertisementId())
                .orElseThrow(() -> new AdvertisementNotFoundException(requestDto.getAdvertisementId()));

        Integer advertisementQuantity = advertisement.getQuantity();
        Integer requestDtoQuantity = requestDto.getQuantity();

        if (advertisementQuantity < requestDtoQuantity) {
            throw new SaleHistoryAdvertisementQuantityIsLowerThanExpectedException(advertisementQuantity, requestDtoQuantity);
        }

        SaleHistory saleHistory = new SaleHistory(
                advertisement,
                currentUserId,
                requestDtoQuantity,
                LocalDateTime.now()
        );

        advertisement.setQuantity(advertisementQuantity - requestDtoQuantity);
        advertisementRepository.save(advertisement);
        log.debug("advertisement updated: {}", advertisement);

        saleHistory = saleHistoryRepository.save(saleHistory);
        log.debug("saleHistory created: {}", saleHistory);
        return saleHistoryMapper.fromSaleHistoryToSaleHistoryResponseDto(saleHistory);
    }

    @Override
    @Transactional
    public void cancelSale(UUID saleId) {
        log.info("cancelSale({}) in {}", saleId, getClass().getSimpleName());
        UserPrincipal current = SecurityUtils.getCurrentUserPrincipal();

        SaleHistory saleHistory = saleHistoryRepository.findById(saleId)
                .orElseThrow(() -> new SaleHistoryNotFoundException(saleId));

        Advertisement advertisement = saleHistory.getAdvertisement();

        isOwnerOrAdmin(advertisement, current);

        advertisement.setQuantity(advertisement.getQuantity() + saleHistory.getQuantity());
        advertisementRepository.save(advertisement);
        log.debug("advertisement updated: {}", advertisement);

        saleHistoryRepository.delete(saleHistory);
    }


    @Override
    @Transactional(readOnly = true)
    public List<SaleHistoryResponseDto> getSalesByAdvertisementId(UUID advertisementId) {
        log.info("getSalesByAdvertisementId({}) in {}", advertisementId, getClass().getSimpleName());
        UserPrincipal current = SecurityUtils.getCurrentUserPrincipal();

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        isOwnerOrAdmin(advertisement, current);

        return saleHistoryRepository.getSalesByAdvertisementId(advertisementId);
    }

    @Override
    public List<SaleHistoryTotalForAdvertisementsResponseDto> getTotalForSalesInAdvertisements() {
        log.info("getTotalForSalesInAdvertisements() in {}", getClass().getSimpleName());
        return saleHistoryRepository.getTotalForSalesInAdvertisements(SecurityUtils.getCurrentUserPrincipal().getId());
    }

    private static void isOwnerOrAdmin(Advertisement advertisement, UserPrincipal current) {
        boolean isOwner = advertisement.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole().equals(UserRole.ROLE_ADMIN);
        if (!isOwner && !isAdmin) throw new SaleHistoryAccessDeniedException();
    }
}
