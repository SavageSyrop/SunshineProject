package com.shevtsov.sunshine.service;

import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.common.GroupRole;

import java.util.List;


public interface GroupService extends AbstractService<Group> {
    GroupMembership createGroupMembership(GroupMembership member);

    GroupMembership getGroupMembershipByUserAndGroupIds(Long userId, Long groupId);

    void deleteMembershipById(Long id);

    void updateMembership(GroupMembership membership);

    List<GroupMembership> getSubscribeRequests(Long groupId);

    GroupMembership getGroupMembershipById(Long subRequestId);

    Group editGroup(User currentUser, Group newGroupInfo);

    GroupMembership grantRoleTo(GroupMembership currentUserMembership, GroupMembership subscriberMembership, GroupRole groupRole);

    GroupMembership subscribeToGroup(User currentUser, Group group);

    void unsubscribeFromGroup(User currentUser, Group group);

    List<Post> getGroupPublishedPosts(Long groupId);

    List<Post> getGroupOfferedPosts(Long groupId);

    GroupMembership getGroupOwnerMembership(Long groupId);

    List<GroupMembership> getGroupMemberships(Long userId);

    Group addGroup(User currentUser, Group group);
}
