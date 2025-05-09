package org.banana.dto.user;

import org.banana.entity.User;
import org.banana.security.dto.UserPrincipal;
import org.banana.security.dto.UserRegisterRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Created by Banana on 25.04.2025
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userRatingView", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    User userResponseDtoToUser(UserResponseDto userResponseDto);

    @Mapping(target = "averageRating", source = "userRatingView.averageRating")
    @Mapping(target = "ratingCount", source = "userRatingView.ratingCount")
    UserResponseDto userToUserResponseDto(User user);

    @Mapping(target = "userRatingView", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User userRegisterRequestDtoToUser(UserRegisterRequestDto userRegisterRequestDto);

    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    UserResponseDto userPrincipalToUserResponseDto(UserPrincipal userPrincipal);
}
