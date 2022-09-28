package com.shevtsov.sunshine.dao.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shevtsov.sunshine.common.GenderType;
import com.shevtsov.sunshine.common.TimeFormatPatterns;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class UserSearchInfo extends AbstractEntity {
    private String username;
    private String firstName;
    private String lastName;
    private GenderType genderType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormatPatterns.DATE_PATTERN)
    private Date fromDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormatPatterns.DATE_PATTERN)
    private Date toDate;
    private String city;
}
