package com.shevtsov.sunshine.dao;


import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.Post;

import java.util.List;

public interface GroupDao extends AbstractDao<Group> {
    List<Post> getGroupPublishedPosts(Long groupId);

    List<Post> getGroupOfferedPosts(Long groupId);
}
