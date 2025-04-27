package org.banana.security.service;

import org.banana.dto.user.UserDto;

public interface AuthService {

    UserDto register(UserDto userDto);


    String verify(UserDto user);
}
