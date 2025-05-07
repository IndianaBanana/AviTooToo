package org.banana.service;

import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.dto.message.MessageSendRequestDto;

import java.util.List;

/**
 * Created by Banana on 04.05.2025
 */
public interface MessageService {

    MessageResponseDto sendMessage(MessageSendRequestDto requestDto);

    List<MessageResponseDto> getListOfMessages(MessageFilterDto filter);
}
