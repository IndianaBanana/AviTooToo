package org.banana.controller;

import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserPasswordUpdateRequestDto;
import org.banana.security.dto.UserPhoneUpdateRequestDto;
import org.banana.security.dto.UserUsernameUpdateRequestDto;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Created by Banana on 29.04.2025
 */
public interface UserController {

    ResponseEntity<UserResponseDto> findById(UUID id);

    ResponseEntity<UserResponseDto> getCurrentUser();

    ResponseEntity<UserResponseDto> updateUser(UserUpdateRequestDto requestDto);

    ResponseEntity<String> updatePassword(UserPasswordUpdateRequestDto requestDto);

    ResponseEntity<String> updatePhone(UserPhoneUpdateRequestDto requestDto);

    ResponseEntity<String> updateUsername(UserUsernameUpdateRequestDto requestDto);

    ResponseEntity<Void> deleteUser(UserLoginRequestDto requestDto);
}
