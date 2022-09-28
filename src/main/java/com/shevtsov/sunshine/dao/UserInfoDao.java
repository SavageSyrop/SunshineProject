package com.shevtsov.sunshine.dao;


import com.shevtsov.sunshine.dao.entities.UserInfo;
import com.shevtsov.sunshine.dao.entities.UserSearchInfo;

import java.util.List;

public interface UserInfoDao extends AbstractDao<UserInfo> {

    List<UserInfo> getUserInfosByParams(UserSearchInfo userSearchInfo);

}
