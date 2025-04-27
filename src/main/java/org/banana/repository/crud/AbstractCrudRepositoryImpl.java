package org.banana.repository.crud;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractCrudRepositoryImpl<T, ID extends Serializable> implements CrudRepository<T, ID> {

    public static final String EXISTS_BY_ID = "SELECT 1 FROM %s e WHERE e.%s = :id";
    private final Class<T> entityClass;
    @Getter(AccessLevel.PROTECTED)
    @PersistenceContext
    private Session session;
    private String idAttributeName;

    protected AbstractCrudRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
        getIdFieldName(entityClass);
    }


    @Override
    public <S extends T> S save(S entity) {
        log.debug("save() in {}: {}", getClass().getSimpleName(), entity);
        return getSession().merge(entity);
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S e : entities) {
            result.add(save(e));
        }
        return result;
    }

    @Override
    public Optional<T> findById(ID id) {
        log.debug("findById({}) in {}", id, getClass().getSimpleName());
        return Optional.ofNullable(getSession().find(entityClass, id));
    }

    @Override
    public boolean existsById(ID id) {
        String query = String.format(
                EXISTS_BY_ID,
                entityClass.getSimpleName(), idAttributeName
        );
        Integer result = session.createQuery(query, Integer.class)
                .setParameter("id", id)
                .getSingleResultOrNull();
        return result != null && result == 1;
    }

    @Override
    public Iterable<T> findAll() {
        log.debug("findAll() in {}", getClass().getSimpleName());
        return getSession()
                .createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                .getResultList();
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        log.debug("findAllById() in {}", getClass().getSimpleName());
        List<T> list = new ArrayList<>();
        for (ID id : ids) {
            findById(id).ifPresent(list::add);
        }
        return list;
    }

    @Override
    public long count() {
        log.debug("count() in {}", getClass().getSimpleName());
        return getSession()
                .createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }

    @Override
    public void deleteById(ID id) {
        log.debug("deleteById({}) in {}", id, getClass().getSimpleName());
        findById(id).ifPresent(getSession()::remove);
    }

    @Override
    public void delete(T entity) {
        log.debug("delete({}) in {}", entity, getClass().getSimpleName());
        getSession().remove(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        log.debug("deleteAllById() in {}", getClass().getSimpleName());
        for (ID id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        log.debug("deleteAll(entities) in {}", getClass().getSimpleName());
        for (T e : entities) {
            delete(e);
        }
    }

    @Override
    public void deleteAll() {
        log.debug("deleteAll() in {}", getClass().getSimpleName());
        getSession()
                .createMutationQuery("DELETE FROM " + entityClass.getSimpleName())
                .executeUpdate();
    }

    private void getIdFieldName(Class<T> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                idAttributeName = field.getName();
                break;
            }
        }
        log.debug("Cached id property name for {}: {}", entityClass.getSimpleName(), idAttributeName);
    }
}
