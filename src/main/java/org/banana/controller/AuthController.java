package org.banana.controller;

import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserRegisterRequestDto;
import org.springframework.http.ResponseEntity;

public interface AuthController {

    ResponseEntity<String> login(UserLoginRequestDto requestDto);

    ResponseEntity<String> register(UserRegisterRequestDto requestDto);
}
