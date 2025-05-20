package org.banana.dto.history;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Запрос на добавление продажи")
public class SaleHistoryAddRequestDto {

    @Schema(description = "UUID объявления", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull
    private final UUID advertisementId;

    @Schema(description = "Количество товара", minimum = "1", example = "5")
    @NotNull
    @Min(1)
    private final Integer quantity;
}
