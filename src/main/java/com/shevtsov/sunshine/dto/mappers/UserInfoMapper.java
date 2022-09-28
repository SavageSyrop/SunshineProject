package com.shevtsov.sunshine.dto.mappers;


import com.shevtsov.sunshine.dto.UserInfoFullDto;
import com.shevtsov.sunshine.dto.UserInfoSearchDto;
import com.shevtsov.sunshine.dto.UserInfoSecuredDto;
import com.shevtsov.sunshine.dto.UserInfoShortDto;
import com.shevtsov.sunshine.dao.entities.UserInfo;
import com.shevtsov.sunshine.dao.entities.UserSearchInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserInfoMapper extends ListMapper<UserInfoFullDto, UserInfo> {
    public UserInfoFullDto toDto(UserInfo entity) {
        return new UserInfoFullDto(entity.getId(), entity.getUsername(), entity.getPassword(), entity.getEmail(),
                entity.getFirstName(), entity.getLastName(), entity.getGenderType(), entity.getDateOfBirth(), entity.getCity(), entity.getOpenProfile());
    }

    public UserInfoShortDto toShortDto(UserInfo entity) {
        return new UserInfoShortDto(entity.getId(), entity.getUsername(), entity.getEmail(), entity.getGenderType());
    }

    @Override
    public UserInfo toEntity(UserInfoFullDto dto) {
        UserInfo userInfo = new UserInfo(dto.getUsername(), dto.getPassword(), dto.getEmail(), dto.getFirstName(), dto.getLastName(), dto.getGenderType(), dto.getDateOfBirth(), dto.getCity(), dto.getOpenProfile());
        userInfo.setId(dto.getId());
        return userInfo;
    }

    public UserSearchInfo toSearchEntity(UserInfoSearchDto dto) {
        return new UserSearchInfo(dto.getUsername(), dto.getFirstName(), dto.getLastName(), dto.getGenderType(), dto.getFromDate(), dto.getToDate(), dto.getCity());
    }


    public UserInfoSecuredDto toSecuredDto(UserInfo entity) {
        return new UserInfoSecuredDto(entity.getId(), entity.getUsername(), entity.getEmail(),
                entity.getFirstName(), entity.getLastName(), entity.getGenderType(), entity.getDateOfBirth(), entity.getCity(), entity.getOpenProfile());
    }


    public List<UserInfoSecuredDto> toListSecuredDto(List<UserInfo> usersInfos) {
        List<UserInfoSecuredDto> userInfoDtoList = new ArrayList<>();
        for (UserInfo userInfo : usersInfos) {
            userInfoDtoList.add(toSecuredDto(userInfo));
        }
        return userInfoDtoList;
    }

    public List<UserInfoShortDto> toListShortDto(List<UserInfo> usersInfos) {
        List<UserInfoShortDto> userInfoDtoList = new ArrayList<>();
        for (UserInfo userInfo : usersInfos) {
            userInfoDtoList.add(toShortDto(userInfo));
        }
        return userInfoDtoList;
    }
}
