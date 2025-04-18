package org.banana.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    T save(T t);

    void delete(T t);

    void add(T t);

    void deleteById(ID id);

    boolean existsById(ID id);
}
