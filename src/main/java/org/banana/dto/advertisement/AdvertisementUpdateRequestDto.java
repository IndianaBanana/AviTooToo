package org.banana.dto.advertisement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AdvertisementUpdateValidation
public class AdvertisementUpdateRequestDto {

    @NotNull
    private UUID advertisementId;

    private UUID cityId;

    private UUID advertisementTypeId;

    private String title;

    private String description;

    @DecimalMin("0.00")
    private BigDecimal price;

    @Min(1)
    private Integer quantity;
}
