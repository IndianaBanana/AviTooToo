package org.banana.dto.city;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityDto {

    private UUID id;

    @NotBlank
    private String name;

    public CityDto(String name) {
        this.name = name;
    }
}
