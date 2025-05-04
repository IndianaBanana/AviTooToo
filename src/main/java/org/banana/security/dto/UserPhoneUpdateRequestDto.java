package org.banana.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import static org.banana.dto.ValidationConstants.PHONE_ERROR_MESSAGE;
import static org.banana.dto.ValidationConstants.PHONE_REGEX;

/**
 * Created by Banana on 29.04.2025
 */
@Data
@AllArgsConstructor
public class UserPhoneUpdateRequestDto {

    @NotBlank
    @Pattern(regexp = PHONE_REGEX, message = PHONE_ERROR_MESSAGE)
    private String newPhone;

    @NotBlank
    @ToString.Exclude
    private String password;
}
