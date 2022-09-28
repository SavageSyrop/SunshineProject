package com.shevtsov.sunshine.dto.mappers;

import com.shevtsov.sunshine.dao.RoleDao;
import com.shevtsov.sunshine.dto.UserPublicDto;
import com.shevtsov.sunshine.dto.UserShortDto;
import com.shevtsov.sunshine.dao.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper extends ListMapper<UserPublicDto, User> {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private ChatParticipationMapper chatParticipationMapper;

    @Autowired
    private GroupMembershipMapper groupMembershipMapper;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private WallPostMapper postMapper;

    @Override
    public UserPublicDto toDto(User entity) {
        return new UserPublicDto(entity.getId(), userInfoMapper.toSecuredDto(entity.getUserInfo()), entity.getRole().getName().name());

    }

    public UserShortDto toShortDto(User entity) {
        return new UserShortDto(entity.getId(), userInfoMapper.toShortDto(entity.getUserInfo()), entity.getRole().getName().name());
    }
}
