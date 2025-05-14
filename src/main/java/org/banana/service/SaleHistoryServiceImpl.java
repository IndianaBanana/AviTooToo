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
        UUID currentUserPrincipalId = SecurityUtils.getCurrentUserPrincipal().getId();

        Advertisement advertisement = advertisementRepository.findById(requestDto.getAdvertisementId())
                .orElseThrow(() -> new AdvertisementNotFoundException(requestDto.getAdvertisementId()));

        if (advertisement.getQuantity() < requestDto.getQuantity())
            throw new SaleHistoryAdvertisementQuantityIsLowerThanExpectedException(advertisement.getQuantity(), requestDto.getQuantity());

        SaleHistory saleHistory = new SaleHistory(advertisement, currentUserPrincipalId, requestDto.getQuantity(), LocalDateTime.now());
        advertisement.setQuantity(advertisement.getQuantity() - requestDto.getQuantity());
        advertisementRepository.save(advertisement);
        saleHistory = saleHistoryRepository.save(saleHistory);
        return saleHistoryMapper.fromSaleHistoryToSaleHistoryResponseDto(saleHistory);
    }

    @Override
    @Transactional
    public void cancelSale(UUID saleId) {
        UserPrincipal current = SecurityUtils.getCurrentUserPrincipal();

        SaleHistory sh = saleHistoryRepository.findById(saleId)
                .orElseThrow(() -> new SaleHistoryNotFoundException(saleId));

        Advertisement ad = sh.getAdvertisement();

        boolean isOwner = ad.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole() == UserRole.ROLE_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new SaleHistoryAccessDeniedException();
        }
        ad.setQuantity(ad.getQuantity() + sh.getQuantity());
        advertisementRepository.save(ad);

        saleHistoryRepository.delete(sh);
    }


    @Override
    @Transactional(readOnly = true)
    public List<SaleHistoryResponseDto> getSalesByAdvertisementId(UUID advertisementId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));
        UserPrincipal current = SecurityUtils.getCurrentUserPrincipal();
        boolean isOwner = advertisement.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole().equals(UserRole.ROLE_ADMIN);
        if (!isOwner && !isAdmin) {
            throw new SaleHistoryAccessDeniedException();
        }
        return saleHistoryRepository.getSalesByAdvertisementId(advertisementId);
    }

    @Override
    public List<SaleHistoryTotalForAdvertisementsResponseDto> getTotalForSalesInAdvertisements() {
        return saleHistoryRepository.getTotalForSalesInAdvertisements(SecurityUtils.getCurrentUserPrincipal().getId());
    }
}
