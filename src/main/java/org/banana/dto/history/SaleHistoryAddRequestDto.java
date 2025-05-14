package org.banana.dto.history;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SaleHistoryAddRequestDto {

    @NotNull
    private final UUID advertisementId;

    @NotNull
    @Min(1)
    private final Integer quantity;
}
