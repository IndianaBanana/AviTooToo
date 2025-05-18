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
     * Обновляет имя пользователя
     *
     * @param id       идентификатор юзера
     * @param username новое имя
     * @throws
     */
    void updateUsername(UUID id, String username);

    void updatePhone(UUID id, String phone);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);
}
