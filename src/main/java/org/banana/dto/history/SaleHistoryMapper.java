package org.banana.dto.history;

import org.banana.entity.SaleHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Created by Banana on 25.04.2025
 */
@Mapper(componentModel = "spring")
public interface SaleHistoryMapper {

    @Mapping(target = "advertisementId", source = "advertisement.id")
    @Mapping(target = "advertisementTitle", source = "advertisement.title")
    SaleHistoryResponseDto fromSaleHistoryToSaleHistoryResponseDto(SaleHistory saleHistory);
}
