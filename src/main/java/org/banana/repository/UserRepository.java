package org.banana.repository;

import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.entity.User;
import org.banana.repository.crud.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    UserResponseDto updateUser(UserUpdateRequestDto userRequest);
}
