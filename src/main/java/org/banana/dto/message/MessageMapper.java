package org.banana.dto.message;

import org.banana.entity.Message;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageResponseDto messageToMessageResponseDto(Message message);
}
