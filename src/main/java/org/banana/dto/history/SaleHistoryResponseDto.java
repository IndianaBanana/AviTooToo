package org.banana.dto.history;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SaleHistoryResponseDto {

    private final UUID id;
    private final String advertisementTitle;
    private final UUID advertisementId;
    private final UUID buyerId;
    private final LocalDateTime saleDateTime;
    private final Integer quantity;
}
