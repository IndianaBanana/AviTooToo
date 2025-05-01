package org.banana.controller;

import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserRegisterRequestDto;

/**
 * Created by Banana on 29.04.2025
 */
public interface AuthController {

    String login(UserLoginRequestDto requestDto);

    String register(UserRegisterRequestDto requestDto);
}
