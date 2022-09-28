package com.shevtsov.sunshine.dto;

import com.shevtsov.sunshine.common.GroupWallType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GroupDto extends AbstractDto {
    private String name;
    private String info;
    private GroupWallType wallType;
    private Boolean openToJoin;
    private List<WallPostDto> wall;
    private List<GroupMembershipDto> groupMemberships;


    public GroupDto(Long id, String name, String info, GroupWallType wallType, Boolean openToJoin, List<WallPostDto> wall, List<GroupMembershipDto> groupMemberships) {
        this.id = id;
        this.name = name;
        this.info = info;
        this.wallType = wallType;
        this.openToJoin = openToJoin;
        this.wall = wall;
        this.groupMemberships = groupMemberships;
    }
}
