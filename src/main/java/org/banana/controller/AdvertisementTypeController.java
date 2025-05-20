package org.banana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.banana.dto.advertisement.type.AdvertisementTypeDto;
import org.banana.service.AdvertisementTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/advertisement-type")
@RequiredArgsConstructor
@Tag(name = "Типы объявлений", description = "Методы для работы с типами объявлений")
@ApiResponses({@ApiResponse(responseCode = "401", description = "Не корректный JWT или его отсутствие", content = @Content),})
public class AdvertisementTypeController {

    private final AdvertisementTypeService advertisementTypeService;

    @Operation(
            summary = "Получить все типы объявлений",
            description = "Возвращает список всех существующих типов объявлений.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список типов объявлений успешно получен",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AdvertisementTypeDto.class))
                            )
                    )
            }
    )
    @GetMapping
    public List<AdvertisementTypeDto> getAllAdvertisementTypes() {
        return advertisementTypeService.findAll();
    }

    @Operation(
            summary = "Поиск типов объявлений по шаблону",
            description = "Ищет типы объявлений, содержащие указанный шаблон в названии (регистр не учитывается).",
            parameters = {
                    @Parameter(name = "pattern", description = "Шаблон для поиска", example = "дача")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Типы объявлений, соответствующие шаблону, успешно найдены",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AdvertisementTypeDto.class))
                            )
                    )
            }
    )
    @GetMapping("/{pattern}")
    public List<AdvertisementTypeDto> searchAdvertisementTypes(@PathVariable String pattern) {
        return advertisementTypeService.findByNameLike(pattern);
    }

    @Operation(
            summary = "Добавить новый тип объявления",
            description = "Создаёт новый тип объявления. Доступно только администраторам.",
            parameters = {
                    @Parameter(name = "name", description = "Название нового типа", example = "Дача")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Тип объявления успешно создан",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AdvertisementTypeDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён (требуется роль ADMIN)",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Тип объявления с таким названием уже существует",
                            content = @Content
                    )
            }
    )
    @PostMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdvertisementTypeDto> addAdvertisementType(@PathVariable String name) {
        AdvertisementTypeDto createdType = advertisementTypeService.addAdvertisementType(name);
        URI uri = URI.create(String.format("/api/v1/advertisement-type/%s", createdType.getName()));
        return ResponseEntity.created(uri).body(createdType);
    }
}
