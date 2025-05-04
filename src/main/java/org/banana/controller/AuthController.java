package org.banana.controller;

import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserRegisterRequestDto;
import org.springframework.http.ResponseEntity;

/**
 * Created by Banana on 29.04.2025
 */
public interface AuthController {

    ResponseEntity<String> login(UserLoginRequestDto requestDto);

    ResponseEntity<String> register(UserRegisterRequestDto requestDto);
}
