package com.shevtsov.sunshine.service.impl;


import com.shevtsov.sunshine.dao.ChatDao;
import com.shevtsov.sunshine.dao.ChatParticipationDao;
import com.shevtsov.sunshine.dao.FriendRequestDao;
import com.shevtsov.sunshine.dao.MessageDao;
import com.shevtsov.sunshine.dao.UserDao;
import com.shevtsov.sunshine.dao.entities.Chat;
import com.shevtsov.sunshine.dao.entities.ChatParticipation;
import com.shevtsov.sunshine.dao.entities.Message;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.common.ChatType;
import com.shevtsov.sunshine.exceptions.AlreadyExistsException;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.InvalidActionException;
import com.shevtsov.sunshine.exceptions.SelfInteractionException;
import com.shevtsov.sunshine.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Component
public class ChatServiceImpl extends AbstractServiceImpl<Chat, ChatDao> implements ChatService {
    public ChatServiceImpl(ChatDao defaultDao) {
        super(defaultDao);
    }

    @Autowired
    private MailService mailService;

    @Autowired
    private ChatParticipationDao chatParticipationDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private FriendRequestDao friendRequestDao;

    /**
     * поиск приватного чата между двумя пользователями
     */
    @Override
    public Chat getPrivateChatBetweenCurrentAndRecipientUsers(Long id, Long recId) {
        return chatParticipationDao.getPrivateChatBetweenCurrentAndRecipientUsers(id, recId);

    }

    @Override
    public ChatParticipation createChatParticipation(ChatParticipation chatParticipation) {
        return chatParticipationDao.create(chatParticipation);
    }

    @Override
    public ChatParticipation getUserParticipationInChatByChatId(Long chatId, Long userId) {
        return chatParticipationDao.getUserParticipationInChatByChatId(chatId, userId);
    }

    @Override
    public List<ChatParticipation> getChatParticipantsByChatId(Long chatId) {
        return chatParticipationDao.getChatParticipantsByChatId(chatId);
    }


    /**
     * @param chatId         id чата, из которого нужно найти пользователя
     * @param currentUserId  id текущего пользователя
     * @param toDeleteUserId id удаляемого пользователя
     * @throws EntityNotFoundException когда удаляющий пользователь не состоит в чате
     * @throws EntityNotFoundException когда удаляемый пользователь не состоит в чате
     * @throws InvalidActionException  когда удаляющий пользователь не владелец чата
     * @throws InvalidActionException  когда владелец удаляет себя из чата, при том что есть другие участники
     */

    @Override
    public void removeUserFromChat(Long chatId, Long currentUserId, Long toDeleteUserId) {
        ChatParticipation currentUserChatParticipation = getUserParticipationInChatByChatId(chatId, currentUserId);

        if (currentUserChatParticipation == null) {
            throw new EntityNotFoundException("User with id " + currentUserId + " is not in chat with id " + chatId);
        }

        ChatParticipation removingUserChatParticipation = getUserParticipationInChatByChatId(chatId, toDeleteUserId);

        if (removingUserChatParticipation == null) {
            throw new EntityNotFoundException("User with id " + toDeleteUserId + " is not in chat with id " + chatId);
        }

        Chat chat = defaultDao.getById(chatId);

        if (chat.getOwner() == null) {
            throw new InvalidActionException("This chat is private, you can't remove user from it");
        }

        if (chat.getOwner().getId().equals(currentUserId) && currentUserId.equals(toDeleteUserId)) {
            if (getChatParticipantsByChatId(chatId).size() != 1) {
                throw new InvalidActionException("You can't delete owner of chat while there are still other users!");
            } else {
                chatParticipationDao.deleteById(removingUserChatParticipation.getId());
                defaultDao.deleteById(chatId);
            }
        } else {
            if (currentUserId.equals(toDeleteUserId)) {
                chatParticipationDao.deleteById(removingUserChatParticipation.getId());
                return;
            }
            if (!chat.getOwner().getId().equals(currentUserId)) {
                throw new AuthorizationErrorException("Only owner of chat can delete users from it!");
            }
            chatParticipationDao.deleteById(removingUserChatParticipation.getId());
        }
    }

