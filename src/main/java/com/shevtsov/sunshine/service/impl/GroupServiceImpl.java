package com.shevtsov.sunshine.service.impl;

import com.shevtsov.sunshine.dao.GroupDao;
import com.shevtsov.sunshine.dao.GroupMembershipDao;
import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.common.GroupRole;
import com.shevtsov.sunshine.exceptions.ActionAlreadyCompletedException;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.InvalidActionException;
import com.shevtsov.sunshine.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Component
public class GroupServiceImpl extends AbstractServiceImpl<Group, GroupDao> implements GroupService {

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private GroupMembershipDao groupMembershipDao;


    public GroupServiceImpl(GroupDao defaultDao) {
        super(defaultDao);
    }

    @Override
    public GroupMembership createGroupMembership(GroupMembership member) {
        return groupMembershipDao.create(member);
    }

    @Override
    public GroupMembership getGroupMembershipByUserAndGroupIds(Long userId, Long groupId) {
        return groupMembershipDao.getGroupMembershipByUserAndGroupIds(userId, groupId);
    }

    @Override
    public void deleteMembershipById(Long id) {
        groupMembershipDao.deleteById(id);
    }

    @Override
    public void updateMembership(GroupMembership membership) {
        groupMembershipDao.update(membership);
    }

    @Override
    public List<GroupMembership> getSubscribeRequests(Long groupId) {
        return groupMembershipDao.getSubscribeRequests(groupId);
    }

    @Override
    public GroupMembership getGroupMembershipById(Long subRequestId) {
        return groupMembershipDao.getById(subRequestId);
    }

    /**
     * @param currentUser  текущий пользователь
     * @param newGroupInfo новая информация группы
     * @return группа c обновленной информацией
     * @throws AuthorizationErrorException если текущий пользователь не владелец группы
     */

    @Override
    public Group editGroup(User currentUser, Group newGroupInfo) {
        GroupMembership currentUserMembership = getGroupMembershipByUserAndGroupIds(currentUser.getId(), newGroupInfo.getId());

        if (currentUserMembership == null || currentUserMembership.getGroupRole() != GroupRole.OWNER) {
            throw new AuthorizationErrorException("Only owner of this group can edit it!");
        }

        Group groupToEdit = groupDao.getById(newGroupInfo.getId());

        if (newGroupInfo.getName() != null) {
            groupToEdit.setName(newGroupInfo.getName());
        }

        if (newGroupInfo.getInfo() != null) {
            groupToEdit.setInfo(newGroupInfo.getInfo());
        }

        if (newGroupInfo.getWallType() != null) {
            groupToEdit.setWallType(newGroupInfo.getWallType());
        }

        if (newGroupInfo.getOpenToJoin() != null) {
            groupToEdit.setOpenToJoin(newGroupInfo.getOpenToJoin());
        }

        groupDao.update(groupToEdit);
        return groupToEdit;
    }

    /**
     * @param currentUserMembership членство текущего пользователя
     * @param subscriberMembership  членство выбранного пользователя
     * @param groupRole             выдаваемая роль
     * @return обновленное членство выбранного пользователя
     */

    @Override
    public GroupMembership grantRoleTo(GroupMembership currentUserMembership, GroupMembership subscriberMembership, GroupRole groupRole) {
        if (currentUserMembership.getGroupRole() != GroupRole.OWNER) {
            throw new AuthorizationErrorException("Only owner of this group can grant roles!");
        }

        subscriberMembership.setGroupRole(groupRole);
        updateMembership(subscriberMembership);
        if (groupRole == GroupRole.OWNER) {
            currentUserMembership.setGroupRole(GroupRole.SUBSCRIBER);
            updateMembership(currentUserMembership);
        }
        return subscriberMembership;
    }

    /**
     * @param currentUser текущий пользователь
     * @param group       группа
     * @return членство подписчика текущего пользователя
     * @throws ActionAlreadyCompletedException если текущий пользователь уже подписан
     */

    @Override
    public GroupMembership subscribeToGroup(User currentUser, Group group) {
        GroupMembership groupMembership = getGroupMembershipByUserAndGroupIds(currentUser.getId(), group.getId());

        if (groupMembership != null) {
            throw new ActionAlreadyCompletedException("You are already subscribed to " + group.getName() + ". To unsubscribe follow /group" + group.getId() + "/unsubscribe");
        }

        if (group.getOpenToJoin()) {
            groupMembership = new GroupMembership(currentUser, group, GroupRole.SUBSCRIBER);
        } else {
            groupMembership = new GroupMembership(currentUser, group, GroupRole.AWAITING_CHECK);
        }
        groupMembership = createGroupMembership(groupMembership);
        return groupMembership;
    }

    /**
     * @param currentUser текущий пользователь
     * @param group       выбранная группа
     * @throws EntityNotFoundException если пользователь не участник группы
     * @throws InvalidActionException  если текущий пользователь имеет роль OWNER
     */

    @Override
    public void unsubscribeFromGroup(User currentUser, Group group) {
        GroupMembership groupMembership = getGroupMembershipByUserAndGroupIds(currentUser.getId(), group.getId());

        if (groupMembership == null) {
            throw new EntityNotFoundException("You are not a member of group with id " + group.getId());
        }

        if (groupMembership.getGroupRole() == GroupRole.OWNER) {
            if (group.getGroupMemberships().size() == 1) {
                groupMembershipDao.deleteById(groupMembership.getId());
                groupDao.deleteById(group.getId());
                return;
            }
            throw new InvalidActionException("You can't unsubscribe from group until giving OWNER rights to other subscriber. To set a successor follow /group{groupId}/grantRoleTo{subscriberId}/OWNER . You also can delete the group, follow /group{groupId} with DELETE method");
        }
        groupMembershipDao.deleteById(groupMembership.getId());
    }

    @Override
    public List<Post> getGroupPublishedPosts(Long groupId) {
        return defaultDao.getGroupPublishedPosts(groupId);
    }

    @Override
    public List<Post> getGroupOfferedPosts(Long groupId) {
        return defaultDao.getGroupOfferedPosts(groupId);
    }

    @Override
    public GroupMembership getGroupOwnerMembership(Long groupId) {
        return groupMembershipDao.getGroupOwnerMembership(groupId);
    }

    @Override
    public List<GroupMembership> getGroupMemberships(Long userId) {
        return groupMembershipDao.getGroupMemberships(userId);
    }

    @Override
    public Group addGroup(User currentUser, Group group) {
        group = defaultDao.create(group);
        GroupMembership ownerMember = new GroupMembership(currentUser, group, GroupRole.OWNER);
        groupMembershipDao.create(ownerMember);
        return group;
    }
}
