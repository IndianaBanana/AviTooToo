package org.banana.repository;

import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.entity.Advertisement;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementRepository extends CrudRepository<Advertisement, UUID> {

    void detach(Advertisement advertisement);

    Optional<AdvertisementResponseDto> findDtoById(UUID id);

    Optional<Advertisement> findFetchedById(UUID id);

    List<AdvertisementResponseDto> findAllFiltered(AdvertisementFilterDto filter, int page, int size);

    /**
     * Изменить количество продаваемой единицы в объявлении (используется для реализации оптимистичной блокировки)
     *
     * @param id          идентификатор объявления
     * @param oldQuantity старое количество
     * @param newQuantity новое количество
     * @return количество обновленных объявлений, если 1 то все хорошо, если 0 то что-то пошло не так.
     */
    int updateAdvertisementQuantity(UUID id, int oldQuantity, int newQuantity);
}
