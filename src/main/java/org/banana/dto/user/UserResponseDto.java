package org.banana.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private UUID userId;

    private String firstName;

    private String lastName;

    private String phone;

    private String username;

    private BigDecimal averageRating;

    private Integer ratingCount;
}
