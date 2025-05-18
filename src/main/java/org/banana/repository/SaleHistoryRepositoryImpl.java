package org.banana.repository;

import lombok.extern.slf4j.Slf4j;
import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;
import org.banana.entity.SaleHistory;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
public class SaleHistoryRepositoryImpl extends AbstractCrudRepositoryImpl<SaleHistory, UUID> implements SaleHistoryRepository {

    public static final String GET_TOTAL_FOR_SALES_IN_ADVERTISEMENTS = """
            select new org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto(
                a.id,
                a.title,
                sum(sh.quantity * sh.advertisement.price),
                sum(sh.quantity),
                min(sh.saleDateTime),
                max(sh.saleDateTime)
            )
            from SaleHistory sh
            join sh.advertisement a
            where a.user.id = :currentUserId
            group by a.id, a.title
            """;

    public static final String GET_SALES_BY_ADVERTISEMENT_ID = """
            select new org.banana.dto.history.SaleHistoryResponseDto(
                sh.id,
                a.title,
                a.id,
                sh.buyerId,
                sh.saleDateTime,
                sh.quantity
            )
            from SaleHistory sh
            join sh.advertisement a
            where a.id = :adId
            order by sh.saleDateTime asc
            """;

    public SaleHistoryRepositoryImpl() {
        super(SaleHistory.class);
    }

    @Override
    public List<SaleHistoryTotalForAdvertisementsResponseDto> getTotalForSalesInAdvertisements(UUID currentUserId) {
        log.info("getTotalForSalesInAdvertisements({})", currentUserId);
        return getSession()
                .createQuery(GET_TOTAL_FOR_SALES_IN_ADVERTISEMENTS, SaleHistoryTotalForAdvertisementsResponseDto.class)
                .setParameter("currentUserId", currentUserId)
                .getResultList();
    }

    @Override
    public List<SaleHistoryResponseDto> getSalesByAdvertisementId(UUID advertisementId) {
        log.info("getSalesByAdvertisementId({})", advertisementId);
        return getSession()
                .createQuery(GET_SALES_BY_ADVERTISEMENT_ID, SaleHistoryResponseDto.class)
                .setParameter("adId", advertisementId)
                .getResultList();
    }
}
