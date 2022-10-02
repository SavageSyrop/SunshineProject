package com.shevtsov.sunshine.controller;

import com.shevtsov.sunshine.common.GroupRole;
import com.shevtsov.sunshine.common.RoleType;
import com.shevtsov.sunshine.dao.entities.Comment;
import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import com.shevtsov.sunshine.dao.entities.Like;
import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.dao.entities.ResponseMessage;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dto.CommentDto;
import com.shevtsov.sunshine.dto.GroupDto;
import com.shevtsov.sunshine.dto.GroupMembershipDto;
import com.shevtsov.sunshine.dto.GroupPostDto;
import com.shevtsov.sunshine.dto.LikeDto;
import com.shevtsov.sunshine.dto.mappers.CommentMapper;
import com.shevtsov.sunshine.dto.mappers.GroupMapper;
import com.shevtsov.sunshine.dto.mappers.GroupMembershipMapper;
import com.shevtsov.sunshine.dto.mappers.GroupPostMapper;
import com.shevtsov.sunshine.dto.mappers.LikeMapper;
import com.shevtsov.sunshine.dto.mappers.WallPostMapper;
import com.shevtsov.sunshine.exceptions.ActionAlreadyCompletedException;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.InvalidActionException;
import com.shevtsov.sunshine.exceptions.LikeRevokedException;
import com.shevtsov.sunshine.service.GroupService;
import com.shevtsov.sunshine.service.PostService;
import com.shevtsov.sunshine.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * Контроллер, обрабатывающий запросы связанные с запросами дружбы
 */

@Slf4j
@RestController
@RequestMapping("")
public class GroupController {

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private PostService postService;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMembershipMapper groupMembershipMapper;

    @Autowired
    private WallPostMapper postMapper;

    @Autowired
    private GroupPostMapper groupPostMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private CommentMapper commentMapper;

    /**
     * @param groupId id группы, информацию о который нужно получить
     * @return информация о группе
     */

    @GetMapping("/group{groupId}")
    @PreAuthorize("hasAuthority('WRITING')")
    public GroupDto getGroup(@PathVariable Long groupId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Group group = groupService.getById(groupId);

        if (!currentUserCanReadWall(groupId)) {
            throw new AuthorizationErrorException("You are not a member of group with id " + groupId);
        }
        return groupMapper.toDto(group);
    }

    /**
     * @param groupDto параметры группы
     * @return информация о группе
     */

