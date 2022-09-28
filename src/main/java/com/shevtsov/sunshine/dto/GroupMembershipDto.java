package com.shevtsov.sunshine.dto;

import com.shevtsov.sunshine.common.GroupRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GroupMembershipDto extends AbstractDto {
    private Long userId;
    private Long groupId;
    private GroupRole groupRole;

    public GroupMembershipDto(Long id, Long userId, Long groupId, GroupRole groupRole) {
        this.id = id;
        this.userId = userId;
        this.groupId = groupId;
        this.groupRole = groupRole;
    }
}
