package org.banana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.banana.dto.message.MessageFilterDto;
import org.banana.dto.message.MessageMarkReadRequestDto;
import org.banana.dto.message.MessageResponseDto;
import org.banana.dto.message.MessageSendRequestDto;
import org.banana.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/message")
@RequiredArgsConstructor
@Tag(name = "Сообщения", description = "Методы для работы с сообщениями")
@ApiResponses({@ApiResponse(responseCode = "401", description = "Не корректный JWT или его отсутствие", content = @Content),})
public class MessageController {

    private final MessageService messageService;

    @Operation(
            summary = "Отправить сообщение",
            description = "Создание нового сообщения в рамках объявления или личной переписки",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Сообщение успешно отправлено",
                            content = @Content(schema = @Schema(implementation = MessageResponseDto.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Невалидные входные данные", content = @Content

                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Нельзя отправить сообщение", content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Получатель или объявление не найдены", content = @Content
                    )
            }
    )
    @PostMapping
    public ResponseEntity<MessageResponseDto> sendMessage(@RequestBody @Valid MessageSendRequestDto requestDto) {
        MessageResponseDto created = messageService.addMessage(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @Operation(
            summary = "Получить историю сообщений",
            description = "Возвращает пагинированный список сообщений с фильтрацией по времени и статусу прочтения",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список сообщений",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MessageResponseDto.class)))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Невалидные параметры фильтрации", content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Диалог не найден, получатель или объявление не найдены", content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Нельзя иметь чат с самим собой", content = @Content
                    ),
            }
    )
    @PostMapping("/chat")
    public ResponseEntity<List<MessageResponseDto>> getMessages(@RequestBody @Valid MessageFilterDto filter) {
        List<MessageResponseDto> list = messageService.getListOfMessages(filter);
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Пометить сообщения как прочитанные",
            description = "Обновление статуса прочтения сообщений до указанного времени и ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Статус обновлен"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Невалидные параметры запроса", content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Диалог не найден, получатель или объявление не найдены", content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Нельзя иметь чат с самим собой", content = @Content
                    ),
            }
    )
    @PatchMapping("/mark-read")
    public ResponseEntity<Void> markReadUpTo(@RequestBody @Valid MessageMarkReadRequestDto dto) {
        messageService.markReadUpTo(dto);
        return ResponseEntity.noContent().build();
    }
}
