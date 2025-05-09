package org.banana.repository;

import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;
import org.banana.entity.SaleHistory;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface SaleHistoryRepository extends CrudRepository<SaleHistory, UUID> {

    List<SaleHistoryTotalForAdvertisementsResponseDto> getTotalForSalesInAdvertisements(UUID currentUserId);

    List<SaleHistoryResponseDto> getSalesByAdvertisementId(UUID advertisementId);
}
