package com.shevtsov.sunshine.controller;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.shevtsov.sunshine.common.SecurityConstants;
import com.shevtsov.sunshine.dao.entities.ResponseMessage;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dao.entities.UserInfo;
import com.shevtsov.sunshine.dao.entities.UserSearchInfo;
import com.shevtsov.sunshine.dto.AbstractUserDto;
import com.shevtsov.sunshine.dto.UserInfoFullDto;
import com.shevtsov.sunshine.dto.UserInfoSearchDto;
import com.shevtsov.sunshine.dto.UserInfoSecuredDto;
import com.shevtsov.sunshine.dto.UserInfoShortDto;
import com.shevtsov.sunshine.dto.UserPublicDto;
import com.shevtsov.sunshine.dto.mappers.GroupMembershipMapper;
import com.shevtsov.sunshine.dto.mappers.UserInfoMapper;
import com.shevtsov.sunshine.dto.mappers.UserMapper;
import com.shevtsov.sunshine.exceptions.AlreadyExistsException;
import com.shevtsov.sunshine.service.UserService;
import com.shevtsov.sunshine.service.impl.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * Контроллер, обрабатывающий запросы связанные с личной информацией пользователей)
 */

@Slf4j
@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupMembershipMapper groupMembershipMapper;

    /**
     * регистрация пользователя, пароль шифрутся и хранится в БД в зашифрованном виде
     *
     * @param userInfoDto получаемая при регистрации личная информация пользователя
     * @param roleId      id устанавливаемой пользователю роли
     * @return информация о созданном пользователе
     */

    @PutMapping("/sign_up")
    public UserPublicDto signUp(@RequestBody UserInfoFullDto userInfoDto, @RequestParam Long roleId) {
        if (userService.getUserByUsername(userInfoDto.getUsername()) != null) {
            throw new AlreadyExistsException("User " + userInfoDto.getUsername() + " already exists!");
        }
        UserInfo userInfo = userInfoMapper.toEntity(userInfoDto);
        User user = userService.addUser(userInfo, roleId);
        return userMapper.toDto(user);
    }

    /**
     * @param username логин пользователя
     * @return статус действия
     */

    @PostMapping("/forgot_password")
    public ResponseMessage forgotPassword(@RequestParam String username) {
        userService.forgotPassword(username);
        return new ResponseMessage("Email successfully sent! Follow instructions in email to set new password!");
    }

    /**
     * @param restoreCode код востановления пароля
     * @param newPassword новый пароль
     * @return статус действия
     */

    @PostMapping("/reset_password/{restoreCode}")
    public ResponseMessage restore(@PathVariable String restoreCode, @RequestParam String newPassword) {
        userService.restorePassword(restoreCode, newPassword);
        return new ResponseMessage("You password has been reset successfully!");
    }

    /**
     * @param activationCode код активации аккаунта
     * @return статус действия
     */

    @PostMapping("/activate/{activationCode}")
    public ResponseMessage activate(@PathVariable String activationCode) {
        userService.activateAccount(activationCode);
        return new ResponseMessage("Your account has been successfully activated");
    }

    /**
     * @param userInfoDto     новые личные данные пользователя
     * @param currentPassword текущий пароль
     * @return обновленные данные и новый токен
     */

    @PostMapping("/edit")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage edit(@RequestBody UserInfoFullDto userInfoDto, @RequestParam String currentPassword) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        boolean credentialsChanged = credentialsHasChanged(userInfoDto, currentUser);
        boolean emailChanged = emailHasChanged(userInfoDto, currentUser);
        UserInfo updated = userService.editCurrentUser(currentUser, userInfoMapper.toEntity(userInfoDto), currentPassword);
        String body = "User personal info has been updated!\n";

        if (emailChanged) {
            body = body + "\nEMAIL HAS BEEN CHANGED AND OLD JWT TOKEN HAS BEEN TAKEN!\nYOU NEED TO ACTIVATE YOUR ACCOUNT BY CHECKING EMAIL. THEN LOGIN AGAIN AND RECEIVE NEW TOKEN!";
            return new ResponseMessage(body);
        }

        if (credentialsChanged) {
            String token = JWT.create()
                    .withSubject(updated.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                    .sign(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()));
            body = body + "\nNEW TOKEN GENERATED DUE TO USERNAME CHANGE! CHANGE YOUR OLD TOKEN!\nTOKEN PREFIX: " + SecurityConstants.TOKEN_PREFIX + "\nTOKEN: " + token;

        }
        return new ResponseMessage(body);
    }

    /**
     * @param id id пользователя, личную информацию которого нужно получить
     * @return личная информация о пользователе
     */

    @GetMapping("/id{id}")
    public AbstractUserDto getUserInfo(@PathVariable Long id) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User user = userService.getById(id);
        if (user.isOpenUser() || userService.isFriendOf(currentUser.getId(), user.getId())) {
            return userMapper.toDto(user);
        } else {
            return userMapper.toShortDto(user);
        }
    }


    /**
     * @param userInfoSearchDto параметры для поиска пользователей
     * @return список пользователей, соответствующих найденным
     */

    @GetMapping("/search")
    public List<UserInfoShortDto> findUsersByParams(@RequestBody UserInfoSearchDto userInfoSearchDto) {
        UserSearchInfo userSearchInfo = userInfoMapper.toSearchEntity(userInfoSearchDto);
        List<UserInfo> userInfos = userService.getUsersByParams(userSearchInfo);
        return userInfoMapper.toListShortDto(userInfos);
    }

    /**
     * @param userId id пользователя, которого нужно забанить
     * @return статус действия
     */

    @PostMapping("/ban")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public ResponseMessage banUser(@RequestParam Long userId) {
        User user = userService.getById(userId);
        userService.banUser(user);
        return new ResponseMessage(user.getUsername() + " has been successfully banned");
    }

    /**
     * @param userId id пользователя, которого нужно разбанить
     * @return статус действия
     */

    @PostMapping("/unban")
    @PreAuthorize("hasAuthority('ADMIN_ACTIONS')")
    public ResponseMessage unbanUser(@RequestParam Long userId) {
        User user = userService.getById(userId);
        userService.unbanUser(user);
        return new ResponseMessage(user.getUsername() + " has been successfully unbanned");
    }

    /**
     * @param userInfoDto получаемые для проверки данные
     * @param currentUser проверяемый текущий пользователь
     * @return статус проверки изменения почты
     */


    private boolean emailHasChanged(UserInfoFullDto userInfoDto, User currentUser) {
        if (userInfoDto.getEmail() != null) {
            return !currentUser.getUserInfo().getEmail().equals(userInfoDto.getEmail());
        }
        return false;
    }

    /**
     * @param userInfoDto получаемые для проверки данные
     * @param currentUser проверяемый текущий пользователь
     * @return статус проверки изменения логина
     */

    private boolean credentialsHasChanged(UserInfoFullDto userInfoDto, User currentUser) {
        if (userInfoDto.getUsername() != null) {
            return !currentUser.getUsername().equals(userInfoDto.getUsername());
        }
        return false;

    }

    private String getAuthenticationName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
