package org.banana.dto.advertisement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для создания или обновления объявления")
public class AdvertisementRequestDto {

    @Schema(description = "UUID города для объявления", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private UUID cityId;

    @Schema(description = "UUID типа объявления", example = "6b1e2d88-4f8b-11ec-81d3-0242ac130003", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private UUID advertisementTypeId;

    @Schema(description = "Заголовок объявления", example = "Продам гараж", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 255)
    private String title;

    @Schema(description = "Описание объявления", example = "В хорошем состоянии, раз в год заезжал, от жены прятался", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String description;

    @Schema(description = "Цена в рублях", example = "15000")
    @DecimalMin("0.00")
    private BigDecimal price;

    @Schema(description = "Количество товарной единицы в объявлении", example = "1")
    @Min(1)
    private Integer quantity;
}
