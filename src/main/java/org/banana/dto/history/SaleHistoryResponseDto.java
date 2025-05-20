package org.banana.dto.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Информация о продаже")
public class SaleHistoryResponseDto {

    @Schema(description = "UUID записи о продаже", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID id;

    @Schema(description = "Название объявления", example = "Мощный игровой ноутбук")
    private final String advertisementTitle;

    @Schema(description = "UUID объявления", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID advertisementId;

    @Schema(description = "UUID покупателя", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID buyerId;

    @Schema(description = "Дата и время продажи", example = "2023-10-05T14:30:00")
    private final LocalDateTime saleDateTime;

    @Schema(description = "Проданное количество", example = "2")
    private final Integer quantity;
}
