package com.shevtsov.sunshine.dto.mappers;

import com.shevtsov.sunshine.dto.GroupMembershipDto;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import org.springframework.stereotype.Component;

@Component
public class GroupMembershipMapper extends ListMapper<GroupMembershipDto, GroupMembership> {
    @Override
    public GroupMembershipDto toDto(GroupMembership entity) {
        return new GroupMembershipDto(entity.getId(), entity.getUser().getId(), entity.getGroup().getId(), entity.getGroupRole());
    }
}
