package org.banana.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

/**
 * Created by Banana on 27.04.2025
 */
@Data
public class UserUpdateRequestDto {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

//    @NotBlank
//    private String phone;
//
//    @NotBlank
//    private String username;
}
