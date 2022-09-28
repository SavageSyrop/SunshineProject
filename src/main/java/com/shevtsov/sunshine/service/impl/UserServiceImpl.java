package com.shevtsov.sunshine.service.impl;


import com.shevtsov.sunshine.dao.FriendRequestDao;
import com.shevtsov.sunshine.dao.RoleDao;
import com.shevtsov.sunshine.dao.UserDao;
import com.shevtsov.sunshine.dao.UserInfoDao;
import com.shevtsov.sunshine.dao.WallDao;
import com.shevtsov.sunshine.dao.entities.FriendRequest;
import com.shevtsov.sunshine.dao.entities.Role;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dao.entities.UserInfo;
import com.shevtsov.sunshine.dao.entities.UserSearchInfo;
import com.shevtsov.sunshine.dao.entities.Wall;
import com.shevtsov.sunshine.common.RoleType;
import com.shevtsov.sunshine.common.UserWallPermissionType;
import com.shevtsov.sunshine.exceptions.AlreadyExistsException;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.BannedException;
import com.shevtsov.sunshine.exceptions.InvalidActionException;
import com.shevtsov.sunshine.exceptions.SelfInteractionException;
import com.shevtsov.sunshine.exceptions.WeakDataException;
import com.shevtsov.sunshine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserServiceImpl extends AbstractServiceImpl<User, UserDao> implements UserService {
    @Autowired
    private UserInfoDao userInfoDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private FriendRequestDao friendRequestDao;

    @Autowired
    private WallDao wallDao;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private MailService mailService;

    public UserServiceImpl(UserDao defaultDao) {
        super(defaultDao);
    }

    public User addUser(UserInfo userInfo, Long roleId) {
        isValidEmail(userInfo.getEmail());
        isValidPassword(userInfo.getPassword());
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        userInfoDao.create(userInfo);
        Role role = roleDao.getById(roleId);
        Wall wall = new Wall(UserWallPermissionType.ALL, UserWallPermissionType.ALL);
        wallDao.create(wall);
        User user = new User(userInfo, wall, role);
        user.setActivationCode(UUID.randomUUID().toString());
        defaultDao.create(user);
        mailService.sendActivationEmail(user);
        return user;
    }

    /**
     * @param currentUser     текущий пользователь
     * @param newData         новый данные пользователя
     * @param currentPassword текущий пароль
     * @return обновленная информация пользователя
     * @throws WeakDataException если текущий пароль неверный
     */

    @Override
    public UserInfo editCurrentUser(User currentUser, UserInfo newData, String currentPassword) {

        UserInfo oldData = currentUser.getUserInfo();

        if (loadUserByUsername(newData.getUsername()) != null && !oldData.getUsername().equals(newData.getUsername())) {
            throw new AlreadyExistsException("User " + newData.getUsername() + " already exists!");
        }

        if (encoder.matches(currentPassword, currentUser.getPassword())) {
            isValidPassword(newData.getPassword());
            if (newData.getPassword() != null) {
                oldData.setPassword(encoder.encode(newData.getPassword()));
            }

            if (newData.getUsername() != null) {
                oldData.setUsername(newData.getUsername());
            }

            if (newData.getEmail() != null && !(currentUser.getUserInfo().getEmail().equals(newData.getEmail()))) {
                isValidEmail(newData.getEmail());
                currentUser.setActivationCode(UUID.randomUUID().toString());
                oldData.setEmail(newData.getEmail());
                mailService.sendActivationEmail(currentUser);
            }

            if (newData.getFirstName() != null) {
                oldData.setFirstName(newData.getFirstName());
            }

            if (newData.getLastName() != null) {
                oldData.setLastName(newData.getLastName());
            }

            if (newData.getGenderType() != null) {
                oldData.setGenderType(newData.getGenderType());
            }

            if (newData.getDateOfBirth() != null) {
                oldData.setDateOfBirth(newData.getDateOfBirth());
            }

            if (newData.getCity() != null) {
                oldData.setCity(newData.getCity());
            }

            if (newData.getOpenProfile() != null) {
                oldData.setOpenProfile(newData.getOpenProfile());
            }

            userInfoDao.update(oldData);
            currentUser.setUserInfo(oldData);
            defaultDao.update(currentUser);

            return oldData;
        } else {
            throw new WeakDataException("Wrong current password was entered!");
        }
    }

    /**
     * @param restoreCode уникальный код для восстановления пароля
     * @param newPassword новый пароль
     * @throws EntityNotFoundException неверный код восстановления
     */

    @Override
    public void restorePassword(String restoreCode, String newPassword) {
        User user = defaultDao.getUserByRestorePasswordCode(restoreCode);
        if (user != null) {
            isValidPassword(newPassword);
            user.setRestorePasswordCode(null);
            UserInfo userInfo = user.getUserInfo();
            userInfo.setPassword(encoder.encode(newPassword));
            userInfoDao.update(userInfo);
            defaultDao.update(user);
            return;
        }
        throw new EntityNotFoundException("Invalid restore code!");
    }

    /**
     * @param activationCode уникальный код для активации аккаунта
     * @return активированный пользователь
     * @throws EntityNotFoundException если неверный код активации
     */

    @Override
    public User activateAccount(String activationCode) {
        User user = defaultDao.getUserByActivationCode(activationCode);
        if (user != null) {
            user.setActivationCode(null);
            defaultDao.update(user);
            return user;
        }
        throw new EntityNotFoundException("Invalid activation code!");
    }

    @Override
    public List<FriendRequest> getUserReceivedFriendRequests(Long id) {
        return friendRequestDao.getUserReceivedFriendRequests(id);
    }

    /**
     * @param currentUser текущий пользователь
     * @param otherUser   добавляемый в друзья пользователь
     * @return созданный запрос дружбы
     * @throws AlreadyExistsException   если запрос дружбы уже существует
     * @throws SelfInteractionException если попытка добавить себя в друзья
     */

    @Override
    public FriendRequest addFriend(User currentUser, User otherUser) {
        if (currentUser.getId().equals(otherUser.getId())) {
            throw new SelfInteractionException("You can't add yourself to friends!");
        }

        FriendRequest currentUserRequest = getFriendRequestByUserIds(currentUser.getId(), otherUser.getId());

        if (currentUserRequest != null) {
            if (currentUserRequest.getIsAccepted()) {
                throw new AlreadyExistsException("You have already a friend of that user!");
            }
            throw new AlreadyExistsException("You have already sent friend request to that user!");
        }

        FriendRequest recipientRequest = getFriendRequestByUserIds(otherUser.getId(), currentUser.getId());

        if (recipientRequest == null) {
            currentUserRequest = new FriendRequest(currentUser, otherUser, false);
            currentUserRequest = friendRequestDao.create(currentUserRequest);
        } else {
            currentUserRequest = new FriendRequest(currentUser, otherUser, true);
            recipientRequest.setIsAccepted(true);
            currentUserRequest = friendRequestDao.create(currentUserRequest);
            friendRequestDao.update(recipientRequest);
        }
        return currentUserRequest;
    }

    @Override
    public FriendRequest acceptFriendRequest(User currentUser, Long requestId) {
        FriendRequest receivedRequest = friendRequestDao.getById(requestId);
        if (!receivedRequest.getRecipientUser().getId().equals(currentUser.getId())) {
            throw new AuthorizationErrorException("This friend request wasn't sent to you!");
        }
        FriendRequest currentUserRequest = new FriendRequest(currentUser, receivedRequest.getSenderUser(), true);
        receivedRequest.setIsAccepted(true);
        friendRequestDao.create(currentUserRequest);
        friendRequestDao.update(receivedRequest);
        return receivedRequest;
    }

    /**
     * @param currentUser текущий пользователь
     * @param requestId   id отменяемого запроса в друзья
     * @return статус действия
     * @throws InvalidActionException если попытка отменить существующую дружбу
     */

    @Override
    public String declineFriendRequest(User currentUser, Long requestId) {
        FriendRequest receivedRequest = friendRequestDao.getById(requestId);

        if (receivedRequest.getSenderUser().getId().equals(currentUser.getId()) && !receivedRequest.getIsAccepted()) {
            friendRequestDao.deleteById(requestId);
            return "You have successfully declined your friend request to " + receivedRequest.getRecipientUser().getUsername();
        }

        if (!receivedRequest.getRecipientUser().getId().equals(currentUser.getId())) {
            throw new AuthorizationErrorException("This friend request wasn't sent to you!");
        }
        User otherUser = defaultDao.getById(receivedRequest.getSenderUser().getId());

        if (receivedRequest.getIsAccepted()) {
            throw new InvalidActionException("To unfriend user follow: /unfriend{userId}");
        }

        friendRequestDao.deleteById(requestId);
        return "You have successfully declined friend request from " + otherUser.getUsername();
    }

    /**
     * @param currentUser текущий пользователь
     * @param otherUser   пользователь, удаляемый из друзей
     * @return статус действия
     * @throws EntityNotFoundException если дружба между текущим и выбранным пользователями не найдена
     */

    @Override
    public String unfriendUser(User currentUser, User otherUser) {
        FriendRequest currentUserRequest = getFriendRequestByUserIds(currentUser.getId(), otherUser.getId());
        FriendRequest otherUserRequest = getFriendRequestByUserIds(otherUser.getId(), currentUser.getId());

        if (currentUserRequest == null) {
            throw new EntityNotFoundException("Friendship between " + currentUser.getUsername() + " and " + otherUser.getUsername() + " doesn't exist");
        }

        if (currentUserRequest.getIsAccepted()) {
            friendRequestDao.deleteById(currentUserRequest.getId());
            otherUserRequest.setIsAccepted(false);
            return "You are not friends with " + otherUser.getUsername() + " anymore";
        } else {
            friendRequestDao.deleteById(currentUserRequest.getId());
            return "You are now unsubscribed from " + otherUser.getUsername();
        }
    }

    @Override
    public void banUser(User userToBan) {
        if (userToBan.getIsBanned()) {
            throw new BannedException("This user is already banned");
        }
        if (userToBan.getRole().getName() != RoleType.ADMIN) {
            userToBan.setIsBanned(true);
        } else {
            throw new InvalidActionException("ADMIN CAN'T BE BANNED");
        }
    }

    @Override
    public void unbanUser(User userToUnban) {
        if (userToUnban.getIsBanned()) {
            userToUnban.setIsBanned(false);
        } else {
            throw new InvalidActionException("This user is not banned");
        }
    }

    @Override
    public void forgotPassword(String username) {
        User user = getUserByUsername(username);
        String passwordCode = UUID.randomUUID().toString();
        user.setRestorePasswordCode(passwordCode);
        mailService.sendForgotPasswordEmail(user);
        defaultDao.update(user);
    }

    /**
     * @param currentUser       текущий пользователь
     * @param postPermission    новый уровень доступа к написанию постов
     * @param commentPermission новый уровень доступа к написанию комментариев
     */
    @Override
    public void changeWallSettings(User currentUser, UserWallPermissionType postPermission, UserWallPermissionType commentPermission) {
        if (postPermission == null) {
            postPermission = currentUser.getWall().getPostPermission();
        }
        if (commentPermission == null) {
            commentPermission = currentUser.getWall().getCommentPermission();
        }
        currentUser.getWall().setCommentPermission(commentPermission);
        currentUser.getWall().setPostPermission(postPermission);
    }

    @Override
    public List<FriendRequest> getUserFriends(Long userId) {
        return friendRequestDao.getUserFriends(userId);
    }

    @Override
    public List<FriendRequest> getUserSentFriendRequests(Long userId) {
        return friendRequestDao.getUserSentFriendRequests(userId);
    }

    @Override
    public Boolean isFriendOf(Long currentUserId, Long recipientUserId) {
        return friendRequestDao.isFriendOf(currentUserId, recipientUserId);
    }

    @Override
    public User getUserByUsername(String username) {
        return defaultDao.getByUsername(username);
    }


    @Override
    public List<UserInfo> getUsersByParams(UserSearchInfo userSearchInfo) {
        return userInfoDao.getUserInfosByParams(userSearchInfo);
    }

    @Override
    public User loadUserByUsername(String username) {
        return defaultDao.getByUsername(username);
    }

    /**
     * @param password пароль для проверки на требования
     * @throws WeakDataException если пароль не соответствует требованиям
     */

    private void isValidPassword(String password) {
        if (password == null) {
            return;
        }
        String regex = "^(?=.*\\d)(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).\\w{7,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        if (!matcher.find()) {
            throw new WeakDataException("Your password is weak! Please enter a password witch contains at least one uppercase, one lowercase letter and one number!");
        }
    }

    /**
     * @param email эмейл для проверки на требования
     * @throws WeakDataException если эмейл не соответствует rfr2822 шаблону
     */

    private void isValidEmail(String email) {
        if (email == null) {
            return;
        }
        String regex = "(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.find()) {
            throw new WeakDataException("You have entered email witch doesn't match rfc2822 pattern!");
        }
    }

    private FriendRequest getFriendRequestByUserIds(Long currentUserId, Long recipientId) {
        return friendRequestDao.getFriendRequestByUserIds(currentUserId, recipientId);
    }
}
