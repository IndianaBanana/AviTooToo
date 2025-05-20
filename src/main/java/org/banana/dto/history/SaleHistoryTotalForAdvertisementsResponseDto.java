package org.banana.dto.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Агрегированная статистика по объявлению")
public class SaleHistoryTotalForAdvertisementsResponseDto {

    @Schema(description = "UUID объявления", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID advertisementId;

    @Schema(description = "Название объявления", example = "Смартфон Xiaomi")
    private final String advertisementName;

    @Schema(description = "Общая выручка", example = "15000.00")
    private final BigDecimal total;

    @Schema(description = "Общее количество проданных единиц", example = "3")
    private final Long totalQuantity;

    @Schema(description = "Начало периода продаж", example = "2023-09-01T00:00:00")
    private final LocalDateTime salePeriodStart;

    @Schema(description = "Окончание периода продаж", example = "2023-10-05T23:59:59")
    private final LocalDateTime salePeriodEnd;
}
