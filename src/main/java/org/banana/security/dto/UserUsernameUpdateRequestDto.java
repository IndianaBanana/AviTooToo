package org.banana.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by Banana on 29.04.2025
 */
@Data
public class UserUsernameUpdateRequestDto {

    @NotBlank
    private String newUsername;

    @NotBlank
    private String password;
}
