package com.shevtsov.sunshine.dao;

import com.shevtsov.sunshine.dao.entities.User;


public interface UserDao extends AbstractDao<User> {
    User getByUsername(String username);

    User getUserByActivationCode(String activationCode);

    User getUserByRestorePasswordCode(String restoreCode);
}
