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

    private static final String EXISTS_BY_USERNAME = "select 1 from User u where u.username = :username";
    private static final String EXISTS_BY_PHONE = "select 1 from User u where u.phone = :phone";
    private static final String FIND_BY_USERNAME = "select u from User u where u.username = :username";
    private static final String FIND_BY_ID = "select u from User u left join fetch u.userRatingView where u.id = :id";
    private static final String UPDATE_PASSWORD = "update User u set u.password = :password where u.id = :id";
    private static final String UPDATE_USERNAME = "update User u set u.username = :username where u.id = :id";
    private static final String UPDATE_PHONE = "update User u set u.phone = :phone where u.id = :id";

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public Optional<User> findFetchedById(UUID uuid) {
        log.info("findFetchedById({})", uuid);
        return Optional.ofNullable(getSession().createQuery(FIND_BY_ID, User.class)
                .setParameter("id", uuid)
                .getSingleResultOrNull());
    }

    @Override
    public void updatePassword(UUID id, String password) {
        log.info("updatePassword({}, ...)", id);
        getSession().createMutationQuery(UPDATE_PASSWORD)
                .setParameter("password", password)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public void updateUsername(UUID id, String username) {
        log.info("updateUsername({}, {})", id, username);
        getSession().createMutationQuery(UPDATE_USERNAME)
                .setParameter("username", username)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public void updatePhone(UUID id, String phone) {
        log.info("updatePhone({}, {})", id, phone);
        getSession().createMutationQuery(UPDATE_PHONE)
                .setParameter("phone", phone)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        log.info("findByUsername({})", username);
        return Optional.ofNullable(
                getSession().createQuery(FIND_BY_USERNAME, User.class)
                        .setParameter("username", username)
                        .getSingleResultOrNull()
        );
    }

    @Override
    public boolean existsByUsername(String username) {
        log.info("existsByUsername({})", username);
        Integer result = getSession().createQuery(EXISTS_BY_USERNAME, Integer.class)
                .setParameter("username", username)
                .getSingleResultOrNull();
        return result != null;
    }

    @Override
    public boolean existsByPhone(String phone) {
        log.info("existsByPhone({})", phone);
        Integer result = getSession().createQuery(EXISTS_BY_PHONE, Integer.class)
                .setParameter("phone", phone)
                .getSingleResultOrNull();
        return result != null;
    }
}
