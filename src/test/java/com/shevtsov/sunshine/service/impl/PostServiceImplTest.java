package com.shevtsov.sunshine.service.impl;

import com.shevtsov.sunshine.dao.entities.Comment;
import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dao.entities.Wall;
import com.shevtsov.sunshine.common.GroupRole;
import com.shevtsov.sunshine.common.GroupWallType;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostServiceImplTest extends AbstractServiceImplTest {
    @InjectMocks
    private PostServiceImpl postService;

    @BeforeAll
    void setAdmins() {
        addGroupMembership(2L, userMap.get(2L), groupMap.get(2L), GroupRole.OWNER);
        addGroupMembership(3L, userMap.get(3L), groupMap.get(3L), GroupRole.OWNER);
        addGroupMembership(4L, userMap.get(4L), groupMap.get(4L), GroupRole.OWNER);
        addGroupMembership(5L, userMap.get(5L), groupMap.get(5L), GroupRole.OWNER);

    }

    @Test
    @Order(1)
    void addGroupPost_WhenAddingPostToGroupWithWalltypeAll_ThenSuccess() {
        User user = getUserDao().getById(2L);
        Group group = getGroupDao().getById(1L);
        Long postId = (long) getPostMap().size() + 1;
        assertNull(getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(user.getId(), group.getId()));
        assertEquals(postService.addGroupPost(user, group, "This test was written at 2:28 am"), getPostDao().getById(postId));
    }

    @Test
    @Order(2)
    void addGroupPost_WhenAddingPostToGroupWithWalltypeOfferedPosts_ThenSuccess() {
        User user = getUserDao().getById(1L);
        Group group = getGroupDao().getById(2L);
        Long postId = (long) getPostMap().size() + 1;
        assertTrue(group.getOpenToJoin());
        assertNull(getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(user.getId(), group.getId()));
        assertEquals(postService.addGroupPost(user, group, "This test was written at 12:51 pm"), getPostDao().getById(postId));
        assertFalse(getPostDao().getById(postId).getIsPublished());
    }


    @Test
    @Order(3)
    void addGroupPost_WhenAddingPostToGroupWithWalltypeSubscribersNotSubscribed_ThenThrowAuthorizationErrorException() {
        User user = getUserDao().getById(6L);
        Group group = getGroupDao().getById(3L);
        assertNull(getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(user.getId(), group.getId()));
        assertThrows(AuthorizationErrorException.class, () -> postService.addGroupPost(user, group, "This test was written at 12:51 pm"));
    }

    @Test
    @Order(4)
    void addGroupPost_WhenAddingPostToGroupWithWalltypeSubscribersNotSubscriber_ThenThrowAuthorizationErrorException() {
        User user = getUserDao().getById(6L);
        Long id = (long) groupMembershipMap.size() + 1;
        Group group = getGroupDao().getById(3L);
        addGroupMembership(id, user, group, GroupRole.AWAITING_CHECK);
        assertNotNull(getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(user.getId(), group.getId()));
        assertThrows(AuthorizationErrorException.class, () -> postService.addGroupPost(user, group, "This test was written at 12:51 pm"));
    }

    @Test
    @Order(5)
    void addGroupPost_WhenAddingPostToGroupWithWalltypeOwnerNotOwner_ThenAuthorizationErrorException() {
        User user = getUserDao().getById(1L);
        Long id = (long) groupMembershipMap.size() + 1;
        Group group = getGroupDao().getById(6L);
        addGroupMembership(id, user, group, GroupRole.SUBSCRIBER);
        assertNotNull(getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(user.getId(), group.getId()));
        assertThrows(AuthorizationErrorException.class, () -> postService.addGroupPost(user, group, "This test was written at 12:51 pm"));
    }

    @Test
    @Order(6)
    void dealWithOfferedPost_WhenPostIsNotInPregloghka_ThenThrowEntityNotFoundException() {
        Post post = getPostDao().getById(1L);
        assertEquals(post.getIsPublished(), true);
        assertThrows(EntityNotFoundException.class, () -> postService.dealWithOfferedPost(post.getId(), true));
    }

    @Test
    @Order(7)
    void dealWithOfferedPost_WhenPostIsInPregloghka_ThenSuccess() {
        User admin = getUserDao().getById(2L);
        Group group = getGroupDao().getById(2L);
        postService.dealWithOfferedPost(2L, true);
        assertEquals(group.getId(), getPostDao().getById(2L).getGroup().getId());
        assertEquals(group.getWallType(), GroupWallType.OFFERED_POSTS);
        assertTrue(getPostDao().getById(2L).getIsPublished());

    }

    @Test
    @Order(8)
    void addPostLike_WhenLikingPostNotSubscriberClosedGroup_ThenThrowsAuthorizationErrorException() {
        User currentUser = getUserDao().getById(1L);
        Group group = groupMap.get(2L);
        group.setOpenToJoin(false);
        assertThrows(AuthorizationErrorException.class, () -> postService.addPostLike(currentUser, getPostDao().getById(2L)));

    }

    @Test
    @Order(9)
    void addPostLike_WhenLikingPostForTheFirstTime_ThenSuccess() {
        User currentUser = getUserDao().getById(1L);
        assertNotNull(postService.addPostLike(currentUser, getPostDao().getById(1L)));
    }

    @Test
    @Order(10)
    void addPostLike_WhenLikingPostForTheSecondTime_ThenReturnNull() {
        User currentUser = getUserDao().getById(1L);
        assertNull(postService.addPostLike(currentUser, getPostDao().getById(1L)));
    }

    @Test
    @Order(11)
    void addGroupPostComment_WhenCommentingNotAuthorized_ThenThrowsAuthorizationErrorException() {
        User currentUser = getUserDao().getById(1L);
        assertThrows(AuthorizationErrorException.class, () -> postService.addGroupPost(currentUser, getGroupDao().getById(6L), "OGON"));
    }

    @Test
    @Order(12)
    void addGroupPostComment_WhenCommentingAuthorized_ThenSuccess() {
        User currentUser = getUserDao().getById(6L);
        assertThrows(AuthorizationErrorException.class, () -> postService.addGroupPost(currentUser, getGroupDao().getById(6L), "OGON"));
    }

    @Test
    @Order(13)
    void addGroupCommentLike_WhenDataCorrect_ThenSuccess() {
        User user = getUserDao().getById(2L);
        Group group = getGroupDao().getById(2L);
        Post post = getPostDao().getById(2L);
        GroupMembership groupMembership = getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(user.getId(), group.getId());
        postService.addGroupPostComment(user, "roflan", post, group, groupMembership);
        assertNotNull(postService.addGroupCommentLike(user.getId(), commentMap.get((long) commentMap.size()), group));

    }


    @Test
    @Order(14)
    void addGroupCommentLike_WhenLikeWasAlreadySent_ThenReturnNull() {
        User user = getUserDao().getById(2L);
        Group group = getGroupDao().getById(2L);
        assertNull(postService.addGroupCommentLike(user.getId(), commentMap.get((long) commentMap.size()), group));
    }


    @Test
    @Order(15)
    void addGroupCommentLike_WhenGroupIsNotOpenToJoin_ThenThrowAuthorizationErrorException() {
        User user = getUserDao().getById(6L);
        Group group = getGroupDao().getById(2L);
        assertThrows(AuthorizationErrorException.class, () -> postService.addGroupCommentLike(user.getId(), commentMap.get((long) commentMap.size()), group));
    }


    @Test
    @Order(16)
    void addWallPost_WhenNotAllowedToPost_ThenThrowAuthorizationErrorException() {
        User user = getUserDao().getById(3L);
        User wallOwner = getUserDao().getById(1L);
        assertThrows(AuthorizationErrorException.class, () -> postService.addWallPost(user, wallOwner, "Comment fail"));
    }

    @Test
    @Order(17)
    void addWallPost_WhenOwnerCommentsOwnWallPost_ThenSuccess() {
        User user = getUserDao().getById(6L);
        User wallOwner = getUserDao().getById(6L);
        assertNotNull(postService.addWallPost(user, wallOwner, "Comment success"));
    }

    @Test
    @Order(18)
    void addWallPostComment_WhenNotAllowedToComment_ThenThrowAuthorizationErrorException() {
        User user = getUserDao().getById(3L);
        User wallOwner = getUserDao().getById(1L);
        Post post = postMap.get((long) postMap.size());
        assertThrows(AuthorizationErrorException.class, () -> postService.addWallPostComment(user, wallOwner, post, "Comment fail"));
    }

    @Test
    @Order(19)
    void addWallPostComment_WhenCommentingAllowed_ThenSuccess() {
        User user = getUserDao().getById(6L);
        User wallOwner = getUserDao().getById(6L);
        Post post = postMap.get((long) postMap.size());
        assertNotNull(postService.addWallPostComment(user, wallOwner, post, "Comment success"));
    }


    @Test
    @Order(20)
    void addWallCommentLike_WhenNotAllowedToViewWall_ThenThrowAuthorizationErrorException() {
        User user = getUserDao().getById(3L);
        User wallOwner = getUserDao().getById(5L);
        Comment comment = commentMap.get((long) commentMap.size());
        assertThrows(AuthorizationErrorException.class, () -> postService.addWallCommentLike(user, comment, wallOwner));
    }

    @Test
    @Order(21)
    void addWallCommentLike_WhenGotAccessFirstTimeLike_ThenSuccess() {
        User user = getUserDao().getById(1L);
        User wallOwner = getUserDao().getById(1L);
        Comment comment = commentMap.get((long) commentMap.size());
        assertNotNull(postService.addWallCommentLike(user, comment, wallOwner));
    }

    @Test
    @Order(22)
    void addWallCommentLike_WhenLikedForSecondTime_ThenReturnNull() {
        User user = getUserDao().getById(1L);
        User wallOwner = getUserDao().getById(1L);
        Comment comment = commentMap.get((long) commentMap.size());
        assertNull(postService.addWallCommentLike(user, comment, wallOwner));
    }

    @Test
    @Order(23)
    void deleteUserWallPost_WhenUserHasNoRightsToDeletePost_ThenThrowAuthorizationErrorException() {
        User user = getUserDao().getById(4L);
        User wallOwner = getUserDao().getById(1L);
        assertThrows(AuthorizationErrorException.class, () -> postService.deleteUserWallPost(user, wallOwner, 1L));
    }

    @Test
    @Order(24)
    void deleteUserWallPost_WhenUserHasRightsToDeletePost_ThenSuccess() {
        User user = getUserDao().getById(2L);
        User wallOwner = getUserDao().getById(1L);
        assertNotNull(postService.deleteUserWallPost(user, wallOwner, 1L));
    }


    @Test
    @Order(25)
    void addGroupPost_WhenOwnerToGroup_ThenSuccess() {
        Group group = groupMap.get(5L);
        User user = userMap.get(5L);
        assertNotNull(postService.addGroupPost(user, group, "Text!"));
    }

    @Test
    @Order(26)
    void addGroupPostComment_WhenOwnerToGroup_ThenSuccess() {
        Group group = groupMap.get(5L);
        User user = userMap.get(4L);
        Post post = postMap.get((long) postMap.size());
        GroupMembership groupMembership = getGroupMembershipDao().getGroupMembershipByUserAndGroupIds(user.getId(), group.getId());
        assertNotNull(postService.addGroupPostComment(user, "test!", post, group, groupMembership));
    }

    @Test
    @Order(27)
    void deleteUserWallPost_WhenGlobalAdmin_ThenSuccess() {
        User user = getUserDao().getById(1L);
        Map posts = postMap;
        Wall wall = wallMap.get(6L);
        assertNotNull(postService.deleteUserWallPost(user,  null, 2L));
    }

}