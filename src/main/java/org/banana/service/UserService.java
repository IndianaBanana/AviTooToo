package org.banana.service;

import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;

public interface UserService {
    UserResponseDto updateUser(UserUpdateRequestDto userUpdateRequestDto);
}
