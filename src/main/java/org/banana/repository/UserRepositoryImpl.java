package org.banana.repository;

import lombok.extern.slf4j.Slf4j;
import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.entity.User;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
public class UserRepositoryImpl extends AbstractCrudRepositoryImpl<User, UUID> implements UserRepository {

    public static final String EXISTS_BY_USERNAME = "SELECT 1 FROM User c WHERE c.username = :username";
    public static final String EXISTS_BY_PHONE = "SELECT 1 FROM User c WHERE c.phone = :phone";
    public static final String FIND_BY_USERNAME = "SELECT c FROM User c WHERE c.username = :username";
    public static final String FIND_BY_PHONE = "SELECT c FROM User c WHERE c.phone = :phone";

    public UserRepositoryImpl() {
        super(User.class);
    }


    @Override
    public Optional<User> findByUsername(String username) {
        log.debug("entering `findByUsername` method in {}", this.getClass().getSimpleName());
        return Optional.ofNullable(
                getSession().createQuery(FIND_BY_USERNAME, User.class)
                        .setParameter("username", username)
                        .getSingleResultOrNull()
        );
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        log.debug("entering `findByPhone` method in {}", this.getClass().getSimpleName());
        return Optional.ofNullable(
                getSession().createQuery(FIND_BY_PHONE, User.class)
                        .setParameter("phone", phone)
                        .getSingleResultOrNull()
        );
    }

    @Override
    public boolean existsByUsername(String username) {
        log.debug("entering `existsByUsername` method in {}", this.getClass().getSimpleName());
        Integer result = getSession().createQuery(EXISTS_BY_USERNAME, Integer.class)
                .setParameter("username", username)
                .getSingleResultOrNull();
        return result != null && result == 1;
    }

    @Override
    public boolean existsByPhone(String phone) {
        log.debug("entering `existsByPhone` method in {}", this.getClass().getSimpleName());
        Integer result = getSession().createQuery(EXISTS_BY_PHONE, Integer.class)
                .setParameter("phone", phone)
                .getSingleResultOrNull();
        return result != null && result == 1;
    }

    @Override
    public UserResponseDto updateUser(UserUpdateRequestDto userRequest) {
        return null;
    }
}
