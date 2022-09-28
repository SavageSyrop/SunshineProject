package com.shevtsov.sunshine.dao.impl;

import com.shevtsov.sunshine.dao.GroupMembershipDao;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import com.shevtsov.sunshine.common.GroupRole;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class GroupMembershipDaoImpl extends AbstractDaoImpl<GroupMembership> implements GroupMembershipDao {
    @Override
    public GroupMembership getGroupMembershipByUserAndGroupIds(Long userId, Long groupId) {
        Class entityClass = GroupMembership.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroupMembership> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<GroupMembership> rootEntry = criteriaQuery.from(entityClass);
        CriteriaQuery<GroupMembership> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("user").get("id"),
                                userId
                        ),
                        criteriaBuilder.equal(rootEntry.get("group").get("id"),
                                groupId
                        )
                        )
                );
        TypedQuery<GroupMembership> found = entityManager.createQuery(crit);
        if (found.getResultList().size() == 0) {
            return null;
        }
        return found.getSingleResult();
    }

    @Override
    public List<GroupMembership> getSubscribeRequests(Long groupId) {
        Class entityClass = GroupMembership.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroupMembership> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<GroupMembership> rootEntry = criteriaQuery.from(entityClass);
        CriteriaQuery<GroupMembership> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("groupRole"),
                                GroupRole.AWAITING_CHECK
                        ),
                        criteriaBuilder.equal(rootEntry.get("group").get("id"),
                                groupId
                        )
                        )
                );
        TypedQuery<GroupMembership> found = entityManager.createQuery(crit);
        return found.getResultList();
    }

    @Override
    public GroupMembership getGroupOwnerMembership(Long groupId) {
        Class entityClass = GroupMembership.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroupMembership> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<GroupMembership> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<GroupMembership> critCurrent = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(
                                rootEntry.get("group").get("id"),
                                groupId),
                        criteriaBuilder.equal(
                                rootEntry.get("groupRole"),
                                GroupRole.OWNER)
                        )
                );
        TypedQuery<GroupMembership> foundLikes = entityManager.createQuery(critCurrent);
        if (foundLikes.getResultList().size() == 0) {
            return null;
        }
        return foundLikes.getSingleResult();
    }

    @Override
    public List<GroupMembership> getGroupMemberships(Long userId) {
        Class entityClass = GroupMembership.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroupMembership> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<GroupMembership> rootEntry = criteriaQuery.from(entityClass);
        CriteriaQuery<GroupMembership> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.not(
                                criteriaBuilder.equal(rootEntry.get("groupRole"),
                                        GroupRole.AWAITING_CHECK
                                )
                        ),
                        criteriaBuilder.equal(
                                rootEntry.get("user").get("id"),
                                userId
                        )
                        )
                );
        TypedQuery<GroupMembership> found = entityManager.createQuery(crit);
        return found.getResultList();
    }
}
