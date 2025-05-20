package org.banana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserPasswordUpdateRequestDto;
import org.banana.security.dto.UserPhoneUpdateRequestDto;
import org.banana.security.dto.UserUsernameUpdateRequestDto;
import org.banana.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Методы для работы с пользователями")
@ApiResponses({@ApiResponse(responseCode = "401", description = "Не корректный JWT или его отсутствие", content = @Content),})
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Получение публичной информации о пользователе",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Информация о пользователе",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
            },
            parameters = {@Parameter(name = "id", description = "ID пользователя", required = true, in = ParameterIn.PATH)}
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(
            summary = "Получить текущего пользователя",
            description = "Получение полной информации о текущем аутентифицированном пользователе",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Данные пользователя",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class)))
            }
    )
    @GetMapping("")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @Operation(
            summary = "Обновить профиль",
            description = "Обновление имени и фамилии пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Обновленные данные",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Старые имя и фамилия такие же как и раньше", content = @Content)
            }
    )
    @PatchMapping("")
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody @Valid UserUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUser(requestDto));
    }

    @Operation(
            summary = "Сменить пароль",
            description = "Обновление пароля с подтверждением текущего",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Новый JWT токен",
                            content = @Content(schema = @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
                    ),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Неверный текущий пароль", content = @Content)
            }
    )
    @PatchMapping("/security/password")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid UserPasswordUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updatePassword(requestDto));
    }

    @Operation(
            summary = "Сменить телефон",
            description = "Обновление номера телефона с подтверждением пароля",
            security = @SecurityRequirement(name = "bearer-jwt"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Новый JWT токен",
                            content = @Content(schema = @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
                    ),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Неверный текущий пароль", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Номер уже используется", content = @Content)
            }
    )
    @PatchMapping("/security/phone")
    public ResponseEntity<String> updatePhone(@RequestBody @Valid UserPhoneUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updatePhone(requestDto));
    }

    @Operation(
            summary = "Сменить email",
            description = "Обновление email с подтверждением пароля",
            security = @SecurityRequirement(name = "bearer-jwt"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Новый JWT токен",
                            content = @Content(schema = @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
                    ),
                    @ApiResponse(responseCode = "400", description = "Невалидные данные", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Неверный текущий пароль", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Email уже используется", content = @Content)
            }
    )
    @PatchMapping("/security/username")
    public ResponseEntity<String> updateUsername(@RequestBody @Valid UserUsernameUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUsername(requestDto));
    }

    @Operation(
            summary = "Удалить аккаунт",
            description = "Полное удаление аккаунта с подтверждением пароля и username",
            security = @SecurityRequirement(name = "bearer-jwt"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Аккаунт удален"),
                    @ApiResponse(responseCode = "401", description = "Неверные учетные данные", content = @Content)
            }
    )
    @DeleteMapping("/security")
    public ResponseEntity<Void> deleteUser(@RequestBody @Valid UserLoginRequestDto requestDto) {
        userService.deleteUser(requestDto);
        return ResponseEntity.noContent().build();
    }
}
