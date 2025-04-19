package org.banana.security.service;

import org.banana.dto.user.UserResponseDto;

public interface AuthService {

    UserResponseDto register(UserResponseDto userResponseDto);


    String verify(UserResponseDto user);
}
