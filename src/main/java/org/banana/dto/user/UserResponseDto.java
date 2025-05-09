package org.banana.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private UUID id;

    private String firstName;

    private String lastName;

    private String phone;

    private String username;

    private BigDecimal averageRating;

    private Integer ratingCount;

    public UserResponseDto(UUID id, String firstName, String lastName, String phone, String username) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.username = username;
    }
}
