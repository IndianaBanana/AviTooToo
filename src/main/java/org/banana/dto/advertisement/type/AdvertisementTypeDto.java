package org.banana.dto.advertisement.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Тип объявления")
public class AdvertisementTypeDto {

    @Schema(description = "Уникальный идентификатор типа объявления", example = "e1c889e5-7c0f-4ae8-a3b4-9dc80a2eaf55")
    private UUID id;

    @Schema(description = "Название типа объявления", example = "Дача")
    private String name;

    public AdvertisementTypeDto(String name) {
        this.name = name;
    }
}
