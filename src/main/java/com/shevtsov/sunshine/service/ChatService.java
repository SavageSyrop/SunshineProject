package com.shevtsov.sunshine.service;

import com.shevtsov.sunshine.dao.entities.Chat;
import com.shevtsov.sunshine.dao.entities.ChatParticipation;
import com.shevtsov.sunshine.dao.entities.Message;
import com.shevtsov.sunshine.dao.entities.User;

import java.util.List;

public interface ChatService extends AbstractService<Chat> {
    Chat getPrivateChatBetweenCurrentAndRecipientUsers(Long id, Long recId);

    ChatParticipation createChatParticipation(ChatParticipation chatParticipation);

    ChatParticipation getUserParticipationInChatByChatId(Long chatId, Long userId);

    List<ChatParticipation> getChatParticipantsByChatId(Long chatId);

    void removeUserFromChat(Long chatId, Long currentUserId, Long userId);

    ChatParticipation getChatOwnerParticipation(Long currentUserId, Long chatId);

    Message addMessage(User currentUser, Long recipientUserId, Long chatId, String mes);

    Chat createPublicChat(String chatName, User currentUser, User userToAdd);

    Chat renameChat(User currentUser, Chat chat, String chatName);

    ChatParticipation addChatParticipantToChat(User currentUser, Long chatId, Long userToAddId);

    void setChatOwner(User currentUser, User newOwner, Chat chat);

    Message writeToSupport(User currentUser, String userRequest);

    List<Message> getSupportRequests();

    void answerSupportRequest(User user, String answer);

    Message getMessageById(Long messageId);
}

