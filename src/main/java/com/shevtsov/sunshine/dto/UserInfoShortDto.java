package com.shevtsov.sunshine.dto;

import com.shevtsov.sunshine.common.GenderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserInfoShortDto extends AbstractDto {
    protected String username;
    protected String email;
    protected GenderType genderType;

    public UserInfoShortDto(Long id, String username, String email, GenderType genderType) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.genderType = genderType;
    }
}
