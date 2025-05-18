package org.banana.service;

import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageMarkReadRequestDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.dto.message.MessageSendRequestDto;

import java.util.List;


public interface MessageService {

    MessageResponseDto addMessage(MessageSendRequestDto requestDto);

    void markReadUpTo(MessageMarkReadRequestDto dto);

    List<MessageResponseDto> getListOfMessages(MessageFilterDto filter);
}
