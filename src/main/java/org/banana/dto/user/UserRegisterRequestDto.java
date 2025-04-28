package org.banana.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.banana.security.dto.validation.PasswordMatches;

import static org.banana.dto.ValidationConstants.EMAIL_REGEX;
import static org.banana.dto.ValidationConstants.PASSWORD_REGISTRATION_MIN_LENGTH;

/**
 * Created by Banana on 27.04.2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatches(message = "Passwords do not match")
public class UserRegisterRequestDto {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String phone;
    @NotBlank
    @Email(regexp = EMAIL_REGEX)
    private String username;
    @NotBlank
    @Size(min = PASSWORD_REGISTRATION_MIN_LENGTH)
    @ToString.Exclude
    private String password;
    @NotBlank
    @Size(min = PASSWORD_REGISTRATION_MIN_LENGTH)
    @ToString.Exclude
    private String matchingPassword;
}
