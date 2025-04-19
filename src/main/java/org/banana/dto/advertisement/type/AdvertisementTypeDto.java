package org.banana.dto.advertisement.type;

import lombok.Data;

import java.util.UUID;

@Data
public class AdvertisementTypeDto {

    private UUID advertisementTypeId;
    private String name;
}
