package com.shevtsov.sunshine.dao;

import com.shevtsov.sunshine.dao.entities.Like;


public interface LikeDao extends AbstractDao<Like> {
    Like getCommentLikeByUserId(Long commentId, Long userId);

    Like getPostLikeByUserId(Long postId, Long userId);
}
