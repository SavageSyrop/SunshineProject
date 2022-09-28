package com.shevtsov.sunshine.service.impl;

import com.shevtsov.sunshine.dao.entities.FriendRequest;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dao.entities.UserInfo;
import com.shevtsov.sunshine.dao.entities.Wall;
import com.shevtsov.sunshine.common.GenderType;
import com.shevtsov.sunshine.common.UserWallPermissionType;
import com.shevtsov.sunshine.exceptions.AlreadyExistsException;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.BannedException;
import com.shevtsov.sunshine.exceptions.InvalidActionException;
import com.shevtsov.sunshine.exceptions.SelfInteractionException;
import com.shevtsov.sunshine.exceptions.WeakDataException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceImplTest extends AbstractServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @Order(1)
    void addUser_whenInvalidEmail_thenThrowsWeakDataException() {
        String username = "Ognestrel";
        String password = "Password1";
        String email = "project.sunshinemail.ru";
        String firstName = "Valera";
        String lastName = "Poghiloy";
        GenderType genderType = GenderType.KROGAN;
        Date dateOfBirth = new Date(1937, Calendar.JULY, 8);
        String city = "Sirzhan";
        Boolean openProfile = false;

        UserInfo userInfo = new UserInfo(username, password, email, firstName, lastName, genderType, dateOfBirth, city, openProfile);

        assertThrows(WeakDataException.class, () -> userService.addUser(userInfo, 1L));

    }

    @Test
    @Order(2)
    void addUser_whenInvalidPassword_thenThrowsWeakDataException() {
        String username = "Ognestrel";
        String password = "1234";
        String email = "project.sunshine@mail.ru";
        String firstName = "Valera";
        String lastName = "Poghiloy";
        GenderType genderType = GenderType.KROGAN;
        Date dateOfBirth = new Date(1937, Calendar.JULY, 8);
        String city = "Sirzhan";
        Boolean openProfile = false;


        UserInfo userInfo = new UserInfo(username, password, email, firstName, lastName, genderType, dateOfBirth, city, openProfile);

        assertThrows(WeakDataException.class, () -> userService.addUser(userInfo, 1L));

    }

    @Test
    @Order(3)
    void addUser_whenDataCorrent_thenSuccess() {
        String username = "Ognestrel";
        String password = "Password1";
        String email = "project.sunshine@mail.ru";
        String firstName = "Valera";
        String lastName = "Poghiloy";
        GenderType genderType = GenderType.KROGAN;
        Date dateOfBirth = new Date(1937, Calendar.JULY, 8);
        String city = "Sirzhan";
        Boolean openProfile = false;

        UserInfo userInfo = new UserInfo(username, password, email, firstName, lastName, genderType, dateOfBirth, city, openProfile);
        User user = userService.addUser(userInfo, 1L);


        assertNotNull(user.getActivationCode());
        assertNotNull(user.getId());
        assertTrue(getBCryptPasswordEncoder().matches(password, user.getPassword()));
        assertEquals(user, getUserMap().get(user.getId()));

    }

    @Test
    @Order(4)
    void editCurrentUser_whenUsernameAlreadyExists_thenThrowAlreadyExistsException() {
        String username = "desertfox";
        String password = "Adminus0";
        String email = "sunshine.project@mail.ru";
        String city = "Baltimore";


        User currentUser = getUserDao().getById(2L);
        UserInfo newCurrentUserInfo = new UserInfo();
        newCurrentUserInfo.setUsername(username);
        newCurrentUserInfo.setPassword(password);
        newCurrentUserInfo.setEmail(email);
        newCurrentUserInfo.setCity(city);
        newCurrentUserInfo.setGenderType(GenderType.KROGAN);


        assertThrows(AlreadyExistsException.class, () -> userService.editCurrentUser(currentUser, newCurrentUserInfo, currentUser.getPassword()));
    }

    @Test
    @Order(5)
    void editCurrentUser_whenWrongCurrentPassword_thenThrowWeakDataException() {
        User currentUser = getUserDao().getById(1L);

        UserInfo newCurrentUserInfo = new UserInfo();
        newCurrentUserInfo.setUsername("NewUsername");

        assertThrows(WeakDataException.class, () -> userService.editCurrentUser(currentUser, newCurrentUserInfo, "wrong password"));
    }

    @Test
    @Order(6)
    void editCurrentUser_whenWeakPassword_thenThrowWeakDataException() {
        User currentUser = getUserDao().getById(1L);
        UserInfo newCurrentUserInfo = new UserInfo();
        newCurrentUserInfo.setPassword("weak");

        assertThrows(WeakDataException.class, () -> userService.editCurrentUser(currentUser, newCurrentUserInfo, "Adminus0"));
    }

    @Test
    @Order(7)
    void editCurrentUser_whenWeakEmail_thenThrowWeakDataException() {
        User currentUser = getUserDao().getById(1L);
        UserInfo newCurrentUserInfo = new UserInfo();
        newCurrentUserInfo.setEmail("weakEmail");

        assertThrows(WeakDataException.class, () -> userService.editCurrentUser(currentUser, newCurrentUserInfo, "Password1"));
    }

    @Test
    @Order(8)
    void editCurrentUser_whenDataCorrent_thenSuccess() {
        User currentUser = getUserDao().getById(7L);
        UserInfo newCurrentUserInfo = new UserInfo();
        newCurrentUserInfo.setUsername("Ognestrel228");
        newCurrentUserInfo.setEmail("project63.sunshine12@mail.ru");
        newCurrentUserInfo.setPassword("StrongPassword1");

        userService.editCurrentUser(currentUser, newCurrentUserInfo, "Password1");

        assertEquals("Ognestrel228", getUserMap().get(currentUser.getId()).getUsername());
        assertEquals("project63.sunshine12@mail.ru", getUserMap().get(currentUser.getId()).getUserInfo().getEmail());
        assertTrue(getBCryptPasswordEncoder().matches("StrongPassword1", getUserMap().get(currentUser.getId()).getUserInfo().getPassword()));
    }


    @Test
    @Order(9)
    void restorePassword_WhenRestoreCodeWrong_thenThrowEntityNotFoundException() {
        User user = getUserDao().getById(1L);
        user.setRestorePasswordCode("1234");
        assertThrows(EntityNotFoundException.class, () -> userService.restorePassword("4321", "StrongPassword2"));
    }

    @Test
    @Order(10)
    void restorePassword_WhenNewPasswordIsWeak_thenThrowWeakDataException() {
        assertThrows(WeakDataException.class, () -> userService.restorePassword("1234", "weak"));
    }

    @Test
    @Order(11)
    void restorePassword_WhenDataIsCorrect_thenSuccess() {
        userService.restorePassword("1234", "AdminusNew0");
        User user = getUserDao().getById(1L);
        assertNull(user.getRestorePasswordCode());
        assertTrue(getBCryptPasswordEncoder().matches("AdminusNew0", user.getPassword()));
    }

    @Test
    @Order(12)
    void activateAccount_WhenActivationCodeIsWrong_thenThrowEntityNotFound() {
        User user = getUserDao().getById(1L);
        user.setActivationCode("9999");
        assertThrows(EntityNotFoundException.class, () -> userService.activateAccount("54"));
    }

    @Test
    @Order(13)
    void activateAccount_WhenActivationCodeIsCorrect_thenSuccess() {
        User user = getUserDao().getById(1L);
        user.setActivationCode("9999");
        assertNull(userService.activateAccount("9999").getActivationCode());
    }

    @Test
    @Order(14)
    void addFriend_WhenTryingToFriendYourself_thenThrowsSelfInteractionException() {
        User user = getUserDao().getById(1L);

        assertThrows(SelfInteractionException.class, () -> userService.addFriend(user, user));
    }

    @Test
    @Order(15)
    void addFriend_WhenCorrectUsers_thenSuccess() {
        User sender = getUserDao().getById(1L);
        User recipient = getUserDao().getById(2L);
        assertNotNull(userService.addFriend(sender, recipient));
        assertEquals(sender, friendRequestMap.get(1L).getSenderUser());
        assertEquals(recipient, friendRequestMap.get(1L).getRecipientUser());
    }

    @Test
    @Order(16)
    void addFriend_WhenAlreadySent_thenThrowsAlreadyExistsException() {
        User sender = getUserDao().getById(1L);
        User recipient = getUserDao().getById(2L);
        assertThrows(AlreadyExistsException.class, () -> userService.addFriend(sender, recipient));
    }

    @Test
    @Order(17)
    void declineFriendRequest_WhenRequestReceived_thenSuccess() {
        User recipient = getUserDao().getById(2L);
        assertNotNull(userService.declineFriendRequest(recipient, 1L));
        assertNull(friendRequestMap.get(1L));
    }

    @Test
    @Order(18)
    void declineFriendRequest_WhenRequestIdWrong_thenThrowsEntityNotFound() {
        User recipient = getUserDao().getById(2L);
        assertThrows(EntityNotFoundException.class, () -> userService.declineFriendRequest(recipient, 54L));
    }

    @Test
    @Order(19)
    void declineFriendRequest_WhenWrongRecipient_thenAuthorizationErrorException() {
        User sender = getUserDao().getById(1L);
        User recipient = getUserDao().getById(2L);

        FriendRequest friendReqiest = userService.addFriend(sender, recipient);         // отправлен запрос с айди 2

        User fakeRec = getUserDao().getById(3L);

        assertThrows(AuthorizationErrorException.class, () -> userService.declineFriendRequest(fakeRec, 2L));
    }

    @Test
    @Order(20)
    void acceptFriendRequest_WhenRequestIdWrong_thenThrowsEntityNotFound() {
        User sender = getUserDao().getById(1L);
        User recipient = getUserDao().getById(2L);
        assertThrows(EntityNotFoundException.class, () -> userService.acceptFriendRequest(recipient, 54L));
    }

    @Test
    @Order(21)
    void acceptFriendRequest_WhenRequestReceived_thenSuccess() {
        User recipient = getUserDao().getById(2L);
        assertEquals(true, userService.acceptFriendRequest(recipient, 2L).getIsAccepted());
        assertEquals(true, friendRequestMap.get(2L).getIsAccepted());
    }

    @Test
    @Order(22)
    void declineFriendRequest_WhenRequestAlreadyAccepted_thenThrowsInvalidActionException() {
        User recipient = getUserDao().getById(2L);
        assertThrows(InvalidActionException.class, () -> userService.declineFriendRequest(recipient, 2L));
    }

    @Test
    @Order(23)
    void unfriendUser_WhenRecipientUserNotFriend_thenThrowsEntityNotFound() {
        User currentUser = getUserDao().getById(1L);
        User recipient = getUserDao().getById(3L);
        assertThrows(EntityNotFoundException.class, () -> userService.unfriendUser(currentUser, recipient));
    }

    @Test
    @Order(24)
    void unfriendUser_WhenFriendsipExists_thenSuccess() {
        User currentUser = getUserDao().getById(1L);
        User recipient = getUserDao().getById(2L);
        FriendRequest backwardsFriendRequest = getFriendRequestDao().getFriendRequestByUserIds(2L, 1L);
        Long deletingRequestId = getFriendRequestDao().getFriendRequestByUserIds(1L, 2L).getId();

        assertNotNull(userService.unfriendUser(currentUser, recipient));
        assertNull(friendRequestMap.get(deletingRequestId));
        assertEquals(false, backwardsFriendRequest.getIsAccepted());
    }

    @Test
    @Order(25)
    void unfriendUser_WhenNotAFriend_thenThrowInvalidActionException() {
        User currentUser = getUserDao().getById(1L);
        User recipient = getUserDao().getById(2L);
        assertThrows(EntityNotFoundException.class, () -> userService.unfriendUser(currentUser, recipient));
    }

    @Test
    @Order(26)
    void unfriendUser_WhenSubscribed_thenSuccess() {
        User currentUser = getUserDao().getById(2L);
        User recipient = getUserDao().getById(1L);
        assertNotNull(userService.unfriendUser(currentUser, recipient));
    }

    @Test
    @Order(27)
    void banUser_WhenBanningAdmin_thenThrowInvalidActionException() {
        User userToBan = getUserDao().getById(5L);
        assertThrows(InvalidActionException.class, () -> userService.banUser(userToBan));
    }

    @Test
    @Order(28)
    void banUser_WhenBanningUser_ThenSuccess() {
        User userToBan = getUserDao().getById(2L);
        userService.banUser(userToBan);
        assertEquals(true, userToBan.getIsBanned());
    }

    @Test
    @Order(29)
    void banUser_WhenAlreadyBannedUser_ThenThrowBannedException() {
        User userToBan = getUserDao().getById(2L);
        assertThrows(BannedException.class, () -> userService.banUser(userToBan));
    }

    @Test
    @Order(30)
    void unbanUser_WhenBannedUser_ThenSuccess() {
        User userToUnban = getUserDao().getById(2L);
        userService.unbanUser(userToUnban);
        assertEquals(false, userToUnban.getIsBanned());
    }

    @Test
    @Order(31)
    void unbanUser_WhenNotBanned_ThenInvalidActionException() {
        User userToUnban = getUserDao().getById(2L);
        assertThrows(InvalidActionException.class, () -> userService.unbanUser(userToUnban));
    }

    @Test
    @Order(32)
    void forgotPassword_WhenCalled_ThenSuccess() {
        User userToResetPassword = getUserDao().getById(2L);
        userService.forgotPassword(userToResetPassword.getUsername());
        assertNotNull(userToResetPassword.getRestorePasswordCode());
        userService.restorePassword(userToResetPassword.getRestorePasswordCode(), "Roflan54");         // чтобы восстановить бд до изначального состояния
        assertNull(userToResetPassword.getRestorePasswordCode());
    }

    @Test
    @Order(33)
    void changeWallSettings_WhenNoParams_ThenSavePreviousSuccess() {
        User user = getUserDao().getById(1L);
        Wall wall = user.getWall();
        UserWallPermissionType postPermission = wall.getPostPermission();
        UserWallPermissionType commentPermission = wall.getCommentPermission();;
        userService.changeWallSettings(user,null,null);
        assertEquals(wall.getCommentPermission(), commentPermission);
        assertEquals(wall.getPostPermission(), postPermission);
    }

    @Test
    @Order(34)
    void changeWallSettings_WhenAllParams_ThenSaveNewSuccess() {
        User user = getUserDao().getById(1L);
        Wall wall = user.getWall();
        UserWallPermissionType postPermission = wall.getPostPermission();
        UserWallPermissionType commentPermission = wall.getCommentPermission();;
        userService.changeWallSettings(user,UserWallPermissionType.FRIENDS, UserWallPermissionType.FRIENDS);
        assertNotEquals(wall.getCommentPermission(), commentPermission);
        assertNotEquals(wall.getPostPermission(), postPermission);
    }
}