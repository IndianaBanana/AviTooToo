package org.banana.dto.advertisement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AdvertisementRequestDto {

    @NotNull
    private UUID cityId;

    @NotNull
    private UUID advertisementTypeId;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @Min(0)
    private BigDecimal price;

    @Min(0)
    private int quantity;
}
