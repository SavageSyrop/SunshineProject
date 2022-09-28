package com.shevtsov.sunshine.service.impl;

import com.shevtsov.sunshine.dao.CommentDao;
import com.shevtsov.sunshine.dao.FriendRequestDao;
import com.shevtsov.sunshine.dao.GroupMembershipDao;
import com.shevtsov.sunshine.dao.LikeDao;
import com.shevtsov.sunshine.dao.PostDao;
import com.shevtsov.sunshine.dao.UserDao;
import com.shevtsov.sunshine.dao.entities.Comment;
import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import com.shevtsov.sunshine.dao.entities.Like;
import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dao.entities.Wall;
import com.shevtsov.sunshine.common.GroupRole;
import com.shevtsov.sunshine.common.GroupWallType;
import com.shevtsov.sunshine.common.RoleType;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.InvalidActionException;
import com.shevtsov.sunshine.exceptions.LikeRevokedException;
import com.shevtsov.sunshine.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Component
@Slf4j
public class PostServiceImpl extends AbstractServiceImpl<Post, PostDao> implements PostService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private GroupMembershipDao groupMembershipDao;

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private LikeDao likeDao;

    @Autowired
    private FriendRequestDao friendRequestDao;


    public PostServiceImpl(PostDao defaultDao) {
        super(defaultDao);
    }

    /**
     * @param currentUser текущий полтзователь
     * @param group       группа, в которую добавялется пост
     * @param text        текст поста
     * @return созданный пост группы
     * @throws AuthorizationErrorException если текущий пользователь не соответстует уровню доступа
     */

    @Override
    public Post addGroupPost(User currentUser, Group group, String text) {
        Post groupPost;

        if (group.getWallType() == GroupWallType.ALL) {
            groupPost = new Post(text, currentUser, group);
            groupPost.setIsPublished(true);
            defaultDao.create(groupPost);
            return groupPost;
        }

        if (group.getWallType() == GroupWallType.OFFERED_POSTS) {
            GroupMembership groupMembership = groupMembershipDao.getGroupMembershipByUserAndGroupIds(currentUser.getId(), group.getId());
            if (groupMembership == null && !group.getOpenToJoin()) {
                throw new AuthorizationErrorException("Join this group offer posts in this group!");
            }
            groupPost = new Post(text, currentUser, group);
            if (groupMembership != null && (groupMembership.getGroupRole().equals(GroupRole.OWNER) || groupMembership.getGroupRole().equals(GroupRole.ADMIN))) {
                groupPost.setIsPublished(true);
            } else {
                groupPost.setIsPublished(false);
            }
            defaultDao.create(groupPost);
            return groupPost;
        }

        GroupMembership groupMembership = groupMembershipDao.getGroupMembershipByUserAndGroupIds(currentUser.getId(), group.getId());

        if (groupMembership == null) {
            throw new AuthorizationErrorException("You are not subscribed. This wall is open for " + group.getWallType().toString());
        }

        if (groupMembership.getGroupRole() == GroupRole.AWAITING_CHECK) {
            throw new AuthorizationErrorException("Wait until you are accepted as subscriber. This wall is open for " + group.getWallType().toString());
        }


        switch (group.getWallType()) {
            case OWNER: {
                if (groupMembership.getGroupRole() == GroupRole.OWNER) {
                    groupPost = new Post(text, currentUser, group);
                    defaultDao.create(groupPost);
                    break;
                } else {
                    throw new AuthorizationErrorException("You are not authorized to write posts in this group. " + "This wall is open for " + group.getWallType().toString());
                }
            }
            case ADMINS: {
                if (groupMembership.getGroupRole() != GroupRole.SUBSCRIBER && groupMembership.getGroupRole() != GroupRole.AWAITING_CHECK) {
                    groupPost = new Post(text, currentUser, group);
                    defaultDao.create(groupPost);
                    break;
                } else {
                    throw new AuthorizationErrorException("You are not authorized to write posts in this group. " + "This wall is open for " + group.getWallType().toString());
                }
            }
            case SUBSCRIBERS: {
                if (groupMembership.getGroupRole() != GroupRole.AWAITING_CHECK) {
                    groupPost = new Post(text, currentUser, group);
                    defaultDao.create(groupPost);
                    break;
                }
            }
            default: {
                throw new AuthorizationErrorException("You are not authorized to write posts in this group. " + "This wall is open for " + group.getWallType().toString());
            }
        }
        return groupPost;
    }

    /**
     * @param postId id поста, который нужно отклонить или добавить в группу
     * @param toSave параметр действия , true - принять, false - отменит)
     * @return созданный пост
     * @throws EntityNotFoundException выбранный пост находится не в предложке
     */

    @Override
    public Post dealWithOfferedPost(Long postId, boolean toSave) {
        Post post = defaultDao.getById(postId);

        if (post == null || post.getIsPublished() == null || post.getIsPublished()) {
            throw new EntityNotFoundException("Post with such id is not offered");
        }

        if (toSave) {
            post.setIsPublished(true);
            defaultDao.update(post);
            return post;
        } else {
            defaultDao.deleteById(postId);
            return null;
        }
    }

    /**
     * @param currentUser     текущий пользователь
     * @param message         сообщение
     * @param post            выбранный пост
     * @param group           группа, в которой находится выбранный пост
     * @param groupMembership членство текущего пользователя
     * @return созданный комментарий
     * @throws AuthorizationErrorException если текущий пользователь не соответствует требованиям доступа
     */

    @Override
    public Comment addGroupPostComment(User currentUser, String message, Post post, Group group, GroupMembership groupMembership) {
        Comment newComment;
        switch (group.getWallType()) {
            case OFFERED_POSTS:
            case ALL: {
                newComment = new Comment(currentUser, message, post);
                commentDao.create(newComment);
                break;
            }
            case OWNER: {
                if (groupMembership != null && groupMembership.getGroupRole() == GroupRole.OWNER) {
                    newComment = new Comment(currentUser, message, post);
                    commentDao.create(newComment);
                    break;
                } else {
                    throw new AuthorizationErrorException("You are not authorized to write posts in this group. " + "This wall is open for " + group.getWallType().toString());
                }
            }
            case ADMINS: {
                if (groupMembership != null && groupMembership.getGroupRole() != GroupRole.SUBSCRIBER && groupMembership.getGroupRole() != GroupRole.AWAITING_CHECK) {
                    newComment = new Comment(currentUser, message, post);
                    commentDao.create(newComment);
                    break;
                } else {
                    throw new AuthorizationErrorException("You are not authorized to write posts in this group. " + "This wall is open for " + group.getWallType().toString());
                }
            }
            case SUBSCRIBERS: {
                if (groupMembership != null && groupMembership.getGroupRole() != GroupRole.AWAITING_CHECK) {
                    newComment = new Comment(currentUser, message, post);
                    commentDao.create(newComment);
                    break;
                }
            }
            default: {
                throw new AuthorizationErrorException("You are not authorized to write posts in this group. " + "This wall is open for " + group.getWallType().toString());
            }
        }
        return newComment;
    }

    /**
     * @param userId  id текущего пользователя
     * @param comment выбранный комментарий
     * @param group   группа, под постом в которой находится комментарий
     * @return созданный лайк
     * @throws AuthorizationErrorException если у текущего пользозателя нет доступа к стене
     * @throws LikeRevokedException        если повторно поставлен лайк, тогда он отменяется
     */
    @Override
    public Like addGroupCommentLike(Long userId, Comment comment, Group group) {
        if (!group.getOpenToJoin() && groupMembershipDao.getGroupMembershipByUserAndGroupIds(userId, group.getId()) == null) {
            throw new AuthorizationErrorException("Only subscribers have access to wall of this group!");
        }

        Like foundLikeId = likeDao.getCommentLikeByUserId(comment.getId(), userId);
        if (foundLikeId != null) {
            likeDao.deleteById(foundLikeId.getId());
            return null;
        }
        Like commentLike = new Like(userDao.getById(userId), comment);
        likeDao.create(commentLike);
        return commentLike;
    }


    /**
     * @param currentUser текущий пользователь
     * @param post        выбранный пост
     * @return созданный лайк поста
     * @throws AuthorizationErrorException у текущего пользователя нет доступа к стене
     * @throws LikeRevokedException        если повторно поставлен лайк, тогда он отменяется
     */

    @Override
    public Like addPostLike(User currentUser, Post post) {
        if (!post.getIsPublished()) {
            throw new InvalidActionException("It's post from offered posts, such posts aren't published and have no likes");
        }

        Group group = post.getGroup();
        if (group != null) {
            GroupMembership currentMembership = groupMembershipDao.getGroupMembershipByUserAndGroupIds(currentUser.getId(), group.getId());
            if (!group.getOpenToJoin()) {
                if (currentMembership == null || currentMembership.getGroupRole() == GroupRole.AWAITING_CHECK) {
                    throw new AuthorizationErrorException("You are not member of group with id " + group.getId());
                }
            }
        }

        Like foundLikeId = likeDao.getPostLikeByUserId(post.getId(), currentUser.getId());
        if (foundLikeId != null) {
            likeDao.deleteById(foundLikeId.getId());
            return null;
        }
        Like postLike = new Like(userDao.getById(currentUser.getId()), post);
        likeDao.create(postLike);
        return postLike;
    }

    @Override
    public Comment getCommentById(Long commentId) {
        return commentDao.getById(commentId);
    }

    @Override
    public List<Post> getPostsFromUserWall(User wallOwner) {
        return defaultDao.getPostsFromUserWall(wallOwner);
    }

    /**
     * @param currentUser текущий пользователь
     * @param wallOwner   владелец стены, на которую добавляется пост
     * @param text        текст поста
     * @return созданный пост
     * @throws AuthorizationErrorException нет доступа к стене выбранного пользователя
     */

    @Override
    public Post addWallPost(User currentUser, User wallOwner, String text) {
        Wall wall = wallOwner.getWall();
        Post post = null;
        switch (wall.getPostPermission()) {
            case ALL: {
                post = defaultDao.create(new Post(text, currentUser, wall));
                break;
            }
            case FRIENDS: {
                if (friendRequestDao.isFriendOf(currentUser.getId(), wallOwner.getId()) || currentUser.getId().equals(wall.getId())) {
                    post = defaultDao.create(new Post(text, currentUser, wall));
                    break;
                } else {
                    throw new AuthorizationErrorException("Only friends of " + wallOwner.getUsername() + " can write posts on this wall!");
                }
            }
            case OWNER: {
                if (currentUser.getId().equals(wallOwner.getId())) {
                    post = defaultDao.create(new Post(text, currentUser, wall));
                    break;
                } else {
                    throw new AuthorizationErrorException("Only owner can write posts on this wall!");
                }
            }
        }
        return post;
    }

    /**
     * @param currentUser   текущий пользователь
     * @param wallOwner     владелец стены
     * @param requestedPost запрошенный для комментирования поста
     * @param message       текст комментария
     * @return созданный комментарий
     * @throws AuthorizationErrorException нет доступа к стене выбранного пользователя
     */

    @Override
    public Comment addWallPostComment(User currentUser, User wallOwner, Post requestedPost, String message) {
        Wall wall = wallOwner.getWall();
        Comment comment = null;
        switch (wall.getCommentPermission()) {
            case ALL: {
                comment = commentDao.create(new Comment(currentUser, message, requestedPost));
                break;
            }
            case FRIENDS: {
                if (friendRequestDao.isFriendOf(currentUser.getId(), wallOwner.getId()) || currentUser.getId().equals(wall.getId())) {
                    comment = commentDao.create(new Comment(currentUser, message, requestedPost));
                    break;
                } else {
                    throw new AuthorizationErrorException("Only friends of " + wallOwner.getUsername() + " can write comments on this wall!");
                }
            }
            case OWNER: {
                if (currentUser.getId().equals(wallOwner.getId())) {
                    comment = commentDao.create(new Comment(currentUser, message, requestedPost));
                    break;
                } else {
                    throw new AuthorizationErrorException("Only owner can write comments on this wall!");
                }
            }
        }
        return comment;
    }


    /**
     * @param userSender пользоватаель, желающий поставить комментарию лайк
     * @param comment    выбранный комментарий
     * @param wallOwner  владелец стены
     * @return созданный лайк
     * @throws LikeRevokedException        если лайк уже стоит, то при повторном выборе от отнимается
     * @throws AuthorizationErrorException нет доступа выбранной стене
     */

    @Override
    public Like addWallCommentLike(User userSender, Comment comment, User wallOwner) {
        if (!wallOwner.isOpenUser() && !friendRequestDao.isFriendOf(userSender.getId(), wallOwner.getId()) && !userSender.getId().equals(wallOwner.getId())) {
            throw new AuthorizationErrorException("You are not friend of  " + wallOwner.getUsername() + " and don't have access to his wall!");
        }
        Like foundLikeId = likeDao.getCommentLikeByUserId(comment.getId(), userSender.getId());
        if (foundLikeId != null) {
            likeDao.deleteById(foundLikeId.getId());
            return null;
        }
        Like commentLike = new Like(userDao.getById(userSender.getId()), comment);
        likeDao.create(commentLike);
        return commentLike;
    }

    /**
     * @param currentUser текущий пользователь
     * @param wallOwner   владелец стены
     * @param postId      id выбранного поста
     * @return статус действия
     * @throws AuthorizationErrorException нет доступа выбранной стене
     */

    @Override
    public String deleteUserWallPost(User currentUser, User wallOwner, Long postId) {
        Post post = defaultDao.getById(postId);
        if (currentUser.getRole().getName().equals(RoleType.ADMIN)) {
            log.info("Admin action: Post with id " + postId + " deleted successfully");
            defaultDao.deleteById(postId);
            return "Admin action: Post with id " + postId + " deleted successfully";
        }
        if (post.getAuthor().getId().equals(currentUser.getId()) || currentUser.getId().equals(wallOwner.getId())) {
            defaultDao.deleteById(postId);
            return "Post deleted successfully";
        } else {
            throw new AuthorizationErrorException("Only owner of the wall, author or global admins can delete this post it!");
        }
    }

    @Override
    public void deleteCommentById(Long commentId) {
        commentDao.deleteById(commentId);
    }
}
