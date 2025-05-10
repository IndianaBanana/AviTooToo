package org.banana.repository;

import lombok.extern.slf4j.Slf4j;
import org.banana.entity.User;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
public class UserRepositoryImpl extends AbstractCrudRepositoryImpl<User, UUID> implements UserRepository {

    private static final String EXISTS_BY_USERNAME = "SELECT 1 FROM User u WHERE u.username = :username";
    private static final String EXISTS_BY_PHONE = "SELECT 1 FROM User u WHERE u.phone = :phone";
    //    private static final String FIND_BY_USERNAME = "SELECT u.id, u.firstName, u.lastName,u.phone,u.username, u.password,u.role FROM User u WHERE u.username = :username";
    private static final String FIND_BY_USERNAME = "SELECT u FROM User u WHERE u.username = :username";
    //    private static final String FIND_BY_ID = "SELECT u FROM User u WHERE u.id = :id";
    private static final String FIND_BY_ID = "SELECT u FROM User u LEFT JOIN FETCH u.userRatingView WHERE u.id = :id";
    //    private static final String FIND_BY_PHONE = "SELECT u FROM User u WHERE u.phone = :phone";
    private static final String UPDATE_PASSWORD = "UPDATE User u SET u.password = :password WHERE u.id = :id";
    private static final String UPDATE_USERNAME = "UPDATE User u SET u.username = :username WHERE u.id = :id";
    private static final String UPDATE_PHONE = "UPDATE User u SET u.phone = :phone WHERE u.id = :id";

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public Optional<User> findFetchedById(UUID uuid) {
        log.debug("entering `findFetchedById` method in {}", this.getClass().getSimpleName());
        return Optional.ofNullable(getSession().createQuery(FIND_BY_ID, User.class)
                .setParameter("id", uuid)
                .getSingleResultOrNull());
    }

    @Override
    public void updatePassword(UUID id, String password) {
        log.debug("entering `updatePassword` method in {}", this.getClass().getSimpleName());
        getSession().createMutationQuery(UPDATE_PASSWORD)
                .setParameter("password", password)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public void updateUsername(UUID id, String username) {
        log.debug("entering `updateUsername` method in {}", this.getClass().getSimpleName());
        getSession().createMutationQuery(UPDATE_USERNAME)
                .setParameter("username", username)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public void updatePhone(UUID id, String phone) {
        log.debug("entering `updatePhone` method in {}", this.getClass().getSimpleName());
        getSession().createMutationQuery(UPDATE_PHONE)
                .setParameter("phone", phone)
                .setParameter("id", id)
                .executeUpdate();
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
}
