package org.banana.dto.advertisement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class AdvertisementFilterDto {

    private List<UUID> cityIds;
    private List<UUID> advertisementTypeIds;
    private String searchParam;
    @DecimalMin("0.00")
    private BigDecimal minPrice;
    @DecimalMin("0.00")
    private BigDecimal maxPrice;
}
