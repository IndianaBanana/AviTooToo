package org.banana.dto.user;

import jakarta.validation.constraints.NotBlank;

/**
 * Created by Banana on 19.04.2025
 */
public class UserAuthDto {

    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
