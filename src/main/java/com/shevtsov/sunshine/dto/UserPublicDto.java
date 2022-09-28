package com.shevtsov.sunshine.dto;

import lombok.Getter;

@Getter
public class UserPublicDto extends AbstractUserDto {
    private UserInfoSecuredDto userInfo;

    public UserPublicDto(Long id, UserInfoSecuredDto userInfo, String roleName) {
        this.id = id;
        this.userInfo = userInfo;
        this.roleName = roleName;
    }
}
