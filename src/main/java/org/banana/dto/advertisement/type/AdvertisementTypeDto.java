package org.banana.dto.advertisement.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementTypeDto {

    private UUID id;
    private String name;

    public AdvertisementTypeDto(String name) {
        this.name = name;
    }
}
