package org.banana.dto.user;

import org.banana.entity.User;
import org.banana.security.dto.UserRegisterRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Created by Banana on 25.04.2025
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    User userResponseDtoToUser(UserResponseDto userResponseDto);

//    @Mapping(target = "averageRating", source = "userRatingView.averageRating")
//    @Mapping(target = "ratingCount", source = "userRatingView.ratingCount")
    UserResponseDto userToUserResponseDto(User user);

    User userRegisterRequestDtoToUser(UserRegisterRequestDto userRegisterRequestDto);
}
