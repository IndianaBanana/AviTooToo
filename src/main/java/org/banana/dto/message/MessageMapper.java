package org.banana.dto.message;

import org.banana.entity.Message;
import org.mapstruct.Mapper;

/**
 * Created by Banana on 25.04.2025
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageResponseDto messageToMessageResponseDto(Message message);
}
