package org.banana.dto.city;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO с информацией о городе")
public class CityDto {

    @Schema(description = "Идентификатор города")
    private UUID id;

    @Schema(description = "Название города")
    @NotBlank
    private String name;

    public CityDto(String name) {
        this.name = name;
    }
}
