package org.banana.security.service;

import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserRegisterRequestDto;

public interface AuthService {

    String register(UserRegisterRequestDto requestDto);


    String verify(UserLoginRequestDto requestDto);
}
