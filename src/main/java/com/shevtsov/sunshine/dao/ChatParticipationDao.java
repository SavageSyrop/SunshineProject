package com.shevtsov.sunshine.dao;

import com.shevtsov.sunshine.dao.entities.Chat;
import com.shevtsov.sunshine.dao.entities.ChatParticipation;

import java.util.List;

public interface ChatParticipationDao extends AbstractDao<ChatParticipation> {
    Chat getPrivateChatBetweenCurrentAndRecipientUsers(Long id, Long recId);

    ChatParticipation getUserParticipationInChatByChatId(Long chatId, Long userId);

    List<ChatParticipation> getChatParticipantsByChatId(Long chatId);
}
