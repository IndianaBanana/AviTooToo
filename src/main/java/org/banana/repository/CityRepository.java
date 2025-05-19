package org.banana.repository;

import org.banana.dto.city.CityDto;
import org.banana.entity.City;
import org.banana.repository.crud.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface CityRepository extends CrudRepository<City, UUID> {

    /**
     * Возвращает список всех городов
     *
     * @return список городов
     */
    List<CityDto> findAllDto();

    /**
     * Возвращает список городов по названию
     *
     * @param pattern название города или часть названия. pattern = pattern% где % - любая последовательность символов
     * @return список городов
     */
    List<CityDto> findByNameLike(String pattern);

    /**
     * Проверяет, существует ли город с таким названием
     *
     * @param name название города
     * @return true если существует
     */
    boolean existsByName(String name);
}
