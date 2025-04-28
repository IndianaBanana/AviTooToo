package org.banana.dto.user;

import org.banana.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.factory.Mappers.getMapper;

/**
 * Created by Banana on 25.04.2025
 */
@Mapper
public interface UserMapper {

    UserMapper INSTANCE = getMapper(UserMapper.class);

    UserResponseDto userToUserDto(User user);
    @Mapping(target = "userId", ignore = true)
    User userDtoToUser(UserResponseDto userResponseDto);

    User userRegisterRequestDtoToUser(UserRegisterRequestDto userRegisterRequestDto);
}
