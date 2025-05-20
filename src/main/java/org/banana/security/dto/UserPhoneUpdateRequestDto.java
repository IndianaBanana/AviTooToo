package org.banana.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import static org.banana.dto.ValidationConstants.PHONE_ERROR_MESSAGE;
import static org.banana.dto.ValidationConstants.PHONE_REGEX;


@Data
@AllArgsConstructor
@Schema(description = "Запрос на изменение телефона")
public class UserPhoneUpdateRequestDto {

    @Schema(description = "Новый номер телефона в формате " + PHONE_REGEX, example = "+79161234567")
    @NotBlank
    @Pattern(regexp = PHONE_REGEX, message = PHONE_ERROR_MESSAGE)
    private String newPhone;

    @Schema(description = "Текущий пароль", example = "currentPassword123")
    @NotBlank
    @ToString.Exclude
    private String password;
}
