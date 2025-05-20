package org.banana.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.banana.security.dto.validation.PasswordOnRegisterValidation;

import static org.banana.dto.ValidationConstants.EMAIL_REGEX;
import static org.banana.dto.ValidationConstants.PASSWORD_MAX_LENGTH;
import static org.banana.dto.ValidationConstants.PASSWORD_MIN_LENGTH;
import static org.banana.dto.ValidationConstants.PHONE_ERROR_MESSAGE;
import static org.banana.dto.ValidationConstants.PHONE_REGEX;


@Data
@AllArgsConstructor
@NoArgsConstructor
@PasswordOnRegisterValidation
@Schema(description = "Запрос на регистрацию пользователя")
public class UserRegisterRequestDto {

    @NotBlank
    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @NotBlank
    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String lastName;

    @NotBlank
    @Schema(description = "Номер телефона", example = "+79001234567")
    @Pattern(regexp = PHONE_REGEX, message = PHONE_ERROR_MESSAGE)
    private String phone;

    @NotBlank
    @Email(regexp = EMAIL_REGEX)
    @Schema(description = "Email пользователя (используется как логин)", example = "ivanov@example.com")
    private String username;

    @NotBlank
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    @Schema(description = "Пароль", example = "securePass123")
    @ToString.Exclude
    private String password;

    @NotBlank
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    @Schema(description = "Повтор пароля", example = "securePass123")
    @ToString.Exclude
    private String matchingPassword;
}
