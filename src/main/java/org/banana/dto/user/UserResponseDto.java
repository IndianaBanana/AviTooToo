package org.banana.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Информация о пользователе")
public class UserResponseDto {

    @Schema(description = "UUID пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Имя", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия", example = "Петров")
    private String lastName;

    @Schema(description = "Телефон", example = "+79161234567")
    private String phone;

    @Schema(description = "Email", example = "user@example.com")
    private String username;

    @Schema(description = "Средний рейтинг пользователя", example = "4.5")
    private BigDecimal averageRating;

    @Schema(description = "Общее количество оценок выполненных пользователю", example = "10")
    private Integer ratingCount;

    public UserResponseDto(UUID id, String firstName, String lastName, String phone, String username) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.username = username;
    }
}
