package com.shevtsov.sunshine.service;

import com.shevtsov.sunshine.dao.entities.FriendRequest;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dao.entities.UserInfo;
import com.shevtsov.sunshine.dao.entities.UserSearchInfo;
import com.shevtsov.sunshine.common.UserWallPermissionType;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends AbstractService<User>, UserDetailsService {
    User addUser(UserInfo userInfo, Long roleId);

    UserInfo editCurrentUser(User currentUser, UserInfo userInfo, String currentPassword);

    List<UserInfo> getUsersByParams(UserSearchInfo userSearchInfo);

    void restorePassword(String restoreCode, String newPassword);

    User activateAccount(String activationCode);

    List<FriendRequest> getUserReceivedFriendRequests(Long id);

    FriendRequest addFriend(User currentUser, User otherUser);

    FriendRequest acceptFriendRequest(User currentUser, Long requestId);

    String declineFriendRequest(User currentUser, Long requestId);

    String unfriendUser(User currentUser, User otherUser);

    void banUser(User userToBan);

    void unbanUser(User userToUnban);

    void forgotPassword(String username);

    void changeWallSettings(User currentUser, UserWallPermissionType postPermission, UserWallPermissionType commentPermission);

    List<FriendRequest> getUserFriends(Long userId);

    List<FriendRequest> getUserSentFriendRequests(Long userId);

    Boolean isFriendOf(Long currentUserId, Long recipientUserId);

    User getUserByUsername(String username);
}