    /**
     * @param currentUserId id текущего пользователя
     * @param chatId        id чата, владельца которого нужно получить
     * @return сущность членства владельца чата
     * @throws AuthorizationErrorException если текущий пользователь не состоит в чате
     * @throws InvalidActionException      если чат приватный
     */

    @Override
    public ChatParticipation getChatOwnerParticipation(Long currentUserId, Long chatId) {
        ChatParticipation currentUserParticipation = getUserParticipationInChatByChatId(chatId, currentUserId);
        if (currentUserParticipation == null) {
            throw new AuthorizationErrorException("You are not part of chat with id " + chatId);
        }
        if (currentUserParticipation.getChat().getChatType() == ChatType.PRIVATE) {
            throw new InvalidActionException("Private chats don't have owner!");
        }
        return getUserParticipationInChatByChatId(chatId, currentUserParticipation.getChat().getOwner().getId());
    }

    /**
     * добавляет сообщение в приватный чат (если задан параметр recipientUserId) или групповой (если задан chatId)
     *
     * @param currentUser     id текущего пользователя
     * @param recipientUserId id получателя сообщения (опционально)
     * @param chatId          id чата (опционально)
     * @param mes             текст сообщения
     * @return созданное сообщение группового или личного чата
     * @throws AuthorizationErrorException если не друг пишет пользователю с закрытым профилем
     * @throws AuthorizationErrorException если сообщение отправлено в чат, в котором не состоит текущий пользователь
     */

    @Override
    public Message addMessage(User currentUser, Long recipientUserId, Long chatId, String mes) {
        Chat chat;
        if (recipientUserId != null) {          // пишем лично
            User recipientUser = userDao.getById(recipientUserId);

            if (recipientUser.getId().equals(currentUser.getId())) {
                throw new SelfInteractionException("You can't create chat with yourself");
            }

            if (!recipientUser.isOpenUser() && !friendRequestDao.isFriendOf(currentUser.getId(), recipientUser.getId())) {
                throw new AuthorizationErrorException("Only friends can write users with closed profile!");
            }

            chat = getPrivateChatBetweenCurrentAndRecipientUsers(currentUser.getId(), recipientUser.getId());
            if (chat == null) {
                chat = new Chat(ChatType.PRIVATE, currentUser.getUsername() + " / " + recipientUser.getUsername(), null);
                defaultDao.create(chat);
                ChatParticipation chatParticipationCur = new ChatParticipation(chat, currentUser);
                ChatParticipation chatParticipationRec = new ChatParticipation(chat, recipientUser);
                createChatParticipation(chatParticipationCur);
                createChatParticipation(chatParticipationRec);
            }
            Message message = new Message(currentUser, mes, chat);
            message = messageDao.create(message);
            return message;
        } else {        // пишем группе
            chat = defaultDao.getById(chatId);
            if (getUserParticipationInChatByChatId(chatId, currentUser.getId()) == null) {
                throw new AuthorizationErrorException("You have no chat participation in this chat. To write here you need to be added by other user");
            }
            Message message = new Message(currentUser, mes, chat);
            message = messageDao.create(message);
            return message;
        }
    }


    /**
     * @param chatName    имя чата
     * @param currentUser текущий пользователь
     * @param userToAdd   пользователь, который добавляет в групповой чат при создании
     * @return созданный груповой чат
     * @throws SelfInteractionException когда создается чат с самим собой
     * @throws InvalidActionException   когда чат создается с пользователем, не являющимся другом
     */

    @Override
    public Chat createPublicChat(String chatName, User currentUser, User userToAdd) {
        if (currentUser.getId().equals(userToAdd.getId())) {
            throw new SelfInteractionException("You can't create chat with yourself");
        }
        if (!friendRequestDao.isFriendOf(currentUser.getId(), userToAdd.getId())) {
            throw new InvalidActionException("You can't add this user because he is not your friend!");
        }
        Chat chat = new Chat(ChatType.PUBLIC, chatName, currentUser);
        defaultDao.create(chat);
        ChatParticipation currentUserPart = new ChatParticipation(chat, currentUser);
        ChatParticipation userToAddPart = new ChatParticipation(chat, userToAdd);
        createChatParticipation(currentUserPart);
        createChatParticipation(userToAddPart);
        return chat;
    }

