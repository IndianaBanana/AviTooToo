package org.banana.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserPasswordUpdateRequestDto;
import org.banana.security.dto.UserPhoneUpdateRequestDto;
import org.banana.security.dto.UserUsernameUpdateRequestDto;
import org.banana.security.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Banana on 30.04.2025
 */
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Override
    @GetMapping("")
    public UserResponseDto getCurrentUser() {
        return userService.getCurrentUser();
    }

    @Override
    @PatchMapping("")
    public UserResponseDto updateUser(@RequestBody @Valid UserUpdateRequestDto requestDto) {
        return userService.updateUser(requestDto);
    }

    @Override
    @PatchMapping("/security/password")
    public String updatePassword(@RequestBody @Valid UserPasswordUpdateRequestDto requestDto) {
        return userService.updatePassword(requestDto);
    }

    @Override
    @PatchMapping("/security/phone")
    public String updatePhone(@RequestBody @Valid UserPhoneUpdateRequestDto requestDto) {
        return userService.updatePhone(requestDto);
    }

    @Override
    @PatchMapping("/security/username")
    public String updateUsername(@RequestBody @Valid UserUsernameUpdateRequestDto requestDto) {
        return userService.updateUsername(requestDto);
    }

    @Override
    @PatchMapping("/security/delete")
    public void deleteUser(@RequestBody @Valid UserLoginRequestDto requestDto) {
        userService.deleteUser(requestDto);
    }
}
