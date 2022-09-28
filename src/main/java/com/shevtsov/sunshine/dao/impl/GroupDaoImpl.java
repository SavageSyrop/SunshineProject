package com.shevtsov.sunshine.dao.impl;

import com.shevtsov.sunshine.dao.GroupDao;
import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.Post;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class GroupDaoImpl extends AbstractDaoImpl<Group> implements GroupDao {
    @Override
    public List<Post> getGroupPublishedPosts(Long groupId) {
        Class entityClass = Post.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Post> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Post> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Post> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("group").get("id"),
                                groupId),
                        criteriaBuilder.isTrue(rootEntry.get("isPublished"))
                        )
                );
        TypedQuery<Post> found = entityManager.createQuery(crit);
        return found.getResultList();
    }

    @Override
    public List<Post> getGroupOfferedPosts(Long groupId) {
        Class entityClass = Post.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Post> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Post> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Post> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("group").get("id"),
                                groupId),
                        criteriaBuilder.isFalse(rootEntry.get("isPublished"))
                        )
                );
        TypedQuery<Post> found = entityManager.createQuery(crit);
        return found.getResultList();
    }
}
