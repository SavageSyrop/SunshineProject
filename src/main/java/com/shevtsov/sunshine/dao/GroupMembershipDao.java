package com.shevtsov.sunshine.dao;


import com.shevtsov.sunshine.dao.entities.GroupMembership;

import java.util.List;

public interface GroupMembershipDao extends AbstractDao<GroupMembership> {
    GroupMembership getGroupMembershipByUserAndGroupIds(Long userId, Long groupId);

    List<GroupMembership> getSubscribeRequests(Long groupId);

    GroupMembership getGroupOwnerMembership(Long groupId);

    List<GroupMembership> getGroupMemberships(Long userId);
}
