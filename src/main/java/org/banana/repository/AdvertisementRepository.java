package org.banana.repository;

import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.entity.Advertisement;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementRepository extends CrudRepository<Advertisement, UUID> {

    /**
     * Отсоединить объявление от сессии
     *
     * @param advertisement объявление которое нужно отсоединить от сессии
     */
    void detach(Advertisement advertisement);

    /**
     * Возвращает DTO объявления по идентификатору
     *
     * @param id идентификатор объявления
     * @return Optional<AdvertisementResponseDto> будет пустой если такого объявления нет
     */
    Optional<AdvertisementResponseDto> findDtoById(UUID id);

    /**
     * Возвращает объявление по идентификатору присоединяя все связанные с ним сущности
     *
     * @param id идентификатор объявления
     * @return Optional<Advertisement> будет пустой если такого объявления нет
     */
    Optional<Advertisement> findFetchedById(UUID id);

    /**
     * Возвращает список объявлений по фильтру
     *
     * @param filter фильтр по которому происходит поиск
     * @param page   offset для пагинации. На столько будет смещено начало списка
     * @param size   максимальное количество объявлений в запросе
     * @return список объявлений
     */
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
