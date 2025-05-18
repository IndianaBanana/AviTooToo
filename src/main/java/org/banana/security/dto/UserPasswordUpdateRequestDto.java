package org.banana.security.dto;

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
public class UserPasswordUpdateRequestDto {

    @NotBlank
    @ToString.Exclude
    private String oldPassword;

    @NotBlank
    @ToString.Exclude
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String newPassword;

    @NotBlank
    @ToString.Exclude
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String matchingNewPassword;
}
