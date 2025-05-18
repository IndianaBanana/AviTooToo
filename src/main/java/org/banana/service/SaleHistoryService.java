package org.banana.service;

import org.banana.dto.history.SaleHistoryAddRequestDto;
import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;
import org.banana.exception.SaleHistoryAccessDeniedException;
import org.banana.exception.SaleHistoryNotFoundException;

import java.util.List;
import java.util.UUID;


public interface SaleHistoryService {

    SaleHistoryResponseDto addSale(SaleHistoryAddRequestDto requestDto);

    /**
     * Отмена продажи
     *
     * @param saleId идентификатор продажи
     * @throws SaleHistoryNotFoundException     если продажа не найдена
     * @throws SaleHistoryAccessDeniedException если пользователь не является владельцем продажи или администратором
     */
    void deleteSale(UUID saleId);

    List<SaleHistoryResponseDto> getSalesByAdvertisementId(UUID advertisementId);

    List<SaleHistoryTotalForAdvertisementsResponseDto> getTotalForSalesInAdvertisements();
}
