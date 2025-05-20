package org.banana.dto.advertisement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class AdvertisementFilterDto {

    @Schema(description = "Показывать только открытые объявления", example = "true")
    private boolean onlyOpened = true;

    @Schema(description = "Список UUID городов для фильтрации", example = "[\"3fa85f64-5717-4562-b3fc-2c963f66afa6\"]")
    private List<UUID> cityIds;

    @Schema(description = "Список UUID типов объявлений для фильтрации", example = "[\"6b1e2d88-4f8b-11ec-81d3-0242ac130003\"]")
    private List<UUID> advertisementTypeIds;

    @Schema(description = "Поисковая фраза в заголовке или описании", example = "гараж")
    private String searchParam;

    @Schema(description = "Минимальная цена для фильтрации", example = "0.00")
    @DecimalMin("0.00")
    private BigDecimal minPrice;

    @Schema(description = "Максимальная цена для фильтрации", example = "100000.00")
    @DecimalMin("0.00")
    private BigDecimal maxPrice;
}
