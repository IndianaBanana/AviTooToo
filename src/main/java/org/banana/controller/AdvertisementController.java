package org.banana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.service.AdvertisementService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/advertisement")
@RequiredArgsConstructor
@Validated
@Tag(name = "Объявления", description = "Операции с объявлениями")
@ApiResponses({@ApiResponse(responseCode = "401", description = "Не корректный JWT или его отсутствие", content = @Content),})
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @Operation(summary = "Получить объявление по ID", description = "Возвращает объявление по его UUID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Объявление найдено",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdvertisementResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Объявление не найдено", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementResponseDto> getAdvertisementById(
            @Parameter(in = ParameterIn.PATH, description = "UUID объявления", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(advertisementService.findById(id));
    }

    @Operation(summary = "Фильтрация объявлений", description = "Возвращает список объявлений по заданным критериям с пагинацией",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список объявлений успешно возвращен",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdvertisementResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Неверные параметры фильтрации или пагинации", content = @Content)
            },
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", description = "Номер страницы (0-основанная)", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", description = "Размер страницы", example = "20")
            }
    )
    @PostMapping("/filter")
    public ResponseEntity<List<AdvertisementResponseDto>> getFilteredAdvertisements(
            @Valid @RequestBody AdvertisementFilterDto filter,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        return ResponseEntity.ok(advertisementService.findAllFiltered(filter, page, size));
    }

    @Operation(summary = "Создать объявление", description = "Добавляет новое объявление с указанными данными",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Объявление успешно создано",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdvertisementResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Неверные данные объявления", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<AdvertisementResponseDto> createAdvertisement(@RequestBody @Valid AdvertisementRequestDto requestDto) {
        AdvertisementResponseDto created = advertisementService.addAdvertisement(requestDto);
        URI uri = URI.create(String.format("/api/v1/advertisement/%s", created.getId()));
        return ResponseEntity.created(uri).body(created);
    }

    @Operation(summary = "Обновить объявление", description = "Полностью заменяет данные существующего объявления",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Объявление успешно обновлено",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdvertisementResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Неверные данные", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Объявление не найдено", content = @Content),
                    @ApiResponse(responseCode = "409", description = "не владелец или неверное состояние объявления", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<AdvertisementResponseDto> updateAdvertisement(
            @PathVariable("id") UUID advertisementId,
            @RequestBody @Valid AdvertisementRequestDto requestDto) {
        return ResponseEntity.ok(advertisementService.updateAdvertisement(advertisementId, requestDto));
    }

    @Operation(summary = "Закрыть объявление", description = "Устанавливает статус объявления как закрытое",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Объявление успешно закрыто",
                            content = @Content(schema = @Schema(implementation = AdvertisementResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "не владелец или объявление уже закрыто", content = @Content)
            }
    )
    @PatchMapping("/{id}/close")
    public ResponseEntity<AdvertisementResponseDto> closeAdvertisement(@PathVariable UUID id) {
        return ResponseEntity.ok(advertisementService.closeAdvertisement(id));
    }

    @Operation(summary = "Повторно открыть объявление", description = "Открывает ранее закрытое объявление",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Объявление успешно повторно открыто",
                            content = @Content(schema = @Schema(implementation = AdvertisementResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "не владелец или объявление не было закрыто", content = @Content)
            }
    )
    @PatchMapping("/{id}/reopen")
    public ResponseEntity<AdvertisementResponseDto> reopenAdvertisement(@PathVariable UUID id) {
        return ResponseEntity.ok(advertisementService.reopenAdvertisement(id));
    }

    @Operation(summary = "Продвинуть объявление", description = "Устанавливает признак продвижения объявления",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Объявление успешно продвинуто",
                            content = @Content(schema = @Schema(implementation = AdvertisementResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "не владелец, уже продвинуто или объявление закрыто", content = @Content)
            }
    )
    @PatchMapping("/{id}/promote")
    public ResponseEntity<AdvertisementResponseDto> promoteAdvertisement(@PathVariable UUID id) {
        return ResponseEntity.ok(advertisementService.promoteAdvertisement(id));
    }

    @Operation(summary = "Удалить объявление", description = "Удаляет объявление по его UUID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Объявление успешно удалено"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен: не владелец или администратор", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Объявление не найдено", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable UUID id) {
        advertisementService.deleteAdvertisement(id);
        return ResponseEntity.noContent().build();
    }
}
