package org.banana.controller;

import lombok.RequiredArgsConstructor;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserRegisterRequestDto;
import org.banana.security.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Banana on 30.04.2025
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

    private final UserService userService;

    @Override
    @PostMapping("/login")
    public String login(@RequestBody UserLoginRequestDto requestDto) {
        return userService.verify(requestDto);
    }

    @Override
    @PostMapping("/register")
    public String register(UserRegisterRequestDto requestDto) {
        return userService.register(requestDto);
    }
}
