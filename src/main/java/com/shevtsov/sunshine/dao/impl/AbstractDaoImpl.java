package com.shevtsov.sunshine.dao.impl;

import com.shevtsov.sunshine.dao.AbstractDao;
import com.shevtsov.sunshine.dao.entities.AbstractEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Transactional
public class AbstractDaoImpl<T extends AbstractEntity> implements AbstractDao<T> {

    @PersistenceContext
    @Getter(AccessLevel.PROTECTED)
    private EntityManager entityManager;

    private Class getEntityClass() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class) type.getActualTypeArguments()[0];
    }

    public T getById(Long id) {
        EntityManager entityManager = getEntityManager();
        T entity = (T) entityManager.find(getEntityClass(), id);
        if (entity == null) {
            throw new EntityNotFoundException(getEntityClass().getSimpleName() + " with id " + id + " is not found!");
        }
        return entity;
    }

    public List<T> getAll() {
        Class entityClass = getEntityClass();
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root rootEntry = criteriaQuery.from(entityClass);
        CriteriaQuery<T> all = criteriaQuery.select(rootEntry);
        List<T> entityList = entityManager.createQuery(all).getResultList();
        return entityList;
    }

    @Transactional(rollbackFor = {SQLException.class})
    public T create(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Transactional(rollbackFor = {SQLException.class})
    public void update(T entity) {
        entityManager.merge(entity);
    }

    @Transactional(rollbackFor = {SQLException.class})
    public void deleteById(Long id) {
        T entity = (T) entityManager.find(getEntityClass(), id);
        if (entity == null) {
            String[] splittedClassName = getEntityClass().getName().split("\\.");
            throw new EntityNotFoundException(splittedClassName[splittedClassName.length - 1] + " with id " + id + " is not found!");
        }
        entityManager.remove(entity);
    }
}
