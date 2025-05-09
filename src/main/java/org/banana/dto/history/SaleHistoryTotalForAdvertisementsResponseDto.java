package org.banana.dto.history;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SaleHistoryTotalForAdvertisementsResponseDto {

    private final UUID id;
    private final String advertisementName;
    private final BigDecimal total;
    private final Long totalQuantity;
    private final LocalDateTime salePeriodStart;
    private final LocalDateTime salePeriodEnd;
}
