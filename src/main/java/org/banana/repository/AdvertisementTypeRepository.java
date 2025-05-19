package org.banana.repository;

import org.banana.dto.advertisement.type.AdvertisementTypeDto;
import org.banana.entity.AdvertisementType;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AdvertisementTypeRepository extends CrudRepository<AdvertisementType, UUID> {

    /**
     * Возвращает типы объявлений по паттерну
     *
     * @param pattern паттерн дял поиска.pattern = %pattern% где % - любая последовательность символов
     * @return список типов
     */
    List<AdvertisementTypeDto> findByNameLike(String pattern);

    /**
     * Возвращает все типы объявлений
     *
     * @return список типов
     */
    List<AdvertisementTypeDto> findAllDto();

    /**
     * Проверяет наличие типа объявления по названию
     *
     * @param name название типа объявления
     * @return true если тип объявления существует
     */
    boolean existsByName(String name);
}
