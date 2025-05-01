package org.banana.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Banana on 29.04.2025
 */
@Data
@AllArgsConstructor
public class UserUsernameUpdateRequestDto {

    @NotBlank
    private String newUsername;

    @NotBlank
    private String password;
}
