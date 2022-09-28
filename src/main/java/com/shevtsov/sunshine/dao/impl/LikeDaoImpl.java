package com.shevtsov.sunshine.dao.impl;

import com.shevtsov.sunshine.dao.LikeDao;
import com.shevtsov.sunshine.dao.entities.Like;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Component
public class LikeDaoImpl extends AbstractDaoImpl<Like> implements LikeDao {
    @Override
    public Like getCommentLikeByUserId(Long commentId, Long userId) {
        Class entityClass = Like.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Like> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Like> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Like> critCurrent = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(
                                rootEntry.get("comment").get("id"),
                                commentId),
                        criteriaBuilder.equal(
                                rootEntry.get("user").get("id"),
                                userId)
                        )
                );
        TypedQuery<Like> foundLikes = entityManager.createQuery(critCurrent);
        if (foundLikes.getResultList().size() == 0) {
            return null;
        }
        return foundLikes.getSingleResult();

    }

    @Override
    public Like getPostLikeByUserId(Long postId, Long userId) {
        Class entityClass = Like.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Like> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Like> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Like> critCurrent = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(
                                rootEntry.get("post").get("id"),
                                postId),
                        criteriaBuilder.equal(
                                rootEntry.get("user").get("id"),
                                userId)
                        )
                );
        TypedQuery<Like> foundLikes = entityManager.createQuery(critCurrent);
        if (foundLikes.getResultList().size() == 0) {
            return null;
        }
        return foundLikes.getSingleResult();
    }
}
