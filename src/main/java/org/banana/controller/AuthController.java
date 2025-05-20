package org.banana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserRegisterRequestDto;
import org.banana.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Методы регистрации и входа пользователя")

public class AuthController {

    private final UserService userService;

    @Operation(
            summary = "Аутентификация и авторизация пользователя",
            description = "Аутентификация пользователя по email и паролю. Возвращает JWT токен.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный вход, JWT в теле ответа",
                            content = @Content(mediaType = "text/plain")),
                    @ApiResponse(responseCode = "401", description = "Неверные учетные данные", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content)
            }
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        String jwt = userService.verify(requestDto);
        return ResponseEntity.ok(jwt);
    }

    @Operation(
            summary = "Регистрация пользователя",
            description = "Регистрация нового пользователя. Возвращает JWT токен.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован. JWT в теле ответа",
                            content = @Content(mediaType = "text/plain")),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Пользователь с таким email/phone уже зарегистрирован", content = @Content)
            }
    )
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRegisterRequestDto requestDto) {
        String jwt = userService.register(requestDto);
        return ResponseEntity
                .created(URI.create("/api/v1/user"))
                .body(jwt);
    }
}