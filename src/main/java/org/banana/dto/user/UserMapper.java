package org.banana.dto.user;

import org.banana.entity.User;
import org.banana.security.dto.UserRegisterRequestDto;
import org.mapstruct.Mapper;

import static org.mapstruct.factory.Mappers.getMapper;

/**
 * Created by Banana on 25.04.2025
 */
@Mapper
public interface UserMapper {

    UserMapper INSTANCE = getMapper(UserMapper.class);

    UserResponseDto userToUserDto(User user);

    User userResponseDtoToUser(UserResponseDto userResponseDto);

    UserResponseDto userToUserResponseDto(User user);

    User userRegisterRequestDtoToUser(UserRegisterRequestDto userRegisterRequestDto);
}
