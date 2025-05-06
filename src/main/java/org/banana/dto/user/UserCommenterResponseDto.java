package org.banana.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Created by Banana on 06.05.2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCommenterResponseDto {

    private UUID id;

    private String firstName;

    private String lastName;

}
