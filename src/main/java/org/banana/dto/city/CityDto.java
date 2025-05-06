package org.banana.dto.city;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CityDto {

    private UUID id;
    @NotBlank
    private String name;
}
