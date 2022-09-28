package com.shevtsov.sunshine.dao.impl;

import com.shevtsov.sunshine.dao.MessageDao;
import com.shevtsov.sunshine.dao.entities.Message;
import com.shevtsov.sunshine.dao.entities.User;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class MessageDaoImpl extends AbstractDaoImpl<Message> implements MessageDao {
    @Override
    public Message getUserSupportRequest(User currentUser) {
        Class entityClass = Message.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Message> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Message> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Message> crit = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("sender").get("id"),
                                currentUser.getId()),
                        criteriaBuilder.isNull(rootEntry.get("chat"))
                        )
                );
        TypedQuery<Message> found = entityManager.createQuery(crit);
        if (found.getResultList().size() == 0) {
            return null;
        }
        return found.getSingleResult();
    }

    @Override
    public List<Message> getSupportRequests() {
        Class entityClass = Message.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Message> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<Message> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<Message> crit = criteriaQuery.select(rootEntry).where(criteriaBuilder.isNull(rootEntry.get("chat")));
        TypedQuery<Message> found = entityManager.createQuery(crit);
        return found.getResultList();
    }
}
