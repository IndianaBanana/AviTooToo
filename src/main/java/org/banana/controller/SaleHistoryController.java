package org.banana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.banana.dto.history.SaleHistoryAddRequestDto;
import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;
import org.banana.service.SaleHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sale-history")
@RequiredArgsConstructor
@Tag(name = "История продаж", description = "Методы для работы с историей продаж")
@ApiResponses({@ApiResponse(responseCode = "401", description = "Не корректный JWT или его отсутствие", content = @Content),})
public class SaleHistoryController {

    private final SaleHistoryService saleHistoryService;

    @Operation(
            summary = "Добавить запись о продаже",
            description = "Создание новой записи о продаже товара из объявления",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Продажа успешно зарегистрирована",
                            content = @Content(schema = @Schema(implementation = SaleHistoryResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Невалидные входные данные", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Объявление не найдено", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Количество проданных единиц больше, чем осталось в объявлении", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Произошла ошибка во время обработки запроса", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<SaleHistoryResponseDto> addSale(@RequestBody @Valid SaleHistoryAddRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saleHistoryService.addSale(requestDto));
    }

    @Operation(
            summary = "Получить историю продаж по объявлению",
            description = "Список всех продаж для конкретного объявления",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список продаж",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SaleHistoryResponseDto.class)))
                    ),
                    @ApiResponse(responseCode = "403", description = "Нет прав на просмотр истории", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Объявление не найдено", content = @Content)
            },
            parameters = {@Parameter(in = ParameterIn.PATH, name = "adId", description = "UUID объявления",
                    required = true, schema = @Schema(type = "string", format = "uuid"))}
    )
    @GetMapping("/advertisement/{adId}")
    public ResponseEntity<List<SaleHistoryResponseDto>> getSalesByAdvertisement(@PathVariable("adId") UUID advertisementId) {

        List<SaleHistoryResponseDto> sales =
                saleHistoryService.getSalesByAdvertisementId(advertisementId);

        return ResponseEntity.ok(sales);
    }

    @Operation(
            summary = "Получить агрегированную статистику",
            description = "Суммарная информация по продажам для всех объявлений пользователя",
            security = @SecurityRequirement(name = "bearer-jwt"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Статистика по продажам",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SaleHistoryTotalForAdvertisementsResponseDto.class)))
                    )
            }
    )
    @GetMapping("/totals")
    public ResponseEntity<List<SaleHistoryTotalForAdvertisementsResponseDto>> getTotals() {
        List<SaleHistoryTotalForAdvertisementsResponseDto> totals =
                saleHistoryService.getTotalForSalesInAdvertisements();

        return ResponseEntity.ok(totals);
    }

    @Operation(
            summary = "Отменить запись о продаже",
            description = "Отмена записи о продаже товара из объявления с возвратом количества проданных единиц",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Продажа успешно зарегистрирована",
                            content = @Content(schema = @Schema(implementation = SaleHistoryResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Невалидные входные данные", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Нет прав на отмену продажи", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Запись о продаже не найдена", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Произошла ошибка во время обработки запроса", content = @Content)
            },
            parameters = {@Parameter(in = ParameterIn.PATH, name = "saleId",
                    description = "UUID записи о продаже", required = true, schema = @Schema(type = "string", format = "uuid"))}
    )
    @DeleteMapping("/{saleId}")
    public ResponseEntity<Void> cancelSale(@PathVariable UUID saleId) {
        saleHistoryService.deleteSale(saleId);
        return ResponseEntity.noContent().build();
    }
}
