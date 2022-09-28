package com.shevtsov.sunshine.dao;

import com.shevtsov.sunshine.dao.entities.Message;
import com.shevtsov.sunshine.dao.entities.User;

import java.util.List;


public interface MessageDao extends AbstractDao<Message> {
    Message getUserSupportRequest(User currentUser);

    List<Message> getSupportRequests();
}
