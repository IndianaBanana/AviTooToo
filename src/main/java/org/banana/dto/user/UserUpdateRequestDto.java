package org.banana.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Данные для обновления профиля")
public class UserUpdateRequestDto {

    @Schema(description = "Новое имя", example = "Иван")
    @NotBlank
    private String firstName;

    @Schema(description = "Новая фамилия", example = "Петров")
    @NotBlank
    private String lastName;
}
