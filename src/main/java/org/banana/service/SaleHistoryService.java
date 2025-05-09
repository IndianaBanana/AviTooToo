package org.banana.service;

import org.banana.dto.history.SaleHistoryAddRequestDto;
import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Created by Banana on 04.05.2025
 */
public interface SaleHistoryService {

    SaleHistoryResponseDto addSale(SaleHistoryAddRequestDto requestDto);

    void cancelSale(UUID saleId);

    List<SaleHistoryResponseDto> getSalesByAdvertisementId(UUID advertisementId);

    List<SaleHistoryTotalForAdvertisementsResponseDto> getTotalForSalesInAdvertisements();
}
