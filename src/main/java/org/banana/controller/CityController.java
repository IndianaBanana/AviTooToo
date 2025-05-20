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
import lombok.RequiredArgsConstructor;
import org.banana.dto.city.CityDto;
import org.banana.service.CityService;
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
@RequestMapping("/api/v1/city")
@RequiredArgsConstructor
@Tag(name = "Города для размещения объявлений", description = "Управление городами: получение, поиск, добавление (для администратора)")
@ApiResponses({@ApiResponse(responseCode = "401", description = "Не корректный JWT или его отсутствие", content = @Content),})
public class CityController {

    private final CityService cityService;

    @Operation(
            summary = "Получить список всех городов",
            description = "Возвращает список всех зарегистрированных городов.",
            responses = {@ApiResponse(responseCode = "200", description = "Список городов успешно получен",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CityDto.class))))
            }
    )
    @GetMapping
    public List<CityDto> getAllCities() {
        return cityService.findAll();
    }

    @Operation(
            summary = "Поиск городов по шаблону",
            description = "Ищет города, содержащие переданный шаблон в названии.",
            parameters = {@Parameter(name = "pattern", description = "Шаблон для поиска по названию города", required = true, in = ParameterIn.PATH)},
            responses = {@ApiResponse(responseCode = "200", description = "Список городов, соответствующих шаблону")}
    )
    @GetMapping("/{pattern}")
    public List<CityDto> searchCities(@PathVariable String pattern) {
        return cityService.findByNameLike(pattern);
    }

    @Operation(
            summary = "Добавить новый город",
            description = "Добавляет город по имени. Только для пользователей с ролью ADMIN.",
            parameters = {@Parameter(name = "name", description = "Название нового города", required = true, in = ParameterIn.PATH)},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Город успешно создан", content = @Content(schema = @Schema(implementation = CityDto.class))),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Город с таким именем уже существует", content = @Content)
            }
    )
    @PostMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CityDto> addCity(@PathVariable String name) {
        CityDto cityDto = cityService.addCity(name);
        URI uri = URI.create(String.format("/api/v1/city/%s", cityDto.getName()));
        return ResponseEntity.created(uri).body(cityDto);
    }
}
