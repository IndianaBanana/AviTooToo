package org.banana.dto.advertisement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.banana.dto.user.UserResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementResponseDto {

    @Schema(description = "UUID объявления", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Данные пользователя, создавшего объявление")
    private UserResponseDto userResponseDto;

    @Schema(description = "Название города", example = "Москва")
    private String cityName;

    @Schema(description = "Тип объявления", example = "Дача")
    private String advertisementType;

    @Schema(description = "Заголовок объявления", example = "Продам гараж")
    private String title;

    @Schema(description = "Описание объявления", example = "В хорошем состоянии, раз в год заезжал, от жены прятался")
    private String description;

    @Schema(description = "Цена объявления", example = "15000.00")
    private BigDecimal price;

    @Schema(description = "Количество единиц товара", example = "1")
    private Integer quantity;

    @Schema(description = "Флаг продвижения объявления", example = "false")
    private boolean isPromoted;

    @Schema(description = "Дата и время создания объявления", example = "2025-05-20T14:30:00")
    private LocalDateTime createDate;

    @Schema(description = "Дата и время закрытия объявления, если закрыто", example = "2025-05-25T10:00:00")
    private LocalDateTime closeDate;
}
