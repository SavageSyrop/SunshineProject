package com.shevtsov.sunshine.dao.impl;

import com.shevtsov.sunshine.dao.UserInfoDao;
import com.shevtsov.sunshine.dao.entities.UserInfo;
import com.shevtsov.sunshine.dao.entities.UserSearchInfo;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserInfoDaoImpl extends AbstractDaoImpl<UserInfo> implements UserInfoDao {

    @Override
    public List<UserInfo> getUserInfosByParams(UserSearchInfo userSearchInfo) {
        Class entityClass = UserInfo.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserInfo> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<UserInfo> rootEntry = criteriaQuery.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();

        if (userSearchInfo.getFirstName() != null) {
            predicates.add(criteriaBuilder.equal(rootEntry.get("firstName"), userSearchInfo.getFirstName()));
        }

        if (userSearchInfo.getLastName() != null) {
            predicates.add(criteriaBuilder.equal(rootEntry.get("lastName"), userSearchInfo.getLastName()));
        }

        if (userSearchInfo.getGenderType() != null) {
            predicates.add(criteriaBuilder.equal(rootEntry.get("genderType"), userSearchInfo.getGenderType()));
        }

        if (userSearchInfo.getFromDate() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(rootEntry.get("dateOfBirth"), userSearchInfo.getFromDate()));
        }

        if (userSearchInfo.getToDate() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(rootEntry.get("dateOfBirth"), userSearchInfo.getToDate()));
        }

        if (userSearchInfo.getCity() != null) {
            predicates.add(criteriaBuilder.equal(rootEntry.get("city"), userSearchInfo.getCity()));
        }

        CriteriaQuery<UserInfo> crit = criteriaQuery.select(rootEntry).where(predicates.toArray(new Predicate[0]));
        TypedQuery<UserInfo> foundInfos = entityManager.createQuery(crit);
        if (foundInfos.getResultList().size() == 0) {
            throw new EntityNotFoundException("No users have been found by those parameters!");
        }

        return foundInfos.getResultList();
    }
}
