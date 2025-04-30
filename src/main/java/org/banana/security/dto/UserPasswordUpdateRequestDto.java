package org.banana.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.banana.security.dto.validation.PasswordChangeValidation;

import static org.banana.dto.ValidationConstants.PASSWORD_MIN_LENGTH;

/**
 * Created by Banana on 27.04.2025
 */
@Data
@PasswordChangeValidation
public class UserPasswordUpdateRequestDto {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = PASSWORD_MIN_LENGTH)
    private String newPassword;

    @NotBlank
    @Size(min = PASSWORD_MIN_LENGTH)
    private String matchingNewPassword;
}
