package org.banana.dto.advertisement.type;

import org.banana.entity.AdvertisementType;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Created by Banana on 25.04.2025
 */
@Mapper(componentModel = "spring")
public interface AdvertisementTypeMapper {

    List<AdvertisementTypeDto> advertisementTypesToAdvertisementTypeDtos(List<AdvertisementType> advertisementTypes);

    AdvertisementTypeDto advertisementTypeToAdvertisementTypeDto(AdvertisementType advertisementType);
}
