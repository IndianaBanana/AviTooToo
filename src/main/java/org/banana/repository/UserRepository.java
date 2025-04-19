package org.banana.repository;

import org.banana.entity.User;
import org.banana.repository.crud.CrudRepository;

import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {

}