    /**
     * @param currentUser текущий пользователь
     * @param chat        чат, который переименовывают
     * @param chatName    новое имя чата
     * @return переименованный чат
     * @throws AuthorizationErrorException текущий пользователь не участник чата
     */

    @Override
    public Chat renameChat(User currentUser, Chat chat, String chatName) {
        if (getUserParticipationInChatByChatId(chat.getId(), currentUser.getId()) != null) {
            chat.setName(chatName);
        } else {
            throw new AuthorizationErrorException("You have no chat participation in this chat. To write here you need to be added by other user");
        }
        return chat;
    }

    /**
     * @param currentUser текущий пользователь
     * @param chatId      id чата, куда добавляется новый участник
     * @param userToAddId id пользователя, которого нужно добавить в чат
     * @return членство добавленного пользователя
     * @throws InvalidActionException      если тип чата не групповой
     * @throws AuthorizationErrorException если текущий пользователь не участник чата
     * @throws AlreadyExistsException      если добавляемый пользователь уже участник чата
     * @throws InvalidActionException      если добавляемый пользователь не друг текущего
     */

    @Override
    public ChatParticipation addChatParticipantToChat(User currentUser, Long chatId, Long userToAddId) {
        User userToAdd = userDao.getById(userToAddId);

        Chat chat = defaultDao.getById(chatId);

        if (getUserParticipationInChatByChatId(chatId, currentUser.getId()) == null) {
            throw new AuthorizationErrorException("Current user is not a participant of chat with id " + chatId);
        }

        if (chat.getChatType() == ChatType.PRIVATE) {
            throw new InvalidActionException("You can add users only to PUBLIC chats");
        }

        if (getUserParticipationInChatByChatId(chatId, userToAddId) != null) {
            throw new AlreadyExistsException("User already is a member of this chat");
        }

        if (!friendRequestDao.isFriendOf(currentUser.getId(), userToAdd.getId())) {
            throw new InvalidActionException("You can't add this user because he is not your friend!");
        }

        ChatParticipation chatParticipation = new ChatParticipation(chat, userToAdd);
        chatParticipation = createChatParticipation(chatParticipation);
        return chatParticipation;
    }


    @Override
    public void setChatOwner(User currentUser, User newOwner, Chat chat) {

        if (getUserParticipationInChatByChatId(chat.getId(), currentUser.getId()) == null) {
            throw new EntityNotFoundException("Current user is not member of this chat!");
        }

        if (chat.getChatType() == ChatType.PRIVATE) {
            throw new InvalidActionException("Private chats don't have owners!");
        }

        if ((getUserParticipationInChatByChatId(chat.getId(), newOwner.getId())) == null) {
            throw new EntityNotFoundException("Chosen to be new owner user is not member of this chat!");
        }

        if (chat.getOwner().getId().equals(currentUser.getId())) {
            chat.setOwner(newOwner);
        } else {
            throw new AuthorizationErrorException("Current user is not owner of this chat!");
        }
    }

    /**
     * @param currentUser текущий пользователь
     * @param userRequest сообщение для поддержки
     * @return созданное сообщение
     * @throws InvalidActionException если запрос в поддержку уже существует
     */
    @Override
    public Message writeToSupport(User currentUser, String userRequest) {
        if (messageDao.getUserSupportRequest(currentUser) != null) {
            throw new InvalidActionException("You have already sent a support request, our admins will answer it shortly");
        }
        Message message = new Message(currentUser, userRequest, null);
        return messageDao.create(message);
    }

    @Override
    public List<Message> getSupportRequests() {
        return messageDao.getSupportRequests();
    }

    @Override
    public void answerSupportRequest(User user, String answer) {
        Message question = messageDao.getUserSupportRequest(user);
        if (question == null) {
            throw new EntityNotFoundException("This user haven't sent support request!");
        }
        mailService.sendAnswerEmail(user, question.getText(), answer);
        messageDao.deleteById(question.getId());
    }

    @Override
    public Message getMessageById(Long messageId) {
        return messageDao.getById(messageId);
    }
}
