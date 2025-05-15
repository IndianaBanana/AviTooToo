package org.banana.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import static org.banana.dto.ValidationConstants.EMAIL_REGEX;

/**
 * Created by Banana on 29.04.2025
 */
@Data
@AllArgsConstructor
public class UserUsernameUpdateRequestDto {

    @Email(regexp = EMAIL_REGEX)
    @NotBlank
    private String newUsername;

    @NotBlank
    @ToString.Exclude
    private String password;
}
