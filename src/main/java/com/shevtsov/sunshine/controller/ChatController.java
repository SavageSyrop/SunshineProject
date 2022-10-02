package com.shevtsov.sunshine.controller;


import com.shevtsov.sunshine.dao.entities.Chat;
import com.shevtsov.sunshine.dao.entities.ChatParticipation;
import com.shevtsov.sunshine.dao.entities.Message;
import com.shevtsov.sunshine.dao.entities.ResponseMessage;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dto.ChatDto;
import com.shevtsov.sunshine.dto.ChatParticipationDto;
import com.shevtsov.sunshine.dto.MessageDto;
import com.shevtsov.sunshine.dto.mappers.ChatMapper;
import com.shevtsov.sunshine.dto.mappers.ChatParticipationMapper;
import com.shevtsov.sunshine.dto.mappers.MessageMapper;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.WeakDataException;
import com.shevtsov.sunshine.service.ChatService;
import com.shevtsov.sunshine.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер, обрабатывающий запросы связанные с взаимодействием с чатами
 */

@Slf4j
@RestController
@RequestMapping("/")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private ChatParticipationMapper chatParticipationMapper;

    @Autowired
    private MessageMapper messageMapper;


    public ChatController(ChatService chatService, UserService userService, ChatMapper chatMapper, ChatParticipationMapper chatParticipationMapper, MessageMapper messageMapper) {
        this.chatService = chatService;
        this.userService = userService;
        this.chatMapper = chatMapper;
        this.chatParticipationMapper = chatParticipationMapper;
        this.messageMapper = messageMapper;
    }

    /**
     * @return информация о членствах пользователя в чатах
     */

    @GetMapping("/chats")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<ChatParticipationDto> getCurrentUserChatParticipations() {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        return chatParticipationMapper.toListDto(currentUser.getChatParticipations());
    }

    /**
     * @param chatId id чата, c которым происходит взаимодействие
     * @return полная информация о чате, включая сообщения в нём
     * @throws AuthorizationErrorException если текущий пользователь не состоит в чате
     */

    @GetMapping("/chat{chatId}")
    @PreAuthorize("hasAuthority('WRITING')")
    public ChatDto getChat(@PathVariable Long chatId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        if (chatService.getUserParticipationInChatByChatId(chatId, currentUser.getId()) == null) {
            throw new AuthorizationErrorException("This user is not a participant of chat with id " + chatId);
        }
        Chat chat = chatService.getById(chatId);
        return chatMapper.toDto(chat);
    }

    /**
     * @param chatId      id чата, с которым происходит взаимодействие (null, если сообщение предназначается лично пользователю)
     * @param mes         текст нового сообщения
     * @param recipientId id пользователя, получающего сообщение (null, если сообщение предназначается групповому чату)
     * @return информация о созданном сообщении
     * @throws WeakDataException если оба параметра (chatId и recipientId) заданы или не заданы одновременно
     */

    @PostMapping("/chat")
    @PreAuthorize("hasAuthority('WRITING')")
    public MessageDto writeInChat(@RequestParam(required = false) Long chatId, @RequestBody String mes, @RequestParam(required = false) Long recipientId) {
        if ((chatId == null && recipientId == null) || (chatId != null && recipientId != null)) {
            throw new WeakDataException("No params or both received. Specify only chaId OR recipientId. ChatId - to write in groupChat and recipientId to write personally ");
        }
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Message message = chatService.addMessage(currentUser, recipientId, chatId, mes);
        return messageMapper.toDto(message);
    }

    /**
     * @param chatId id чата, с которым происходит взаимодействие
     * @return информация об участии текущего пользователя в чате
     * @throws AuthorizationErrorException текущий пользователь не состоит в запрашиваемом чате
     */


    @GetMapping("/chat{chatId}/participations")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<ChatParticipationDto> getCurrentUserChatParticipantionsByChatId(@PathVariable Long chatId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        if (chatService.getUserParticipationInChatByChatId(chatId, currentUser.getId()) == null) {
            throw new AuthorizationErrorException("This user is not a participant of chat with id " + chatId);
        }
        return chatParticipationMapper.toListDto(chatService.getChatParticipantsByChatId(chatId));
    }

    /**
     * @param userToAddId id пользователя, которого добавляют в чат при создании
     * @param chatName    имя группового чата
     * @return информация об участниках группового чата
     */

    @PostMapping("/public_chat")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<ChatParticipationDto> createPublicChat(@RequestParam Long userToAddId, @RequestParam String chatName) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User userToAdd = userService.getById(userToAddId);
        Chat chat = chatService.createPublicChat(chatName, currentUser, userToAdd);
        return chatParticipationMapper.toListDto(chatService.getChatParticipantsByChatId(chat.getId()));
    }

    /**
     * @param chatId      id чата, с которым происходит взаимодействие
     * @param userToAddId id пользователя, которого добавляют в чат при создании
     * @return информация о добавленном участнике группового чата
     */

    @PostMapping("/public_chat/add_user")
    @PreAuthorize("hasAuthority('WRITING')")
    public ChatParticipationDto addChatParticipantToChat(@RequestParam Long chatId, @RequestParam Long userToAddId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        ChatParticipation chatParticipation = chatService.addChatParticipantToChat(currentUser, chatId, userToAddId);
        return chatParticipationMapper.toDto(chatParticipation);
    }

    /**
     * @param chatId   id чата, с которым происходит взаимодействие
     * @param chatName новое имя чата
     * @return полная информация о чате, включая сообщения в нём
     */

    @PatchMapping("/chat{chatId}")
    @PreAuthorize("hasAuthority('WRITING')")
    public ChatDto renameChat(@PathVariable Long chatId, @RequestParam String chatName) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Chat chat = chatService.getById(chatId);
        chat = chatService.renameChat(currentUser, chat, chatName);
        return chatMapper.toDto(chat);
    }

    /**
     * @param chatId         id чата, с которым происходит взаимодействие
     * @param userToRemoveId id пользователя, удаляемого из группового чата
     * @return статус действия
     */

    @DeleteMapping("/public_chat{chatId}/remove_user")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage removeFromChat(@PathVariable Long chatId, @RequestParam Long userToRemoveId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User userToRemove = userService.getById(userToRemoveId);
        chatService.removeUserFromChat(chatId, currentUser.getId(), userToRemoveId);
        return new ResponseMessage(userToRemove.getUsername() + " has been removed from chat");
    }

    /**
     * @param chatId id чата, с которым происходит взаимодействие
     * @return информация о владельце чата
     */

    @GetMapping("/public_chat{chatId}/owner")
    @PreAuthorize("hasAuthority('WRITING')")
    public ChatParticipationDto getOwner(@PathVariable Long chatId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        ChatParticipation ownerChatParticipation = chatService.getChatOwnerParticipation(currentUser.getId(), chatId);
        return chatParticipationMapper.toDto(ownerChatParticipation);
    }

    /**
     * @param chatId     id чата, с которым происходит взаимодействие
     * @param newOwnerId id пользователя, который станет новым владельцем чата
     * @return статус действия
     */

    @PatchMapping("/public_chat{chatId}/owner")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage setChatOwner(@PathVariable Long chatId, @RequestParam Long newOwnerId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User newOwner = userService.getById(newOwnerId);
        Chat chat = chatService.getById(chatId);
        chatService.setChatOwner(currentUser, newOwner, chat);

        return new ResponseMessage("New owner of chat " + chat.getName() + " is " + newOwner.getUsername());
    }

    /**
     * @param userRequest текст запроса
     * @return информация созданного запроса в поддержку
     */

    @PostMapping("/support")
    @PreAuthorize("hasAuthority('WRITING')")
    public MessageDto writeToSupport(@RequestBody String userRequest) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Message message = chatService.writeToSupport(currentUser, userRequest);
        return messageMapper.toDto(message);
    }

    /**
     * @return информация о полученных необработанных запросов в поддержку
     */

    @GetMapping("/support_requests")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public List<MessageDto> getSupportRequests() {
        return messageMapper.toListDto(chatService.getSupportRequests());
    }

    /**
     * @param messageId id сообщения обращения в поддержку
     * @param answer    ответ пользователю
     * @return статус действия
     */

    @DeleteMapping("/support")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public ResponseMessage answerSupportRequest(@RequestParam Long messageId, @RequestBody String answer) {
        Message message = chatService.getMessageById(messageId);
        chatService.answerSupportRequest(message.getSender(), answer);
        return new ResponseMessage("Request has been closed, answer is sent to: " + message.getSender().getUserInfo().getEmail());
    }

    private String getAuthenticationName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
