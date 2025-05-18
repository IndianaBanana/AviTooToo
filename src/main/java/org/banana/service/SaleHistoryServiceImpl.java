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
import org.banana.exception.SaleHistoryUnexpectedException;
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


@Service
@Slf4j
@RequiredArgsConstructor
public class SaleHistoryServiceImpl implements SaleHistoryService {

    private static final int ATTEMPTS = 5;
    private final SaleHistoryRepository saleHistoryRepository;
    private final AdvertisementRepository advertisementRepository;
    private final SaleHistoryMapper saleHistoryMapper;

    private static void isOwnerOrAdmin(Advertisement advertisement, UserPrincipal current) {
        boolean isOwner = advertisement.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole().equals(UserRole.ROLE_ADMIN);
        if (!isOwner && !isAdmin) throw new SaleHistoryAccessDeniedException();
    }

    @Override
    @Transactional
    public SaleHistoryResponseDto addSale(SaleHistoryAddRequestDto requestDto) {
        log.info("addSale({}) in {}", requestDto, getClass().getSimpleName());
        UUID currentUserId = SecurityUtils.getCurrentUserPrincipal().getId();
        Integer requestDtoQuantity = requestDto.getQuantity();

        // используя оптимистичную блокировку пробуем обновить количество объявления
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            Advertisement advertisement = advertisementRepository.findById(requestDto.getAdvertisementId())
                    .orElseThrow(() -> new AdvertisementNotFoundException(requestDto.getAdvertisementId()));

            if (advertisement.getCloseDate() != null) {
                throw new AdvertisementNotFoundException(requestDto.getAdvertisementId());
            }

            Integer advertisementQuantity = advertisement.getQuantity();

            if (advertisementQuantity < requestDtoQuantity) {
                throw new SaleHistoryAdvertisementQuantityIsLowerThanExpectedException(advertisementQuantity, requestDtoQuantity);
            }

            int rowsUpdated = advertisementRepository.updateAdvertisementQuantity(
                    requestDto.getAdvertisementId(),
                    advertisementQuantity,
                    advertisementQuantity - requestDtoQuantity);
            if (rowsUpdated == 1) {
                // если получилось обновить - выходим из цикла
                SaleHistory saleHistory = new SaleHistory(advertisement, currentUserId, requestDtoQuantity, LocalDateTime.now());
                saleHistory = saleHistoryRepository.save(saleHistory);
                log.debug("saleHistory created: {}", saleHistory);
                return saleHistoryMapper.fromSaleHistoryToSaleHistoryResponseDto(saleHistory);
            }
        }
        // не удалось обновить за пять попыток поэтому кидаем исключение
        throw new SaleHistoryUnexpectedException();
    }

    @Override
    @Transactional
    public void deleteSale(UUID saleId) {
        log.info("deleteSale({}) in {}", saleId, getClass().getSimpleName());
        UserPrincipal current = SecurityUtils.getCurrentUserPrincipal();

        SaleHistory saleHistory = saleHistoryRepository.findById(saleId)
                .orElseThrow(() -> new SaleHistoryNotFoundException(saleId));

        Integer saleHistoryQuantity = saleHistory.getQuantity();
        Advertisement advertisement = saleHistory.getAdvertisement();
        UUID advertisementId = advertisement.getId();

        isOwnerOrAdmin(advertisement, current);
        if (advertisement.getCloseDate() != null) {
            throw new AdvertisementNotFoundException(advertisementId);
        }
        // используя оптимистичную блокировку пробуем обновить количество объявления
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            if (attempt > 0) {
                advertisement = advertisementRepository.findById(advertisementId)
                        .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));
            }

            Integer advertisementQuantity = advertisement.getQuantity();

            int rowsUpdated = advertisementRepository.updateAdvertisementQuantity(
                    advertisementId,
                    advertisementQuantity,
                    advertisementQuantity + saleHistoryQuantity
            );
            if (rowsUpdated == 1) {
                // если получилось обновить - выходим из цикла
                saleHistoryRepository.delete(saleHistory);
                log.debug("saleHistory deleted: {}", saleHistory);
                return;
            }
        }
        // не удалось обновить за пять попыток поэтому кидаем исключение
        throw new SaleHistoryUnexpectedException();
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
}
