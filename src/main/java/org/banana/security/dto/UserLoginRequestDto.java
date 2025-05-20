package org.banana.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;


@Data
@AllArgsConstructor
@Schema(description = "Запрос на вход пользователя")
public class UserLoginRequestDto {

    @NotBlank
    @Schema(description = "Логин пользователя (email)", example = "ivanov@example.com")
    private String username;

    @NotBlank
    @ToString.Exclude
    @Schema(description = "Пароль", example = "securePass123")
    private String password;
}
