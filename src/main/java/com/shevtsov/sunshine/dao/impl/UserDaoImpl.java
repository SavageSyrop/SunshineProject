package com.shevtsov.sunshine.dao.impl;

import com.shevtsov.sunshine.dao.UserDao;
import com.shevtsov.sunshine.dao.entities.User;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Component
public class UserDaoImpl extends AbstractDaoImpl<User> implements UserDao {
    @Override
    public User getByUsername(String username) {
        Class entityClass = User.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<User> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<User> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.equal(
                        rootEntry.get("userInfo").get("username"),
                        username)
                );
        TypedQuery<User> foundUsers = entityManager.createQuery(crit);
        if (foundUsers.getResultList().size() == 0) {
            return null;
        }
        return foundUsers.getSingleResult();
    }

    @Override
    public User getUserByActivationCode(String activationCode) {
        Class entityClass = User.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<User> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<User> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.equal(
                        rootEntry.get("activationCode"),
                        activationCode)
                );
        TypedQuery<User> foundUsers = entityManager.createQuery(crit);
        if (foundUsers.getResultList().size() == 0) {
            return null;
        }
        return foundUsers.getSingleResult();
    }

    @Override
    public User getUserByRestorePasswordCode(String restoreCode) {
        Class entityClass = User.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<User> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<User> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.equal(
                        rootEntry.get("restorePasswordCode"),
                        restoreCode)
                );
        TypedQuery<User> foundUsers = entityManager.createQuery(crit);
        if (foundUsers.getResultList().size() == 0) {
            return null;
        }
        return foundUsers.getSingleResult();
    }
}
