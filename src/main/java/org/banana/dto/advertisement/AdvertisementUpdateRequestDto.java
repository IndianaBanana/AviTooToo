package org.banana.dto.advertisement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AdvertisementUpdateRequestDto {
    private UUID cityId;

    private UUID advertisementTypeId;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @DecimalMin("0.00")
    private BigDecimal price;

    @Min(1)
    private Integer quantity;
}
