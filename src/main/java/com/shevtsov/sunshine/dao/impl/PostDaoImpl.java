package com.shevtsov.sunshine.dao.impl;

import com.shevtsov.sunshine.dao.PostDao;
import com.shevtsov.sunshine.dao.entities.ChatParticipation;
import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.dao.entities.User;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class PostDaoImpl extends AbstractDaoImpl<Post> implements PostDao {
    @Override
    public List<Post> getPostsFromUserWall(User wallOwner) {
        Class entityClass = Post.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Post> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Post> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Post> critCurrent = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder
                        .and(
                                criteriaBuilder.isNull(
                                        rootEntry.get("group")),
                                criteriaBuilder.equal(
                                        rootEntry.get("wall").get("id"),
                                        wallOwner.getId())
                        )
                );
        TypedQuery<Post> found = entityManager.createQuery(critCurrent);
        return found.getResultList();
    }
}
