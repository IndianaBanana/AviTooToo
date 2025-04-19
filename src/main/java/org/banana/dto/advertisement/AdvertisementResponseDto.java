package org.banana.dto.advertisement;

import lombok.Data;
import org.banana.dto.user.UserResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdvertisementResponseDto {

    private UUID advertisementId;
    private UserResponseDto userResponseDto;
    private String cityName;
    private String advertisementType;
    private String title;
    private String description;
    private BigDecimal price;
    private int quantity;
    private boolean isPaid;
    private LocalDateTime createDate;
    private LocalDateTime closeDate;
}
