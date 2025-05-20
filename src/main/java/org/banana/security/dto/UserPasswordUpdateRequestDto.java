package org.banana.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.banana.security.dto.validation.PasswordChangeValidation;

import static org.banana.dto.ValidationConstants.PASSWORD_MAX_LENGTH;
import static org.banana.dto.ValidationConstants.PASSWORD_MIN_LENGTH;


@Data
@AllArgsConstructor
@PasswordChangeValidation
@Schema(description = "Запрос на изменение пароля")
public class UserPasswordUpdateRequestDto {

    @Schema(description = "Текущий пароль", example = "oldPassword123")
    @NotBlank
    @ToString.Exclude
    private String oldPassword;

    @Schema(description = "Новый пароль", example = "newSecurePassword456")
    @NotBlank
    @ToString.Exclude
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String newPassword;

    @Schema(description = "Подтверждение нового пароля", example = "newSecurePassword456")
    @NotBlank
    @ToString.Exclude
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String matchingNewPassword;
}
