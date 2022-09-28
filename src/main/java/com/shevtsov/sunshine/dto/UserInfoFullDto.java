package com.shevtsov.sunshine.dto;

import com.shevtsov.sunshine.common.GenderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoFullDto extends UserInfoSecuredDto {
    private String password;

    public UserInfoFullDto(Long id, String username, String password, String email, String firstName, String lastName, GenderType genderType, Date dateOfBirth, String city, Boolean openProfile) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.genderType = genderType;
        this.dateOfBirth = dateOfBirth;
        this.city = city;
        this.openProfile = openProfile;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
