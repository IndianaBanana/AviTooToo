package org.banana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.banana.dto.comment.CommentRequestDto;
import org.banana.dto.comment.CommentResponseDto;
import org.banana.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comment")
@Validated
@RequiredArgsConstructor
@Tag(name = "Комментарии", description = "Методы для работы с комментариями")
@ApiResponses({@ApiResponse(responseCode = "401", description = "Не корректный JWT или его отсутствие", content = @Content),})
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "Создать новый комментарий",
            description = "Создает новый комментарий к объявлению. Может быть корневым или ответом на другой комментарий",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Комментарий успешно создан",
                            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные входные данные", content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Объявление или родительский комментарий не найдены", content = @Content
                    )
            }
    )
    @PostMapping
    public ResponseEntity<CommentResponseDto> addComment(@Valid @RequestBody CommentRequestDto requestDto) {
        CommentResponseDto createdComment = commentService.addComment(requestDto);
        return ResponseEntity.created(URI.create("/api/v1/comment/" + createdComment.getId())).body(createdComment);
    }

    @Operation(
            summary = "Удалить комментарий",
            description = "Мягкое удаление комментария (помечает как удаленный). Доступно только автору или администратору",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Комментарий успешно удален", content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Нет прав для удаления комментария", content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Комментарий не найден", content = @Content
                    )
            },
            parameters = {
                    @Parameter(
                            description = "UUID комментария", required = true, example = "550e8400-e29b-41d4-a716-446655440000",
                            name = "commentId", in = ParameterIn.PATH
                    )
            }
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Получить комментарий по ID",
            description = "Возвращает полную информацию о комментарии",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Комментарий найден",
                            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Комментарий не найден", content = @Content
                    )
            },
            parameters = {
                    @Parameter(
                            description = "UUID комментария", required = true, example = "550e8400-e29b-41d4-a716-446655440000",
                            name = "commentId", in = ParameterIn.PATH
                    )
            }
    )
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> findCommentById(@PathVariable UUID commentId) {
        return ResponseEntity.ok(commentService.findCommentById(commentId));
    }

    @Operation(
            summary = "Получить комментарии объявления",
            description = "Возвращает пагинированный список корневых комментариев с их ответами",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список комментариев",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CommentResponseDto.class)))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Объявление не найдено", content = @Content
                    )
            },
            parameters = {
                    @Parameter(
                            description = "UUID объявления", required = true, example = "550e8400-e29b-41d4-a716-446655440000",
                            name = "advertisementId", in = ParameterIn.PATH
                    ),
                    @Parameter(
                            description = "Номер страницы (начинается с 0)", example = "0",
                            name = "page", in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            description = "Размер страницы", example = "10",
                            name = "size", in = ParameterIn.QUERY
                    )
            }
    )
    @GetMapping("/advertisement/{advertisementId}")
    public ResponseEntity<List<CommentResponseDto>> findAllByAdvertisementId(
            @PathVariable UUID advertisementId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ResponseEntity.ok(commentService.findAllByAdvertisementId(advertisementId, page, size));
    }
}
