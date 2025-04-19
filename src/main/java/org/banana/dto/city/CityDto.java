package org.banana.dto.city;

import lombok.Data;

import java.util.UUID;

@Data
public class CityDto {

    private UUID cityId;
    private String name;
}
