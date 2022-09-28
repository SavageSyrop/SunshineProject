package com.shevtsov.sunshine.dao.entities;

import com.shevtsov.sunshine.common.GenderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "userInfos")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserInfo extends AbstractEntity {

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String email;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column
    private GenderType genderType;

    @Column
    private Date dateOfBirth;

    @Column
    private String city;
    /**
     * регулирует уровень доступа к информации пользователя, его друзьям, стене, подпискам на группы и возможность написать лично
     */
    @Column
    private Boolean openProfile;

    public UserInfo(String username, String password,String email, String firstName, String lastName, GenderType genderType, Date dateOfBirth, String city, Boolean openProfile) {
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
}
