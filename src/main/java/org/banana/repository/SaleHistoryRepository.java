package org.banana.repository;

import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;
import org.banana.entity.SaleHistory;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface SaleHistoryRepository extends CrudRepository<SaleHistory, UUID> {

    /**
     * Возвращает агрегированные данные продаж пользователя для каждого объявления
     *
     * @param currentUserId идентификатор юзера для которого нужно найти продажи
     * @return List<SaleHistoryTotalForAdvertisementsResponseDto>
     */
    List<SaleHistoryTotalForAdvertisementsResponseDto> getTotalForSalesInAdvertisements(UUID currentUserId);

    /**
     * Возвращает продажи по идентификатору объявления
     *
     * @param advertisementId идентификатор объявления по которому нужно найти продажи
     * @return List<SaleHistoryResponseDto> список продаж
     */
    List<SaleHistoryResponseDto> getSalesByAdvertisementId(UUID advertisementId);
}
