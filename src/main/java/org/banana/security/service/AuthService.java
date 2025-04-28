package org.banana.security.service;

import org.banana.dto.user.UserLoginRequestDto;
import org.banana.dto.user.UserRegisterRequestDto;

public interface AuthService {

    String register(UserRegisterRequestDto requestDto);


    String verify(UserLoginRequestDto requestDto);
}
