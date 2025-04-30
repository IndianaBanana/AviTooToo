package org.banana.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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
