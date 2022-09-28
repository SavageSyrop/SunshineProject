package com.shevtsov.sunshine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shevtsov.sunshine.common.GenderType;
import com.shevtsov.sunshine.common.TimeFormatPatterns;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoSearchDto extends UserInfoShortDto {
    private String firstName;
    private String lastName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormatPatterns.DATE_PATTERN)
    private Date fromDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormatPatterns.DATE_PATTERN)
    private Date toDate;
    private String city;

    public UserInfoSearchDto(String username, String email, String firstName, String lastName, GenderType genderType, Date fromDate, Date toDate, String city) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.genderType = genderType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.city = city;
    }
}
