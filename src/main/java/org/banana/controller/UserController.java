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
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PatchMapping("")
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody @Valid UserUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUser(requestDto));
    }

    @PatchMapping("/security/password")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid UserPasswordUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updatePassword(requestDto));
    }

    @PatchMapping("/security/phone")
    public ResponseEntity<String> updatePhone(@RequestBody @Valid UserPhoneUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updatePhone(requestDto));
    }

    @PatchMapping("/security/username")
    public ResponseEntity<String> updateUsername(@RequestBody @Valid UserUsernameUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUsername(requestDto));
    }

    @DeleteMapping("/security")
    public ResponseEntity<Void> deleteUser(@RequestBody @Valid UserLoginRequestDto requestDto) {
        userService.deleteUser(requestDto);
        return ResponseEntity.noContent().build();
    }
}
