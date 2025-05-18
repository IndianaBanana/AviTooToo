package org.banana.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;


@Data
@AllArgsConstructor
public class UserLoginRequestDto {

    @NotBlank
    private String username;

    @NotBlank
    @ToString.Exclude
    private String password;
}
