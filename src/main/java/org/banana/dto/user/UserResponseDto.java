package org.banana.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.banana.security.UserRole;

import java.util.UUID;

@Data
public class UserResponseDto {

    @NotNull
    private UUID userId;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String phone;
    @NotBlank
    private String username;
    @ToString.Exclude
    @NotNull
    private String password;
    private UserRole role;
}
