package com.shevtsov.sunshine.dao.impl;

import com.shevtsov.sunshine.dao.ChatParticipationDao;
import com.shevtsov.sunshine.dao.entities.Chat;
import com.shevtsov.sunshine.dao.entities.ChatParticipation;
import com.shevtsov.sunshine.common.ChatType;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.List;

@Component
public class ChatParticipationDaoImpl extends AbstractDaoImpl<ChatParticipation> implements ChatParticipationDao {

    @Override
    public Chat getPrivateChatBetweenCurrentAndRecipientUsers(Long id, Long recId) {
        Class entityClass = ChatParticipation.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChatParticipation> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<ChatParticipation> rootEntry = criteriaQuery.from(entityClass);

        Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
        Root<ChatParticipation> subRoot = subquery.from(entityClass);
        subquery.select(subRoot.get("chat").get("id")).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(subRoot.get("user"), recId),
                        criteriaBuilder.equal(subRoot.get("chat").get("chatType"), ChatType.PRIVATE)
                )
        );

        criteriaQuery.select(rootEntry).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(rootEntry.get("user").get("id"), id),
                        criteriaBuilder.in(rootEntry.get("chat").get("id")).value(subquery)
                )
        );

        TypedQuery<ChatParticipation> foundChatParticipations = entityManager.createQuery(criteriaQuery);
        if (foundChatParticipations.getResultList().size() == 0) {
            return null;
        }
        return foundChatParticipations.getSingleResult().getChat();
    }


    @Override
    public List<ChatParticipation> getChatParticipantsByChatId(Long chatId) {
        Class entityClass = ChatParticipation.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChatParticipation> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<ChatParticipation> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<ChatParticipation> critCurrent = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder.equal(
                        rootEntry.get("chat").get("id"),
                        chatId));
        TypedQuery<ChatParticipation> foundChatParticipation = entityManager.createQuery(critCurrent);
        if (foundChatParticipation.getResultList().size() == 0) {
            return null;
        }
        return foundChatParticipation.getResultList();
    }

    @Override
    public ChatParticipation getUserParticipationInChatByChatId(Long chatId, Long userId) {
        Class entityClass = ChatParticipation.class;
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChatParticipation> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<ChatParticipation> rootEntry = criteriaQuery.from(entityClass);

        CriteriaQuery<ChatParticipation> critCurrent = criteriaQuery.select(rootEntry)
                .where(criteriaBuilder
                        .and(
                                criteriaBuilder.equal(
                                        rootEntry.get("chat").get("id"),
                                        chatId),
                                criteriaBuilder.equal(
                                        rootEntry.get("user").get("id"),
                                        userId)
                        )
                );
        TypedQuery<ChatParticipation> foundChatParticipation = entityManager.createQuery(critCurrent);
        if (foundChatParticipation.getResultList().size() == 0) {
            return null;
        }
        return foundChatParticipation.getSingleResult();
    }
}
