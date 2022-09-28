package com.shevtsov.sunshine.dao;

import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.dao.entities.User;

import java.util.List;


public interface PostDao extends AbstractDao<Post> {
    List<Post> getPostsFromUserWall(User wallOwner);
}
