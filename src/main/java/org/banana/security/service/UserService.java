package org.banana.security.service;

import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserPasswordUpdateRequestDto;
import org.banana.security.dto.UserPhoneUpdateRequestDto;
import org.banana.security.dto.UserRegisterRequestDto;
import org.banana.security.dto.UserUsernameUpdateRequestDto;

public interface UserService {

    String register(UserRegisterRequestDto requestDto);

    String verify(UserLoginRequestDto requestDto);

    UserResponseDto updateUser(UserUpdateRequestDto userUpdateRequestDto);

    String updatePassword(UserPasswordUpdateRequestDto requestDto);

    String updateUsername(UserUsernameUpdateRequestDto requestDto);

    String updatePhone(UserPhoneUpdateRequestDto requestDto);

    void deleteUser(UserLoginRequestDto requestDto);
}
