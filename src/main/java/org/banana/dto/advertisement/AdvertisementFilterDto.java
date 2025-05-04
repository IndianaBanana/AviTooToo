package org.banana.dto.advertisement;

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
    @Min(0)
    private BigDecimal minPrice;
    @Min(0)
    private BigDecimal maxPrice;
}
