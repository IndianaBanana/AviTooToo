package org.banana.repository;

import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractCrudRepositoryImpl<T, ID extends Serializable> implements CrudRepository<T, ID> {

    private final Class<T> entityClass;

    @PersistenceContext
    private Session session;

    protected AbstractCrudRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Session getSession() {
        return session;
    }

    @Override
    public Optional<T> findById(ID id) {
        log.debug("entering `findById` method in {}", this.getClass().getSimpleName());
        return Optional.ofNullable(getSession().find(entityClass, id));
    }

    @Override
    public List<T> findAll() {
        log.debug("entering `findAll` method in {}", this.getClass().getSimpleName());
        return getSession().createQuery(getHqlFindAllQuery(), entityClass).getResultList();
    }

    @Override
    public T save(T t) {
        log.debug("entering `save` method in {}", this.getClass().getSimpleName());
        getSession().merge(t);
        return t;
    }

    @Override
    public void add(T t) {
        log.debug("entering `add` method in {}", this.getClass().getSimpleName());
        getSession().persist(t);
    }

    @Override
    public void delete(T t) {
        log.debug("entering `delete` method in {}", this.getClass().getSimpleName());
        getSession().remove(t);
    }

    @Override
    public void deleteById(ID id) {
        log.debug("entering `deleteById` method in {}", this.getClass().getSimpleName());
        Session session = getSession();
        session.remove(session.find(entityClass, id));
    }

    @Override
    public boolean existsById(ID id) {
        log.debug("entering `existsById` method in {}", this.getClass().getSimpleName());
        Query<Integer> query = getSession().createQuery(
                getHqlExistsByIdQuery(),
                Integer.class
        );
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }

    protected String getHqlFindAllQuery() {
        return "from " + entityClass.getSimpleName();
    }

    protected String getHqlExistsByIdQuery() {
        return "SELECT 1 FROM " + entityClass.getSimpleName() + " e WHERE e.id = :id";
    }
}
