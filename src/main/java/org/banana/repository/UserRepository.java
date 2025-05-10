package org.banana.repository;

import org.banana.entity.User;
import org.banana.repository.crud.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {

//    void updateFirstNameAndLastName(UUID id, String firstName, String lastName);

    Optional<User> findFetchedById(UUID uuid);

    void updatePassword(UUID id, String password);

    void updateUsername(UUID id, String username);

    void updatePhone(UUID id, String phone);

    Optional<User> findByUsername(String username);

//    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);
}
