package org.banana.dto.advertisement;

import org.banana.dto.user.UserMapper;
import org.banana.entity.Advertisement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {UserMapper.class}, componentModel = "spring")
public interface AdvertisementMapper {

    @Mapping(source = "user", target = "userResponseDto")
//    @Mapping(target = "userResponseDto.averageRating", ignore = true)
//    @Mapping(target = "userResponseDto.ratingCount", ignore = true)
    @Mapping(source = "city.name", target = "cityName")
    @Mapping(source = "advertisementType.name", target = "advertisementType")
    AdvertisementResponseDto advertisementToAdvertisementResponseDto(Advertisement advertisement);

//    @Mapping(source = "city.name", target = "cityName")
//    @Mapping(source = "advertisementType.name", target = "advertisementType")
//    AdvertisementResponseDto advertisementToAdvertisementResponseDtoWithoutUser(Advertisement advertisement);

    Advertisement advertisementRequestDtoToAdvertisement(AdvertisementRequestDto advertisementRequestDto);

    List<AdvertisementResponseDto> advertisementListToAdvertisementResponseDtoList(List<Advertisement> advertisements);
}
