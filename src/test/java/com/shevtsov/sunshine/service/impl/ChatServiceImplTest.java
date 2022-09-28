package com.shevtsov.sunshine.service.impl;

import com.shevtsov.sunshine.dao.entities.Chat;
import com.shevtsov.sunshine.dao.entities.Message;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.common.ChatType;
import com.shevtsov.sunshine.exceptions.AlreadyExistsException;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.InvalidActionException;
import com.shevtsov.sunshine.exceptions.SelfInteractionException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChatServiceImplTest extends AbstractServiceImplTest {
    @InjectMocks
    private ChatServiceImpl chatService;


    @Test
    @Order(1)
    void addMessage_WhenRecipientDoesntExist_ThenThrowEntityNotFound() {
        User currentUser = getUserDao().getById(1L);
        Long recipientId = 54L;

        String message = "Welcome to South Park!";
        assertThrows(EntityNotFoundException.class, () -> chatService.addMessage(currentUser, recipientId, null, message));
    }

    @Test
    @Order(2)
    void addMessage_WhenChatDoesntExist_ThenThrowEntityNotFound() {
        User currentUser = getUserDao().getById(1L);
        Long recipientId = null;
        Long chatId = 54L;
        String message = "Welcome to South Park!";
        assertThrows(EntityNotFoundException.class, () -> chatService.addMessage(currentUser, recipientId, chatId, message));
    }

    @Test
    @Order(3)
    void addMessage_WhenWritingYourself_ThenThrowSelfInteractionException() {
        User currentUser = getUserDao().getById(1L);
        Long recipientId = 1L;
        String message = "Welcome to South Park!";
        assertThrows(SelfInteractionException.class, () -> chatService.addMessage(currentUser, recipientId, null, message));
    }

    @Test
    @Order(4)
    void addMessage_WhenDataCorrect_ThenSuccess() {
        User currentUser = getUserDao().getById(1L);
        Long recipientId = 2L;
        String message = "Welcome to South Park!";
        Message addedMessage = chatService.addMessage(currentUser, recipientId, null, message);
        assertEquals(message, addedMessage.getText());
        assertEquals(currentUser.getUsername(), addedMessage.getSenderName());
    }

    @Test
    @Order(5)
    void addMessage_WhenNotAFriendOfUserWithPrivateAccount_ThenThrowAuthorizationErrorException() {
        User currentUser = getUserDao().getById(1L);
        Long recipientId = 4L;
        String message = "Welcome to South Park!";
        assertThrows(AuthorizationErrorException.class, () -> chatService.addMessage(currentUser, recipientId, null, message));
    }

    @Test
    @Order(6)
    void addMessage_WhenAFriendOfUserWithPrivateAccount_ThenSuccess() {
        User currentUser = getUserDao().getById(1L);
        Long recipientId = 4L;
        User recipient = getUserDao().getById(4L);
        createFriendship(currentUser, recipient);
        String message = "Welcome to South Park!";
        assertNotNull(chatService.addMessage(currentUser, recipientId, null, message));
        assertEquals(chatMap.get(2L).getChatType(), ChatType.PRIVATE);
    }

    @Test
    @Order(7)
    void addMessage_WhenChatIdOfPrivateChat_ThenThrowAuthorizationErrorException() {
        User currentUser = getUserDao().getById(3L);
        Long chatId = 1L;
        String message = "Hello, neighbor!";
        assertThrows(AuthorizationErrorException.class, () -> chatService.addMessage(currentUser, null, chatId, message));
    }

    @Test
    @Order(8)
    void createPublicChat_WhenAddingYourself_ThenThrowSelfInteractionException() {
        User user = getUserDao().getById(3L);
        User userToAdd = getUserDao().getById(3L);
        assertThrows(SelfInteractionException.class, () -> chatService.createPublicChat("Naggers", user, userToAdd));
    }

    @Test
    @Order(9)
    void createPublicChat_WhenUserToAddIsNotAFriend_ThenThrowInvalidActionException() {
        User user = getUserDao().getById(3L);
        User userToAdd = getUserDao().getById(4L);
        assertThrows(InvalidActionException.class, () -> chatService.createPublicChat("Naggers", user, userToAdd));
    }

    @Test
    @Order(10)
    void createPublicChat_WhenDataCorrect_ThenSuccess() {
        User user = getUserDao().getById(3L);
        User userToAdd = getUserDao().getById(4L);
        createFriendship(user, userToAdd);             // требуется дружба между пользователя чтобы можно было создать чат с ним или добавить в группу
        assertNotNull(chatService.createPublicChat("Naggers", user, userToAdd));
    }

    @Test
    @Order(11)
    void  addMessage_WhenPublicChatExists_ThenSuccess(){
        User user = getUserDao().getById(3L);
        Chat chat = chatMap.get((long)chatMap.size());
        assertNotNull(chatService.addMessage(user, null, chat.getId(), "Text"));
    }

    @Test
    @Order(12)
    void  renameChat_WhenCurrentUserIsOwner_Success(){
        User user = getUserDao().getById(3L);
        Chat chat = chatMap.get((long)chatMap.size());
        assertNotNull(chatService.renameChat(user, chat, "New Chat Name"));
    }

    @Test
    @Order(13)
    void  renameChat_WhenCurrentUserIsNotOwner_ThenAuthorizationErrorException(){
        User user = getUserDao().getById(2L);
        Chat chat = chatMap.get((long)chatMap.size());
        assertThrows(AuthorizationErrorException.class, () -> chatService.renameChat(user, chat, "New Chat Name"));
    }


    @Test
    @Order(14)
    void addMessage_WhenUserNotParticipantInChat_ThenThrowAuthorizationErrorException() {
        User currentUser = getUserDao().getById(6L);
        Chat chat = getChatDao().getById(3L);
        assertEquals(chat.getChatType(), ChatType.PUBLIC);
        String message = "KENDRICK CALMAR!";
        assertThrows(AuthorizationErrorException.class, () -> chatService.addMessage(currentUser, null, chat.getId(), message));
    }


    @Test
    @Order(15)
    void getChatOwnerParticipation_WhenUserNotPartOfChat_ThenThrowAuthorizationErrorException() {
        User user = getUserDao().getById(1L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(AuthorizationErrorException.class, () -> chatService.getChatOwnerParticipation(user.getId(), chat.getId()));
    }

    @Test
    @Order(16)
    void getChatOwnerParticipation_WhenPrivateChatOwner_ThenThrowInvalidActionException() {

        User user = getUserDao().getById(1L);
        Chat chat = getChatDao().getById(2L);
        assertThrows(InvalidActionException.class, () -> chatService.getChatOwnerParticipation(user.getId(), chat.getId()));
    }

    @Test
    @Order(17)
    void getChatOwnerParticipation_WhenCorrectData_ThenSuccess() {
        User user = getUserDao().getById(3L);
        Chat chat = getChatDao().getById(3L);
        assertNotNull(chatService.getChatOwnerParticipation(user.getId(), chat.getId()));
    }


    @Test
    @Order(18)
    void addChatParticipantToChat_WhenAddingToPrivateChat_ThenThrowInvalidActionException() {
        User currentUser = getUserDao().getById(1L);
        Chat chat = getChatDao().getById(2L);
        User userToAdd = getUserDao().getById(3L);
        assertThrows(InvalidActionException.class, () -> chatService.addChatParticipantToChat(currentUser, chat.getId(), userToAdd.getId()));
    }

    @Test
    @Order(19)
    void addChatParticipantToChat_WhenAddingNotFriend_ThenInvalidActionException() {
        User currentUser = getUserDao().getById(3L);
        User userToAdd = getUserDao().getById(5L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(InvalidActionException.class, () -> chatService.addChatParticipantToChat(currentUser, chat.getId(), userToAdd.getId()));
    }


    @Test
    @Order(20)
    void addChatParticipantToChat_WhenUserAlreadyMemberInChat_ThenThrowsAlreadyExistsException() {
        User currentUser = getUserDao().getById(4L);
        User userToAdd = getUserDao().getById(3L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(AlreadyExistsException.class, () -> chatService.addChatParticipantToChat(currentUser, chat.getId(), userToAdd.getId()));
    }

    @Test
    @Order(21)
    void addChatParticipantToChat_WhenCurrentUserNotMemberOfChat_ThenAuthorizationErrorException() {
        User currentUser = getUserDao().getById(6L);
        User userToAdd = getUserDao().getById(1L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(AuthorizationErrorException.class, () -> chatService.addChatParticipantToChat(currentUser, chat.getId(), userToAdd.getId()));
    }

    @Test
    @Order(22)
    void addChatParticipantToChat_WhenCorrectData_ThenSuccess() {
        User currentUser = getUserDao().getById(3L);
        User userToAdd = getUserDao().getById(1L);                                                      // в чате состоят id: 1, 3, 4 . Owner - 3
        createFriendship(currentUser, userToAdd);
        Chat chat = getChatDao().getById(3L);
        assertNotNull(chatService.addChatParticipantToChat(currentUser, chat.getId(), userToAdd.getId()));
        assertEquals(chatParticipationMap.get((long) chatParticipationMap.size()).getUser().getUsername(), userToAdd.getUsername());
    }


    @Test
    @Order(23)
    void removeUserFromChat_WhenCurrentUserIsNotMember_ThenThrowEntityNotFoundException() {
        User currentUser = getUserDao().getById(5L);
        User removeUser = getUserDao().getById(3L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(EntityNotFoundException.class, () -> chatService.removeUserFromChat(chat.getId(), currentUser.getId(), removeUser.getId()));

    }

    @Test
    @Order(24)
    void removeUserFromChat_WhenRemovingNotMember_ThenThrowEntityNotFoundException() {
        User currentUser = getUserDao().getById(3L);
        User removeUser = getUserDao().getById(5L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(EntityNotFoundException.class, () -> chatService.removeUserFromChat(chat.getId(), currentUser.getId(), removeUser.getId()));

    }

    @Test
    @Order(25)
    void removeUserFromChat_WhenRemovingFromPrivateChat_ThenThrowsInvalidActionException() {
        User currentUser = getUserDao().getById(1L);
        User removeUser = getUserDao().getById(4L);
        Chat chat = getChatDao().getById(2L);
        assertThrows(InvalidActionException.class, () -> chatService.removeUserFromChat(chat.getId(), currentUser.getId(), removeUser.getId()));
    }

    @Test
    @Order(26)
    void removeUserFromChat_WhenNotOwnerIsCurrentUser_ThenThrowsAuthorizationErrorException() {
        User currentUser = getUserDao().getById(1L);
        User removeUser = getUserDao().getById(4L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(AuthorizationErrorException.class, () -> chatService.removeUserFromChat(chat.getId(), currentUser.getId(), removeUser.getId()));

    }

    @Test
    @Order(27)
    void removeUserFromChat_WhenRemovingOwnerWhileOthersMembersExist_ThenThrowInvalidActionException() {
        User currentUser = getUserDao().getById(3L);
        User removeUser = getUserDao().getById(3L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(InvalidActionException.class, () -> chatService.removeUserFromChat(chat.getId(), currentUser.getId(), removeUser.getId()));
    }

    @Test
    @Order(28)
    void setChatOwner_WhenChatIsPrivate_ThenThrowInvalidActionException() {
        User currentUser = getUserDao().getById(1L);
        User newOwner = getUserDao().getById(2L);
        Chat chat = getChatDao().getById(2L);
        assertThrows(InvalidActionException.class, () -> chatService.setChatOwner(currentUser, newOwner, chat));
    }

    @Test
    @Order(29)
    void setChatOwner_WhenCurrentUserNotMemberInChat_ThenThrowEntityNotFoundException() {
        User currentUser = getUserDao().getById(6L);
        User newOwner = getUserDao().getById(2L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(EntityNotFoundException.class, () -> chatService.setChatOwner(currentUser, newOwner, chat));
    }

    @Test
    @Order(30)
    void setChatOwner_WhenNewOwnerUserNotMemberInChat_ThenThrowEntityNotFoundException() {
        User currentUser = getUserDao().getById(3L);
        User newOwner = getUserDao().getById(6L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(EntityNotFoundException.class, () -> chatService.setChatOwner(currentUser, newOwner, chat));
    }

    @Test
    @Order(31)
    void setChatOwner_WhenCurrentUserNotOwnerOfChat_ThenThrowAuthorizationErrorException() {
        User currentUser = getUserDao().getById(1L);
        User newOwner = getUserDao().getById(3L);
        Chat chat = getChatDao().getById(3L);
        assertThrows(AuthorizationErrorException.class, () -> chatService.setChatOwner(currentUser, newOwner, chat));
    }


    @Test
    @Order(32)
    void setChatOwner_WhenDataCorrect_ThenSuccess() {
        User currentUser = getUserDao().getById(3L);
        User newOwner = getUserDao().getById(1L);
        Chat chat = getChatDao().getById(3L);
        chatService.setChatOwner(currentUser, newOwner, chat);
        assertEquals(chat.getOwner().getId(), newOwner.getId());
    }

    @Test
    @Order(33)
    void removeUserFromChat_WhenDeletingMemberCorrectData_ThenSuccess() {
        User currentUser = getUserDao().getById(1L);
        User removeUser = getUserDao().getById(4L);
        Chat chat = getChatDao().getById(3L);

        chatService.removeUserFromChat(chat.getId(), currentUser.getId(), removeUser.getId());
        assertNull(getChatParticipationDao().getUserParticipationInChatByChatId(chat.getId(), removeUser.getId()));
        chatService.removeUserFromChat(chat.getId(), currentUser.getId(), 3L);      // удаление-подготовка к следующему тесту
    }


    @Test
    @Order(34)
    void removeUserFromChat_WhenDeletingOwnerCorrectData_ThenSuccess() {
        User currentUser = getUserDao().getById(1L);
        User removeUser = getUserDao().getById(1L);
        Chat chat = getChatDao().getById(3L);
        chatService.removeUserFromChat(chat.getId(), currentUser.getId(), removeUser.getId());
        assertNull(getChatParticipationDao().getUserParticipationInChatByChatId(chat.getId(), removeUser.getId()));

    }

    @Test
    @Order(35)
    void writeToSupport_WhenRequestDoesntAlreadyExists_ThenSuccess() {
        User currentUser = getUserDao().getById(4L);
        assertNotNull(chatService.writeToSupport(currentUser, "Какова твоя профессия, ты миллиционер?"));
    }

    @Test
    @Order(36)
    void writeToSupport_WhenRequestAlreadyExists_ThenInvalidActionException() {
        User currentUser = getUserDao().getById(4L);
        assertThrows(InvalidActionException.class, () -> chatService.writeToSupport(currentUser, "Какова твоя профессия, ты миллиционер?"));
    }

    @Test
    @Order(37)
    void answerSupportRequest_WhenUserDidntSentRequest_ThenEntityNotFoundException(){
        User user = userMap.get(1L);
        assertThrows(EntityNotFoundException.class, () -> chatService.answerSupportRequest(user, "Text!"));
    }

    @Test
    @Order(38)
    void answerSupportRequest_WhenUserSentRequest_ThenSuccess(){
        User user = userMap.get(4L);
        chatService.answerSupportRequest(user, "Text!");
        User currentUser = getUserDao().getById(4L);
        assertNotNull(chatService.writeToSupport(currentUser, "Повторный тест для проверки на ответ и удаление прошлого из бд"));
    }

}
