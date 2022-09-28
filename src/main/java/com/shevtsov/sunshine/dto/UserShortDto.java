package com.shevtsov.sunshine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserShortDto extends AbstractUserDto {
    protected UserInfoShortDto infoDto;

    public UserShortDto(Long id, UserInfoShortDto infoDto, String roleName) {
        this.id = id;
        this.infoDto = infoDto;
        this.roleName = roleName;
    }
}
