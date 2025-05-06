package org.banana.dto.advertisement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.banana.dto.user.UserResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementResponseDto {

    private UUID id;
    private UserResponseDto userResponseDto;
    private String cityName;
    private String advertisementType;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private Boolean isPaid;
    private LocalDateTime createDate;
    private LocalDateTime closeDate;
}
