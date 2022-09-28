package com.shevtsov.sunshine.service.impl;

import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.common.GroupRole;
import com.shevtsov.sunshine.common.GroupWallType;
import com.shevtsov.sunshine.exceptions.ActionAlreadyCompletedException;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.InvalidActionException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GroupServiceImplTest extends AbstractServiceImplTest {

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    @Order(1)
    void editGroup_WhenCurrentUserNotOwner_WhenThrowsAuthorizationErrorException() {
        User currentUser = getUserDao().getById(3L);
        Group group = getGroupDao().getById(1L);
        assertNotNull(group);
        assertThrows(AuthorizationErrorException.class, () -> groupService.editGroup(currentUser, group));
    }

    @Test
    @Order(2)
    void editGroup_WhenCorrect_ThenSuccess() {
        User owner = getUserDao().getById(1L);
        Group group = getGroupDao().getById(1L);
        assertEquals(GroupWallType.ALL, group.getWallType());

        group.setWallType(GroupWallType.SUBSCRIBERS);
        group.setInfo("New info");
        group.setName("New name");
        assertNotNull(groupService.editGroup(owner, group));
        assertNotEquals(GroupWallType.ALL, group.getWallType());
    }


    @Test
    @Order(3)
    void subscribeToGroup_WhenAlreadySubscribed_ThenActionAlreadyCompletedException() {
        User currentUser = getUserDao().getById(1L);
        Group group = getGroupDao().getById(1L);
        assertThrows(ActionAlreadyCompletedException.class, () -> groupService.subscribeToGroup(currentUser, group));
    }

    @Test
    @Order(4)
    void subscribeToGroup_WhenCorrectData_ThenSuccess() {
        User currentUser = getUserDao().getById(2L);
        Group group = getGroupDao().getById(1L);
        assertNotNull(groupService.subscribeToGroup(currentUser, group));
    }


    @Test
    @Order(5)
    void grantRoleTo_WhenCurrentUserNotOwner_ThenThrowAuthorizationErrorException() {
        User currentUser = getUserDao().getById(2L);
        Group group = getGroupDao().getById(1L);
        GroupMembership currentUserMembership = getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(currentUser.getId(), group.getId());
        GroupMembership grantToUserMembership = getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(1L, group.getId());
        assertThrows(AuthorizationErrorException.class, () -> groupService.grantRoleTo(currentUserMembership, grantToUserMembership, GroupRole.OWNER));
    }

    @Test
    @Order(6)
    void grantRoleTo_WhenCurrentUserIsOwner_ThenSuccess() {
        User currentUser = getUserDao().getById(1L);
        User userToGrant = getUserDao().getById(2L);
        Group group = getGroupDao().getById(1L);
        GroupMembership currentUserMembership = getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(currentUser.getId(), group.getId());
        GroupMembership grantToUserMembership = getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(userToGrant.getId(), group.getId());
        assertNotNull(groupService.grantRoleTo(currentUserMembership, grantToUserMembership, GroupRole.OWNER));
        assertEquals(currentUserMembership.getGroupRole(), GroupRole.SUBSCRIBER);
        assertEquals(grantToUserMembership.getGroupRole(), GroupRole.OWNER);
    }

    @Test
    @Order(7)
    void unsubscribeFromGroup_WhenNotMemberOfGroup_ThenThrowEntityNotFoundException() {
        User currentUser = getUserDao().getById(1L);
        Group group = new Group();
        assertThrows(EntityNotFoundException.class, () -> groupService.unsubscribeFromGroup(currentUser, group));
    }

    @Test
    @Order(8)
    void unsubscribeFromGroup_WhenOwnerIsNotTheLastMemberOfGroup_ThenThrowInvalidActionException() {
        User currentUser = getUserDao().getById(2L);

        Group group = getGroupDao().getById(1L);
        assertThrows(InvalidActionException.class, () -> groupService.unsubscribeFromGroup(currentUser, group));

    }

    @Test
    @Order(9)
    void unsubscribeFromGroup_WhenDataCorrect_ThenSuccess() {
        User currentUser = getUserDao().getById(1L);
        Group group = getGroupDao().getById(1L);
        groupService.unsubscribeFromGroup(currentUser, group);
        assertNull(groupService.getGroupMembershipByUserAndGroupIds(currentUser.getId(), group.getId()));

    }


    @Test
    @Order(10)
    void unsubscribeFromGroup_WhenOwnerDataCorrect_ThenSuccess() {
        User currentUser = getUserDao().getById(2L);
        Group group = getGroupDao().getById(1L);
        groupService.unsubscribeFromGroup(currentUser, group);
        assertNull(groupService.getGroupMembershipByUserAndGroupIds(currentUser.getId(), group.getId()));

    }

    @Test
    @Order(11)
    void addGroup_WhenDataCorrect_ThenSuccess() {
        User user = getUserDao().getById(2L);
        Group group = new Group("Name", "Info", GroupWallType.SUBSCRIBERS, true);
        assertNotNull(groupService.addGroup(user, group));
    }
}