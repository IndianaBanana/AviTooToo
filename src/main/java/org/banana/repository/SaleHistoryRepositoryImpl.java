package org.banana.repository;

import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;
import org.banana.entity.SaleHistory;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SaleHistoryRepositoryImpl extends AbstractCrudRepositoryImpl<SaleHistory, UUID> implements SaleHistoryRepository {

    public static final String GET_TOTAL_FOR_SALES_IN_ADVERTISEMENTS = """
            SELECT new org.banana.dto.sale.SaleHistoryTotalForAdvertisementsResponseDto(
                a.id,
                a.title,
                SUM(sh.quantity * sh.advertisement.price),
                SUM(sh.quantity),
                MIN(sh.saleDateTime),
                MAX(sh.saleDateTime)
            )
            FROM SaleHistory sh
            join fetch sh.advertisement a
            where a.user = :currentUserId
            GROUP BY a.id, a.title
            """;
    public static final String GET_SALES_BY_ADVERTISEMENT_ID = """
            SELECT new org.banana.dto.sale.SaleHistoryResponseDto(
                sh.id,
                sh.advertisement.title,
                sh.advertisement.id,
                sh.buyerId,
                sh.saleDateTime,
                sh.quantity
            )
            FROM SaleHistory sh
            WHERE a.id = :adId
            ORDER BY sh.saleDateTime ASC
            """;

    public SaleHistoryRepositoryImpl() {
        super(SaleHistory.class);
    }

    @Override
    public List<SaleHistoryTotalForAdvertisementsResponseDto> getTotalForSalesInAdvertisements(UUID currentUserId) {
        return getSession()
                .createQuery(GET_TOTAL_FOR_SALES_IN_ADVERTISEMENTS, SaleHistoryTotalForAdvertisementsResponseDto.class)
                .setParameter("currentUserId", currentUserId)
                .getResultList();
    }

    @Override
    public List<SaleHistoryResponseDto> getSalesByAdvertisementId(UUID advertisementId) {
        return getSession()
                .createQuery(GET_SALES_BY_ADVERTISEMENT_ID, SaleHistoryResponseDto.class)
                .setParameter("adId", advertisementId)
                .getResultList();
    }
}
