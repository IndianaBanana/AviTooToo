package org.banana.dto.comment;

import org.banana.dto.user.UserResponseDto;
import org.banana.entity.Comment;
import org.banana.entity.User;
import org.banana.security.dto.UserRegisterRequestDto;
import org.mapstruct.Mapper;

/**
 * Created by Banana on 25.04.2025
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentResponseDto fromCommentToCommentResponseDto(Comment comment);
}
