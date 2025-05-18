package org.banana.dto.advertisement;

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
public class AdvertisementRequestDto {

    @NotNull
    private UUID cityId;

    @NotNull
    private UUID advertisementTypeId;

    @NotBlank
    @Size(min = 1, max = 255)
    private String title;

    @NotBlank
    private String description;

    @DecimalMin("0.00")
    private BigDecimal price;

    @Min(1)
    private Integer quantity;
}
