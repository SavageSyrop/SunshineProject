package com.shevtsov.sunshine.controller;

import com.shevtsov.sunshine.dao.entities.FriendRequest;
import com.shevtsov.sunshine.dao.entities.ResponseMessage;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dto.FriendRequestDto;
import com.shevtsov.sunshine.dto.mappers.FriendRequestMapper;
import com.shevtsov.sunshine.exceptions.InvalidActionException;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер, обрабатывающий запросы связанные с запросами дружбы
 */

@Slf4j
@RestController
@RequestMapping("/")
public class FriendController {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    /**
     * @return информация о друзьях текущего пользователя
     */

    @GetMapping("/friends")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<FriendRequestDto> getCurrentUserFriends() {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        return friendRequestMapper.toListDto(userService.getUserFriends(currentUser.getId()));
    }

    /**
     * @param userId пользователь, информацию о чьих друзьях нужно получить
     * @return информация о друзьях пользователя
     * @throws InvalidActionException если запрашиваемый профиль скрыт, а текущий пользователь не друг запрашиваемого
     */

    @GetMapping("/id{userId}/friends")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<FriendRequestDto> getUserFriends(@PathVariable Long userId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User user = userService.getById(userId);
        if (!user.isOpenUser() && !userService.isFriendOf(currentUser.getId(), userId)) {
            throw new InvalidActionException("This user has private profile! You can't see his friends");
        }
        return friendRequestMapper.toListDto((userService.getUserFriends(userId)));
    }


    /**
     * @return информация об отправленных запросах дружбы текущего пользователя
     */

    @GetMapping("/friend_requests/sent")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<FriendRequestDto> getCurrentUserSentFriendRequestUsers() {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        return friendRequestMapper.toListDto(userService.getUserSentFriendRequests(currentUser.getId()));
    }

    /**
     * @return информация о полученных запросах дружбы текущего пользователя
     */

    @GetMapping("/friend_requests/received")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<FriendRequestDto> getCurrentUserReceivedFriendRequests() {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        List<FriendRequest> receivedFriendRequests = userService.getUserReceivedFriendRequests(currentUser.getId());
        return friendRequestMapper.toListDto(receivedFriendRequests);
    }

    /**
     * @param userId id пользователя, которому будет послан запрос дружбы
     * @return информация о созданном запросе дружбы
     */

    @PostMapping("/friend")
    @PreAuthorize("hasAuthority('WRITING')")
    public FriendRequestDto addFriend(@RequestParam Long userId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User otherUser = userService.getById(userId);
        FriendRequest friendRequest = userService.addFriend(currentUser, otherUser);
        return friendRequestMapper.toDto(friendRequest);
    }

    /**
     * @param requestId id запрос дружбы, который нужно принять
     * @return информация об обновленном запросе дружбы
     */

    @PatchMapping("/friend_requests/accept")
    @PreAuthorize("hasAuthority('WRITING')")
    public FriendRequestDto acceptFriendRequest(@RequestParam Long requestId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        FriendRequest request = userService.acceptFriendRequest(currentUser, requestId);
        return friendRequestMapper.toDto(request);
    }

    /**
     * @param requestId id запрос дружбы, который нужно отклонить
     * @return статус действия
     */

    @DeleteMapping("/friend_requests/decline")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage declineFriendRequest(@RequestParam Long requestId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        return new ResponseMessage(userService.declineFriendRequest(currentUser, requestId));
    }

    /**
     * @param userId id пользователя, которого нужно удалить из друзей
     * @return статус действия
     */

    @DeleteMapping("/unfriend")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage revokeOwnFriendRequest(@RequestParam Long userId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User otherUser = userService.getById(userId);
        return new ResponseMessage(userService.unfriendUser(currentUser, otherUser));
    }

    private String getAuthenticationName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
