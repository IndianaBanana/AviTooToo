package org.banana.security.dto;

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

/**
 * Created by Banana on 27.04.2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@PasswordOnRegisterValidation()
public class UserRegisterRequestDto {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Pattern(regexp = PHONE_REGEX, message = PHONE_ERROR_MESSAGE)
    private String phone;

    @NotBlank
    @Email(regexp = EMAIL_REGEX)
    private String username;

    @NotBlank
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    @ToString.Exclude
    private String password;

    @NotBlank
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    @ToString.Exclude
    private String matchingPassword;
}
