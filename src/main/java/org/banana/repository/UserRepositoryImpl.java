package org.banana.repository;

import org.banana.entity.User;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class UserRepositoryImpl extends AbstractCrudRepositoryImpl<User, UUID> implements UserRepository {

    public UserRepositoryImpl() {
        super(User.class);
    }
}
