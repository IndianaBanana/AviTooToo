package org.banana.repository;

import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.entity.Advertisement;
import org.banana.repository.crud.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementRepository extends CrudRepository<Advertisement, UUID> {


    Optional<AdvertisementResponseDto> findDtoById(UUID id);

    void promoteAdvertisement(UUID id);

    void closeAdvertisement(UUID id, LocalDateTime closeDate);

    List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size);
}
