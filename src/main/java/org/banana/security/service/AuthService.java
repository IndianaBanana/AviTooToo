package org.banana.security.service;

import org.banana.dto.UserDto;

public interface AuthService {

    UserDto register(UserDto userDto);


    String verify(UserDto user);
}
