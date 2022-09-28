package com.shevtsov.sunshine.service;

import com.shevtsov.sunshine.dao.entities.Comment;
import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import com.shevtsov.sunshine.dao.entities.Like;
import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.dao.entities.User;

import java.util.List;

public interface PostService extends AbstractService<Post> {

    Post addGroupPost(User currentUser, Group group, String text);

    Comment addGroupPostComment(User currentUser, String message, Post post, Group group, GroupMembership groupMembership);

    Like addPostLike(User currentUser, Post post);

    Like addGroupCommentLike(Long userId, Comment comment, Group group);

    Like addWallCommentLike(User userSender, Comment comment, User wallOwner);

    Post addWallPost(User currentUser, User wallOwner, String text);

    Comment addWallPostComment(User currentUser, User wallOwner, Post requestedPost, String message);

    String deleteUserWallPost(User currentUser, User wallOwner, Long postId);

    void deleteCommentById(Long commentId);

    Post dealWithOfferedPost(Long postId, boolean toSave);

    Comment getCommentById(Long commentId);

    List<Post> getPostsFromUserWall(User wallOwner);
}
