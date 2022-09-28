package com.shevtsov.sunshine.dto.mappers;


import com.shevtsov.sunshine.dto.GroupDto;
import com.shevtsov.sunshine.dto.GroupMembershipDto;
import com.shevtsov.sunshine.dto.WallPostDto;
import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.common.GroupRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupMapper extends ListMapper<GroupDto, Group> {

    @Autowired
    private GroupMembershipMapper groupMembershipMapper;

    @Autowired
    private WallPostMapper postMapper;

    @Override
    public GroupDto toDto(Group entity) {
        List<GroupMembershipDto> membershipDtos = new ArrayList<>();
        for (GroupMembership groupMembership : entity.getGroupMemberships()) {
            if (groupMembership.getGroupRole() != GroupRole.AWAITING_CHECK) {
                membershipDtos.add(groupMembershipMapper.toDto(groupMembership));
            }
        }
        List<WallPostDto> wallDto = new ArrayList<>();
        for (Post post : entity.getWall()) {
            wallDto.add(postMapper.toDto(post));
        }
        return new GroupDto(entity.getId(), entity.getName(), entity.getInfo(), entity.getWallType(), entity.getOpenToJoin(), wallDto, membershipDtos);
    }


    @Override
    public Group toEntity(GroupDto dto) {
        return new Group(dto.getName(), dto.getInfo(), dto.getWallType(), dto.getOpenToJoin());
    }
}
