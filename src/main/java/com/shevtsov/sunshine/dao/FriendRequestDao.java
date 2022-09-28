package com.shevtsov.sunshine.dao;

import com.shevtsov.sunshine.dao.entities.FriendRequest;

import java.util.List;

public interface FriendRequestDao extends AbstractDao<FriendRequest> {
    List<FriendRequest> getUserReceivedFriendRequests(Long id);

    FriendRequest getFriendRequestByUserIds(Long currentUserId, Long recipientId);

    List<FriendRequest> getUserFriends(Long userId);

    List<FriendRequest> getUserSentFriendRequests(Long userId);

    Boolean isFriendOf(Long currentUserId, Long recipientUserId);
}