    @PostMapping("/group")
    @PreAuthorize("hasAuthority('WRITING')")
    public GroupDto createGroup(@RequestBody GroupDto groupDto) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Group group = groupMapper.toEntity(groupDto);
        return groupMapper.toDto(groupService.addGroup(currentUser, group));
    }

    /**
     * @param groupDto - обновленные параметры группы
     * @param groupId  - id группы, чья информацию должна быть обновлена
     * @return обновленная информация о группе
     */

    @PatchMapping("/group{groupId}")
    @PreAuthorize("hasAuthority('WRITING')")
    public GroupDto editGroup(@RequestBody GroupDto groupDto, @PathVariable Long groupId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Group newGroupInfo = groupMapper.toEntity(groupDto);
        newGroupInfo.setId(groupId);
        newGroupInfo = groupService.editGroup(currentUser, newGroupInfo);
        return groupMapper.toDto(newGroupInfo);
    }

    /**
     * @param groupId id группы, на которую подписываются
     * @return информация о созданном членстве в групппе
     */

    @PostMapping("/group{groupId}/subscribe")
    @PreAuthorize("hasAuthority('WRITING')")
    public GroupMembershipDto subscribeToGroup(@PathVariable Long groupId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Group group = groupService.getById(groupId);
        GroupMembership groupMembership = groupService.subscribeToGroup(currentUser, group);
        return groupMembershipMapper.toDto(groupMembership);
    }

    /**
     * @param groupId id группы, от которой отписываются
     * @return статус действия
     */

    @DeleteMapping("/group{groupId}/unsubscribe")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage unsubscribeFromGroup(@PathVariable Long groupId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Group group = groupService.getById(groupId);
        groupService.unsubscribeFromGroup(currentUser, group);
        return new ResponseMessage("You have successfully unsubscribed from " + group.getName());
    }

    /**
     * @param groupId      id группы, с которой происходит взаимодействия
     * @param subscriberId id пользователя, которому выдается роль
     * @param groupRole    выдаваемая роль
     * @return новая информация о членстве пользователя в группе
     * @throws AuthorizationErrorException текущий пользователь не состоит в чате
     * @throws EntityNotFoundException     пользователь, которому выдается роль, не подписчик группы
     */

    @PatchMapping("/group{groupId}/grant_role")
    @PreAuthorize("hasAuthority('WRITING')")
    public GroupMembershipDto grantRoleTo(@PathVariable Long groupId, @RequestParam Long subscriberId, @RequestParam GroupRole groupRole) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        GroupMembership currentUserMembership = groupService.getGroupMembershipByUserAndGroupIds(currentUser.getId(), groupId);

        if (currentUserMembership == null) {
            throw new AuthorizationErrorException("You are not a member of group with id " + groupId);
        }

        GroupMembership subscriberMembership = groupService.getGroupMembershipByUserAndGroupIds(subscriberId, groupId);

        if (subscriberMembership == null) {
            throw new EntityNotFoundException("User with id " + subscriberId + " is not subscribed");
        }

        subscriberMembership = groupService.grantRoleTo(currentUserMembership, subscriberMembership, groupRole);
        return groupMembershipMapper.toDto(subscriberMembership);
    }

    /**
     * @param groupId id группы, которую нужно удалить
     * @return статус действия
     * @throws EntityNotFoundException     текущий пользователь не участник чата
     * @throws AuthorizationErrorException только владелец группы может удалить её
     */

    @DeleteMapping("/group{groupId}")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage deleteGroup(@PathVariable Long groupId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        GroupMembership currentUserMembership = groupService.getGroupMembershipByUserAndGroupIds(currentUser.getId(), groupId);
        Group group = groupService.getById(groupId);

        if (currentUser.getRole().getName().equals(RoleType.ADMIN)) {
            groupService.deleteById(groupId);
            log.info("Admin action: " + group.getName() + " was successfully deleted");
            return new ResponseMessage("Admin action: " + group.getName() + " was successfully deleted");
        }

        if (currentUserMembership == null) {
            throw new EntityNotFoundException("You are not a member of group with id " + groupId);
        }

        if (currentUserMembership.getGroupRole() != GroupRole.OWNER) {
            throw new AuthorizationErrorException("Only owner of this group can delete it!");
        }

        groupService.deleteById(groupId);
        return new ResponseMessage("Group " + group.getName() + " was successfully deleted");
    }

    /**
     * @param groupId id группы, из которой нужно получить запросы на подписку
     * @return информация о полученных запросах на подписку
     * @throws AuthorizationErrorException текущий пользователь не админ или владелец группы
     */

    @GetMapping("/group{groupId}/sub_requests")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<GroupMembershipDto> getSubRequests(@PathVariable Long groupId) {
        if (currentUserHasAdminRightsInGroup(groupId)) {
            List<GroupMembership> subRequests = groupService.getSubscribeRequests(groupId);
            return groupMembershipMapper.toListDto(subRequests);
        } else {
            throw new AuthorizationErrorException("You are not ADMIN or OWNER");
        }
    }

    /**
     * @param groupId      id группы, с которой происходит взаимодействие
     * @param subRequestId id запроса о вступлении в группу, который будет принят
     * @return информация о созданном членстве
     * @throws AuthorizationErrorException     если текущий пользователь не админ и не владелец
     * @throws EntityNotFoundException         если запроса на подписку не существует
     * @throws ActionAlreadyCompletedException если id уже принятого запроса
     */

    @PatchMapping("/group{groupId}/sub_requests/accept")
    @PreAuthorize("hasAuthority('WRITING')")
    public GroupMembershipDto acceptSubRequest(@PathVariable Long groupId, @RequestParam Long subRequestId) {
        if (currentUserHasAdminRightsInGroup(groupId)) {
            GroupMembership groupMembership = groupService.getGroupMembershipById(subRequestId);
            if (groupMembership == null) {
                throw new EntityNotFoundException("No group membership with id " + subRequestId + " was found");
            }
            if (groupMembership.getGroupRole() != GroupRole.AWAITING_CHECK) {
                throw new ActionAlreadyCompletedException("This request is already accepted!");
            }
            groupMembership.setGroupRole(GroupRole.SUBSCRIBER);
            groupService.updateMembership(groupMembership);
            return groupMembershipMapper.toDto(groupMembership);
        } else {
            throw new AuthorizationErrorException("You are not ADMIN or OWNER");
        }
    }

    /**
     * @param groupId      id группы, с которой происходит взаимодействие
     * @param subRequestId id запроса о вступлении в группу, который будет отклонён
     * @return статус действия
     * @throws AuthorizationErrorException     если текущий пользователь не админ и не владелец
     * @throws EntityNotFoundException         если запроса на подписку не существует
     * @throws ActionAlreadyCompletedException если id уже принятого запроса
     */

    @DeleteMapping("/group{groupId}/sub_requests/decline")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage declineSubRequest(@PathVariable Long groupId, @RequestParam Long subRequestId) {
        if (currentUserHasAdminRightsInGroup(groupId)) {
            GroupMembership groupMembership = groupService.getGroupMembershipById(subRequestId);
            if (groupMembership == null) {
                throw new EntityNotFoundException("No group membership with id " + subRequestId + " was found");
            }
            if (groupMembership.getGroupRole() != GroupRole.AWAITING_CHECK) {
                throw new ActionAlreadyCompletedException("This request is already accepted!");
            }
            groupService.deleteMembershipById(subRequestId);
            return new ResponseMessage("Subscribe request successfully declined!");
        } else {
            throw new AuthorizationErrorException("You are not ADMIN or OWNER");
        }
    }

    /**
     * @param groupId id группы, информацию владельца которой нужно получить
     * @return информация о членстве владельца
     * @throws AuthorizationErrorException текущий пользователь не имеет доступа к стене
     */

    @GetMapping("/group{groupId}/owner")
    @PreAuthorize("hasAuthority('WRITING')")
    public GroupMembershipDto getGroupOwner(@PathVariable Long groupId) {
        Group group = groupService.getById(groupId);
        if (!currentUserCanReadWall(groupId)) {
            throw new AuthorizationErrorException("You are not authorized to see group owner!");
        }
        return groupMembershipMapper.toDto(groupService.getGroupOwnerMembership(groupId));
    }

    /**
     * @param groupId id группы, стену которой нужно получить
     * @return информация о постах в группе
     * @throws AuthorizationErrorException если у текущего пользователя нет доступа к стене
     */

    //                                  WALL
    @GetMapping("/group{groupId}/wall")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<GroupPostDto> getWall(@PathVariable Long groupId) {
        Group group = groupService.getById(groupId);
        if (currentUserCanReadWall(groupId)) {
            return groupPostMapper.toListDto(groupService.getGroupPublishedPosts(groupId));
        } else {
            throw new AuthorizationErrorException("Group wall is only for subscribers!");
        }
    }

    /**
     * @param groupId id группы, на стену которой нужно добавить пост
     * @param text    текст поста
     * @return информация о созданном посте
     */

    @PostMapping("/group{groupId}/wall/post")
    @PreAuthorize("hasAuthority('WRITING')")
    public GroupPostDto addPost(@PathVariable Long groupId, @RequestBody String text) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Group group = groupService.getById(groupId);
        Post groupPost = postService.addGroupPost(currentUser, group, text);
        return groupPostMapper.toDto(groupPost);
    }

    /**
     * @param groupId id группы, где находится запрашиваемый пост
     * @param postId  id поста, который нужно получить
     * @return информация о посте
     * @throws AuthorizationErrorException когда у текущего пользователя нет доступа к стене
     */

    @GetMapping("/group{groupId}/wall/post")
    @PreAuthorize("hasAuthority('WRITING')")
    public GroupPostDto getGroupPost(@PathVariable Long groupId, @RequestParam Long postId) {
        if (currentUserCanReadWall(groupId)) {
            Post post = postService.getById(postId);
            if (post.getIsPublished()) {
                return groupPostMapper.toDto(postService.getById(postId));
            } else {
                if (currentUserHasAdminRightsInGroup(groupId)) {
                    return groupPostMapper.toDto(postService.getById(postId));
                } else {
                    throw new AuthorizationErrorException("Only admins and owner have access to offered posts.");
                }
            }
        } else {
            throw new AuthorizationErrorException("Group wall is only for subscribers!");
        }

    }

    /**
     * @param groupId id группы, из которой нужно удать пост
     * @param postId  id удаляемого поста
     * @return статус действия
     * @throws AuthorizationErrorException у текущего пользователя нет доступа к предложенным новостям группы
     */

    @DeleteMapping("/group{groupId}/wall/post")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage deletePost(@PathVariable Long groupId, @RequestParam Long postId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Post post = postService.getById(postId);

        if (!post.getIsPublished() && !currentUserHasAdminRightsInGroup(groupId)) {
            throw new AuthorizationErrorException("Only admins have access to offered posts!");
        }

        if (currentUser.getId().equals(post.getAuthor().getId()) || currentUserHasAdminRightsInGroup(groupId)) {
            if (!post.getIsPublished()) {
                postService.deleteById(postId);
                return new ResponseMessage("You have deleted post from offered posts");
            } else {
                postService.deleteById(postId);
                return new ResponseMessage("Post with id " + postId + " deleted successfully");
            }
        } else {
            throw new AuthorizationErrorException("Only author or admins can delete posts.");
        }
    }

    /**
     * @param groupId id группы, предложенные новости из которой нужно получить
     * @return информация о постах, ожидающих проверки
     * @throws AuthorizationErrorException у текущего пользователя нет доступа к предложке
     */

    @GetMapping("/group{groupId}/wall/offered_posts")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<GroupPostDto> getOfferedPosts(@PathVariable Long groupId) {
        if (currentUserHasAdminRightsInGroup(groupId)) {
            Group group = groupService.getById(groupId);
            List<Post> offered_posts = groupService.getGroupOfferedPosts(group.getId());
            return groupPostMapper.toListDto(offered_posts);
        } else {
            throw new AuthorizationErrorException("Only admins and owner have access to offered posts.");
        }
    }

    /**
     * @param groupId id группы, предложенный пост в которой нужно опубликовать
     * @param postId  id поста, который нужно опубликовать
     * @return инфомация об опубликованном посте
     * @throws AuthorizationErrorException текущий пользователь не имеет доступа к предложке
     */

    @PatchMapping("/group{groupId}/wall/offered_posts/accept")
    // Объединить в один метод с declinePost невозможно, разные типы возвратов (вернуть null неиформативно)
    @PreAuthorize("hasAuthority('WRITING')")
    public GroupPostDto acceptOfferedPost(@PathVariable Long groupId, @RequestParam Long postId) {
        if (currentUserHasAdminRightsInGroup(groupId)) {
            Post post = postService.dealWithOfferedPost(postId, true);
            return groupPostMapper.toDto(post);
        } else {
            throw new AuthorizationErrorException("Only admins and owner have access to offered posts.");
        }
    }

    /**
     * @param groupId id группы, предложенный пост в которой нужно отклонить
     * @param postId  id поста, который нужно отклонить
     * @return статус действия
     * @throws AuthorizationErrorException текущий пользователь не имеет доступа к предложке
     */

    @DeleteMapping("/group{groupId}/wall/offered_posts/decline")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage declineOfferedPost(@PathVariable Long groupId, @RequestParam Long postId) {
        if (currentUserHasAdminRightsInGroup(groupId)) {
            postService.dealWithOfferedPost(postId, false);
        } else {
            throw new AuthorizationErrorException("Only admins and owner have access to offered posts.");
        }
        return new ResponseMessage("Post declined from offered posts");
    }

    /**
     * @param groupId id группы, с которой происходит взаимодейстие
     * @param postId  id поста, лайки которого нужно получить
     * @return список информации о поставленных посту лайков
     * @throws AuthorizationErrorException текущий пользователь пытается лайкнуть пост из предложки
     * @throws AuthorizationErrorException текущий пользователь не имеет доступа к стене группы
     */


    @GetMapping("/group{groupId}/wall/post{postId}/likes")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<LikeDto> getLikesFromPost(@PathVariable Long groupId, @PathVariable Long postId) {
        Post requestedPost = postService.getById(postId);

        if (!requestedPost.getIsPublished()) {
            throw new InvalidActionException("It's post from offered posts, such posts aren't published and have no likes");
        }

        if (currentUserCanReadWall(groupId)) {
            return likeMapper.toListDto(requestedPost.getPostLikes());
        } else {
            throw new AuthorizationErrorException("Group wall is only for subscribers!");
        }
    }

    /**
     * @param groupId id группы, с которой происходит взаимодействие
     * @param postId  id поста, которому ставится лайк
     * @return информация о поставленном лайке
     * @throws AuthorizationErrorException текущий пользователь не имеет доступа к стене группы
     */


    @PostMapping("/group{groupId}/wall/post{postId}/likes")
    @PreAuthorize("hasAuthority('WRITING')")
    public LikeDto likePost(@PathVariable Long groupId, @PathVariable Long postId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Post post = postService.getById(postId);
        if (currentUserCanReadWall(groupId)) {
            Like postLike = postService.addPostLike(currentUser, post);
            if (postLike == null) {
                throw new LikeRevokedException();
            }
            return likeMapper.toDto(postLike);
        } else {
            throw new AuthorizationErrorException("Group wall is only for subscribers!");
        }
    }

    /**
     * @param groupId id группы, с которой происходит взаимодействие
     * @param postId  id поста, комментарии которого нужно получить
     * @return список информации о комментариях к посту
     * @throws InvalidActionException      попытка получить комментарии поста из предложки
     * @throws AuthorizationErrorException текущий пользователь не имеет доступа к стене группы
     */

    @GetMapping("/group{groupId}/wall/post{postId}/comments")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<CommentDto> getCommentsFromPost(@PathVariable Long groupId, @PathVariable Long postId) {
        Post requestedPost = postService.getById(postId);
        if (!requestedPost.getIsPublished()) {
            throw new InvalidActionException("It's post from offered posts, such posts aren't published and can't be commented");
        }
        if (currentUserCanReadWall(groupId)) {
            return commentMapper.toListDto(requestedPost.getComments());
        } else {
            throw new AuthorizationErrorException("Group wall is only for subscribers!");
        }
    }

    /**
     * @param groupId id группы, с которой происходит взаимодействие
     * @param postId  id поста, который нужно прокомментировать
     * @param message текст комментария
     * @return информация об созданном комментарии
     * @throws InvalidActionException попытка прокомментировать пост из предложки
     */

    @PostMapping("/group{groupId}/wall/post{postId}/comments")
    @PreAuthorize("hasAuthority('WRITING')")
    public CommentDto commentPost(@PathVariable Long groupId, @PathVariable Long postId, @RequestBody String message) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Post post = postService.getById(postId);
        if (!post.getIsPublished()) {
            throw new InvalidActionException("It's post from offered posts, such posts aren't published and can't be commented");
        }
        if (!currentUserCanReadWall(groupId)) {
            throw new AuthorizationErrorException("Group wall is only for subscribers!");
        }
        Group group = groupService.getById(groupId);
        Comment newComment = postService.addGroupPostComment(currentUser, message, post, group, groupService.getGroupMembershipByUserAndGroupIds(currentUser.getId(), groupId));
        return commentMapper.toDto(newComment);
    }

    /**
     * @param groupId   id группы, с которой происходит взаимодействие
     * @param postId    id поста, комментарий с которой нужно удалить
     * @param commentId id комментарий, который нужно удалить
     * @return статус действия
     * @throws AuthorizationErrorException если у текущего пользователя нет доступа к удалению комментария
     */

    @DeleteMapping("/group{groupId}/wall/post{postId}/comments")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage deleteCommentToPost(@PathVariable Long groupId, @PathVariable Long postId, @RequestParam Long commentId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Comment comment = postService.getCommentById(commentId);

        if (currentUser.getRole().getName().equals(RoleType.ADMIN)) {
            log.info("Admin action: Comment with id " + commentId + " successfully deleted");
            postService.deleteCommentById(commentId);
            return new ResponseMessage("Admin action: Comment with id " + commentId + " successfully deleted");
        }

        if (currentUser.getId().equals(comment.getAuthor().getId()) || currentUserHasAdminRightsInGroup(groupId)) {
            postService.deleteCommentById(commentId);
        } else {
            throw new AuthorizationErrorException("Only author, public admins/owner or global admins can delete comments!");
        }
        return new ResponseMessage("Comment successfully deleted");
    }

    /**
     * @param groupId   id группы, с которой происходит взаимодействие
     * @param postId    id поста, с которым происходит взаимодействие
     * @param commentId id комментария, лайки с которого нужно получить
     * @return список информации о лайках на комментарии
     * @throws AuthorizationErrorException если у текущего пользователя нет доступа к стене
     */

    @GetMapping("/group{groupId}/wall/post{postId}/comment{commentId}/likes")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<LikeDto> getLikesFromComment(@PathVariable Long groupId, @PathVariable Long postId, @PathVariable Long commentId) {
        if (currentUserCanReadWall(groupId)) {
            Post post = postService.getById(postId);
            Comment comment = postService.getCommentById(commentId);
            return likeMapper.toListDto(comment.getCommentLikes());
        } else {
            throw new AuthorizationErrorException("Group wall is only for subscribers!");
        }
    }

    /**
     * @param groupId   id группы, с которой происходит взаимодействие
     * @param postId    id поста, с которым происходит взаимодействие
     * @param commentId id комментария, лайк на который нужно поставить
     * @return информация о поставленном лайке
     * @throws AuthorizationErrorException если у текущего пользователя нет доступа к стене
     */


    @PostMapping("/group{groupId}/wall/post{postId}/comment{commentId}/likes")
    @PreAuthorize("hasAuthority('WRITING')")
    public LikeDto likeCommentAtPost(@PathVariable Long groupId, @PathVariable Long postId, @PathVariable Long commentId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        if (currentUserCanReadWall(groupId)) {
            Post post = postService.getById(postId);
            Comment comment = postService.getCommentById(commentId);
            Group group = groupService.getById(groupId);
            Like commentLike = postService.addGroupCommentLike(currentUser.getId(), comment, group);
            if (commentLike == null) {
                throw new LikeRevokedException();
            }
            return likeMapper.toDto(commentLike);
        } else {
            throw new AuthorizationErrorException("Group wall is only for subscribers!");
        }
    }


    /**
     * @param id id пользователя, группы которого нужно получить
     * @return список информации о группах пользователя
     * @throws InvalidActionException если у запрашиваемого пользователя закрытый профиль, а текущий пользователь не его друг
     */


    @GetMapping("/id{id}/groups")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<GroupMembershipDto> getUserGroups(@PathVariable Long id) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User user = userService.getById(id);
        if (!user.isOpenUser() && !userService.isFriendOf(currentUser.getId(), user.getId()) && !currentUser.getId().equals(id)) {
            throw new InvalidActionException("This user has private profile! You can't see his groups");
        } else {
            return groupMembershipMapper.toListDto(groupService.getGroupMemberships(id));
        }
    }

    /**
     * @return список информации о группах пользователя
     */

    @GetMapping("/groups")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<GroupMembershipDto> getSubedGroups() {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        return groupMembershipMapper.toListDto(groupService.getGroupMemberships(currentUser.getId()));
    }

    /**
     * @param groupId id группы, права администратора в которой нужно проверить у текущего пользователя
     * @return логическое значение наличия прав администратора в группе
     * @throws AuthorizationErrorException если текущий пользователь не участник группы
     */

    private Boolean currentUserHasAdminRightsInGroup(Long groupId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        GroupMembership currentMembership = groupService.getGroupMembershipByUserAndGroupIds(currentUser.getId(), groupId);
        if (currentMembership == null) {
            throw new AuthorizationErrorException("You are not member of group with id " + groupId);
        }
        return (currentMembership.getGroupRole() == GroupRole.ADMIN || currentMembership.getGroupRole() == GroupRole.OWNER);
    }

    /**
     * @param groupId id группы, членство в которой нужно проверить у текущего пользователя
     * @return логическое значение членства в группе
     * @throws AuthorizationErrorException если текущий пользователь не участник группы
     */

    private Boolean currentUserIsMemberOfGroup(Long groupId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        GroupMembership currentMembership = groupService.getGroupMembershipByUserAndGroupIds(currentUser.getId(), groupId);
        if (currentMembership == null) {
            throw new AuthorizationErrorException("You are not member of group with id " + groupId);
        }
        return currentMembership.getGroupRole() != GroupRole.AWAITING_CHECK;
    }

    /**
     * @param groupId id группы, разрешение на просмотр стены в которой нужно проверить у текущего пользователя
     * @return логическое значение наличия доступа к стене
     */

    private Boolean currentUserCanReadWall(Long groupId) {
        Group group = groupService.getById(groupId);
        if (!group.getOpenToJoin()) {
            return currentUserIsMemberOfGroup(groupId);
        } else {
            return true;
        }
    }

    private String getAuthenticationName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
