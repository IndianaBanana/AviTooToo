package org.banana.dto.comment;

import org.banana.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "commenter.id", source = "commenter.id")
    @Mapping(target = "commenter.firstName", source = "commenter.firstName")
    @Mapping(target = "commenter.lastName", source = "commenter.lastName")
    CommentResponseDto fromCommentToCommentResponseDto(Comment comment);

    @Mapping(target = "commenter.id", source = "commenter.id")
    @Mapping(target = "commenter.firstName", source = "commenter.firstName")
    @Mapping(target = "commenter.lastName", source = "commenter.lastName")
    List<CommentResponseDto> fromCommentListToCommentResponseDtoList(List<Comment> comments);
}
