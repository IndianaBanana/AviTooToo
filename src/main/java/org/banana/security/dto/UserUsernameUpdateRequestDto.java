package org.banana.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import static org.banana.dto.ValidationConstants.EMAIL_REGEX;


@Data
@AllArgsConstructor
@Schema(description = "Запрос на изменение email")
public class UserUsernameUpdateRequestDto {

    @Schema(description = "Новый email", example = "new.email@example.com")
    @Email(regexp = EMAIL_REGEX)
    @NotBlank
    private String newUsername;

    @Schema(description = "Текущий пароль для подтверждения операции", example = "currentPassword123")
    @NotBlank
    @ToString.Exclude
    private String password;
}
