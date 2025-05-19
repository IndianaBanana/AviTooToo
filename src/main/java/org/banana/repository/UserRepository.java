package org.banana.repository;

import org.banana.entity.User;
import org.banana.repository.crud.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {

    /**
     * Находит пользователя по uuid и сразу подгружает его RatingView
     *
     * @param uuid идентификатор юзера
     * @return Optional<User> будет пустой если такого юзера нет
     */
    Optional<User> findFetchedById(UUID uuid);

    /**
     * Обновляет пароль пользователя,
     *
     * @param id       идентификатор юзера
     * @param password обязательно заранее зашифрованный пароль
     */
    void updatePassword(UUID id, String password);

    /**
     * Обновляет username пользователя
     *
     * @param id       идентификатор юзера
     * @param username новый username
     */
    void updateUsername(UUID id, String username);

    /**
     * Обновляет телефон
     *
     * @param id    идентификатор юзера
     * @param phone новый телефон
     */
    void updatePhone(UUID id, String phone);

    /**
     * @param username уникальная строка для каждого пользователя в системе
     * @return Optional<User> будет пустой если такого юзера нет
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверяет есть ли юзер с таким username
     *
     * @param username уникальная строка для каждого пользователя в системе
     * @return true если такой юзер есть
     */
    boolean existsByUsername(String username);

    /**
     * Проверяет есть ли юзер с таким телефоном
     *
     * @param phone телефон пользователя
     * @return true если такой юзер есть
     */
    boolean existsByPhone(String phone);
}
