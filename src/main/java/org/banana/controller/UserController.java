package org.banana.controller;

import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserPasswordUpdateRequestDto;
import org.banana.security.dto.UserPhoneUpdateRequestDto;
import org.banana.security.dto.UserUsernameUpdateRequestDto;

/**
 * Created by Banana on 29.04.2025
 */
public interface UserController {

    UserResponseDto getCurrentUser();

    UserResponseDto updateUser(UserUpdateRequestDto requestDto);

    String updatePassword(UserPasswordUpdateRequestDto requestDto);

    String updatePhone(UserPhoneUpdateRequestDto requestDto);

    String updateUsername(UserUsernameUpdateRequestDto requestDto);

    void deleteUser(UserLoginRequestDto requestDto);
}
