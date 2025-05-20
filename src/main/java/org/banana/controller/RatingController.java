package org.banana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.banana.dto.rating.RatingDto;
import org.banana.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rating")
@RequiredArgsConstructor
@Tag(name = "Рейтинг", description = "Рейтинг пользователей")
@ApiResponses({@ApiResponse(responseCode = "401", description = "Не корректный JWT или его отсутствие", content = @Content),})
public class RatingController {

    private final RatingService ratingService;

    @Operation(
            summary = "Оценить пользователя",
            description = "Добавление или обновление оценки пользователю. Обновление актуального рейтинга пользователя происходит асинхронно с задержкой 15 минут.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Оценка принята, будет обновлена в течение 15 минут",
                            content = @Content(schema = @Schema(implementation = String.class, example = "User rating will be updated in 15 minutes."))
                    ),
                    @ApiResponse(responseCode = "400", description = "Невалидные входные данные", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Попытка оценить самого себя", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
            }
    )
    @PostMapping("")
    public ResponseEntity<String> rateUser(@Valid @RequestBody RatingDto ratingDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.addRating(ratingDto));
    }

    @Operation(
            summary = "Удалить оценку",
            description = "Удаление ранее поставленной оценки пользователю. Обновление актуального рейтинга пользователя происходит асинхронно с задержкой 15 минут.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Оценка удалена, изменения вступят в силу в течение 15 минут",
                            content = @Content(schema = @Schema(implementation = String.class, example = "User rating will be updated in 15 minutes."))
                    ),
                    @ApiResponse(responseCode = "400", description = "Невалидный UUID пользователя", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Попытка удалить оценку самого себя", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
            }
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> removeRating(@Valid @PathVariable UUID userId) {
        return ResponseEntity.ok(ratingService.deleteRating(userId));
    }
}
