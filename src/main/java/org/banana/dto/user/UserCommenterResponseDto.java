package org.banana.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCommenterResponseDto {

    private UUID id;

    private String firstName;

    private String lastName;
}
