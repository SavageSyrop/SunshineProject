package com.shevtsov.sunshine.dao.impl;

import com.shevtsov.sunshine.dao.FriendRequestDao;
import com.shevtsov.sunshine.dao.entities.FriendRequest;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class FriendRequestDaoImpl extends AbstractDaoImpl<FriendRequest> implements FriendRequestDao {
    @Override
    public List<FriendRequest> getUserReceivedFriendRequests(Long id) {
        Class entityClass = FriendRequest.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FriendRequest> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<FriendRequest> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<FriendRequest> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(criteriaBuilder.equal(
                        rootEntry.get("recipientUser").get("id"),
                        id
                        ),
                        criteriaBuilder.isFalse(rootEntry.get("isAccepted")
                        )
                ));
        TypedQuery<FriendRequest> found = entityManager.createQuery(crit);
        return found.getResultList();
    }

    @Override
    public FriendRequest getFriendRequestByUserIds(Long currentUserId, Long recipientId) {
        Class entityClass = FriendRequest.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FriendRequest> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<FriendRequest> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<FriendRequest> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("senderUser").get("id"),
                                currentUserId
                        ),
                        criteriaBuilder.equal(rootEntry.get("recipientUser").get("id"),
                                recipientId
                        )
                        )
                );
        TypedQuery<FriendRequest> found = entityManager.createQuery(crit);
        if (found.getResultList().size() == 0) {
            return null;
        }
        return found.getSingleResult();
    }

    @Override
    public List<FriendRequest> getUserFriends(Long userId) {
        Class entityClass = FriendRequest.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FriendRequest> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<FriendRequest> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<FriendRequest> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("senderUser").get("id"),
                                userId
                        ),
                        criteriaBuilder.isTrue(rootEntry.get("isAccepted")
                        )
                        )
                );
        TypedQuery<FriendRequest> found = entityManager.createQuery(crit);

        return found.getResultList();
    }

    @Override
    public List<FriendRequest> getUserSentFriendRequests(Long userId) {
        Class entityClass = FriendRequest.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FriendRequest> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<FriendRequest> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<FriendRequest> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("senderUser").get("id"),
                                userId
                        ),
                        criteriaBuilder.isFalse(rootEntry.get("isAccepted")
                        )
                        )
                );
        TypedQuery<FriendRequest> found = entityManager.createQuery(crit);

        return found.getResultList();
    }

    @Override
    public Boolean isFriendOf(Long currentUserId, Long recipientUserId) {
        Class entityClass = FriendRequest.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FriendRequest> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<FriendRequest> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<FriendRequest> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("recipientUser").get("id"),
                                recipientUserId
                        ),
                        criteriaBuilder.isTrue(rootEntry.get("isAccepted")
                        ),
                        criteriaBuilder.equal(rootEntry.get("senderUser").get("id"),
                                currentUserId
                        )
                        )
                );
        TypedQuery<FriendRequest> found = entityManager.createQuery(crit);

        if (found.getResultList().size() == 0) {
            return false;
        } else {
            return true;
        }
    }
}