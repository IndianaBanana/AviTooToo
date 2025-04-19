package org.banana.dto.history;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SaleHistoryDto {

    private UUID saleHistoryId;
    private UUID advertisementId;
    private UUID buyerId;
    private LocalDateTime saleDate;
    private short quantity;
}
