package org.banana.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageMarkReadRequestDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.dto.message.MessageSendRequestDto;
import org.banana.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponseDto> sendMessage(@RequestBody @Valid MessageSendRequestDto requestDto) {
        MessageResponseDto created = messageService.sendMessage(requestDto);

        URI location = URI.create(String.format("/api/v1/messages/%s", created.getId()));

        return ResponseEntity
                .created(location)
                .body(created);
    }

    @PostMapping("/chat")
    public ResponseEntity<List<MessageResponseDto>> getMessages(@RequestBody @Valid MessageFilterDto filter) {
        List<MessageResponseDto> list = messageService.getListOfMessages(filter);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/mark-read")
    public ResponseEntity<Void> markReadUpTo(@RequestBody @Valid MessageMarkReadRequestDto dto) {
        messageService.markReadUpTo(dto);
        return ResponseEntity.noContent().build();
    }
}
