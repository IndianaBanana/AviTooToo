package org.banana.controller;

import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.security.dto.UserPasswordUpdateRequestDto;
import org.banana.security.dto.UserPhoneUpdateRequestDto;
import org.banana.security.dto.UserUsernameUpdateRequestDto;

/**
 * Created by Banana on 30.04.2025
 */
public class UserControllerImpl implements UserController {

    @Override
    public UserResponseDto getCurrentUser() {
        return null;
    }

    @Override
    public UserResponseDto updateUser(UserUpdateRequestDto requestDto) {
        return null;
    }

    @Override
    public String updatePassword(UserPasswordUpdateRequestDto requestDto) {
        return "";
    }

    @Override
    public String updatePhone(UserPhoneUpdateRequestDto requestDto) {
        return "";
    }

    @Override
    public String updateUsername(UserUsernameUpdateRequestDto requestDto) {
        return "";
    }
}
