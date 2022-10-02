package com.shevtsov.sunshine.controller;

import com.shevtsov.sunshine.common.RoleType;
import com.shevtsov.sunshine.common.UserWallPermissionType;
import com.shevtsov.sunshine.dao.entities.Comment;
import com.shevtsov.sunshine.dao.entities.Like;
import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.dao.entities.ResponseMessage;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dto.CommentDto;
import com.shevtsov.sunshine.dto.LikeDto;
import com.shevtsov.sunshine.dto.WallPostDto;
import com.shevtsov.sunshine.dto.mappers.CommentMapper;
import com.shevtsov.sunshine.dto.mappers.LikeMapper;
import com.shevtsov.sunshine.dto.mappers.WallPostMapper;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.LikeRevokedException;
import com.shevtsov.sunshine.service.PostService;
import com.shevtsov.sunshine.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер, обрабатывающий запросы связанные с добавлением постов, комментариев, лайков
 */

@Slf4j
@RestController
@RequestMapping("/")
public class WallController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private WallPostMapper postMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private CommentMapper commentMapper;

    /**
     * @return информация о постах на собственной стене
     */

    @GetMapping("/wall")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<WallPostDto> getOwnWall() {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        return postMapper.toListDto(postService.getPostsFromUserWall(currentUser));
    }

    /**
     * @param text текст поста
     * @return информация о созданном посте
     */

    @PostMapping("/wall")
    @PreAuthorize("hasAuthority('WRITING')")
    public WallPostDto addPostToOwnWall(@RequestBody String text) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Post post = postService.addWallPost(currentUser, currentUser, text);
        return postMapper.toDto(post);
    }

    /**
     * @param postPermission    тип разрешения создания постов на стене
     * @param commentPermission тип разрешения комментирования стены
     * @return статус действия
     */

    @PutMapping("/wall/settings")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage changeWallSettings(@RequestParam(required = false) UserWallPermissionType postPermission, @RequestParam(required = false) UserWallPermissionType commentPermission) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        userService.changeWallSettings(currentUser, postPermission, commentPermission);
        return new ResponseMessage("New wall settings are saved!");
    }

    /**
     * @param id id пользователя, стену которого нужно получить
     * @return список информации о постах со стены пользователя
     */

    @GetMapping("/id{id}/wall")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<WallPostDto> getWallByUserId(@PathVariable Long id) {
        User wallOwner = userService.getById(id);
        if (!currentUserIsAuthorizedToAccessOtherWallById(wallOwner.getId())) {
            throw new AuthorizationErrorException("Only friends can get this users wall!");
        }
        return postMapper.toListDto(postService.getPostsFromUserWall(wallOwner));
    }

    /**
     * @param id   id пользователя, на стену которого нужно добавить пост
     * @param text текст поста
     * @return информация о созданном посте
     */

    @PostMapping("/id{id}/wall")
    @PreAuthorize("hasAuthority('WRITING')")
    public WallPostDto addPostToUserWall(@PathVariable Long id, @RequestBody String text) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User wallOwner = userService.getById(id);
        Post post = postService.addWallPost(currentUser, wallOwner, text);
        return postMapper.toDto(post);
    }

    /**
     * @param id     id пользователя, пост со стены которого нужно получить
     * @param postId id поста, который нужно получить
     * @return информация о посте
     * @throws AuthorizationErrorException если текущий пользователь не имеет доступа к стене выбранного пользователя
     */

    @GetMapping("/id{id}/wall/post")
    @PreAuthorize("hasAuthority('WRITING')")
    public WallPostDto getPostById(@PathVariable Long id, @RequestParam Long postId) {
        User wallOwner = userService.getById(id);
        if (!currentUserIsAuthorizedToAccessOtherWallById(wallOwner.getId())) {
            throw new AuthorizationErrorException("Only friends can get this user wall!");
        }
        return postMapper.toDto(postService.getById(postId));
    }

    /**
     * @param id     id пользователя, со стены которого нужно удалить пост
     * @param postId id поста, котрый нужно удалить
     * @return статус действия
     */

    @DeleteMapping("/id{id}/wall/post")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage deletePost(@PathVariable Long id, @RequestParam Long postId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User wallOwner = userService.getById(id);
        return new ResponseMessage(postService.deleteUserWallPost(currentUser, wallOwner, postId));
    }

    /**
     * @param id     id пользователя, со стеной которого происходит взаимодействие
     * @param postId id поста, у которого нужно получить список лайков
     * @return список информации о лайках поста
     * @throws AuthorizationErrorException если текущий пользователь не имеет доступа к стене выбранного пользователя
     */

    @GetMapping("/id{id}/wall/post{postId}/likes")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<LikeDto> getLikesFromPost(@PathVariable Long id, @PathVariable Long postId) {
        User wallOwner = userService.getById(id);
        if (!currentUserIsAuthorizedToAccessOtherWallById(wallOwner.getId())) {
            throw new AuthorizationErrorException("Only friends can get this user wall!");
        }
        Post requestedPost = postService.getById(postId);
        return likeMapper.toListDto(requestedPost.getPostLikes());
    }

    /**
     * @param id     id пользователя, со стеной которого происходит взаимодействие
     * @param postId id поста, которому нужно поставить лайк
     * @return информация о поставленном лайке
     */

    @PostMapping("/id{id}/wall/post{postId}/likes")
    @PreAuthorize("hasAuthority('WRITING')")
    public LikeDto likePost(@PathVariable Long id, @PathVariable Long postId) {
        Post requestedPost = postService.getById(postId);
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User wallOwner = userService.getById(id);
        if (!currentUserIsAuthorizedToAccessOtherWallById(wallOwner.getId())) {
            throw new AuthorizationErrorException("Only friends can get this user wall!");
        }
        Like like = postService.addPostLike(currentUser, requestedPost);
        if (like == null) {
            throw new LikeRevokedException();
        }
        return likeMapper.toDto(like);
    }

    /**
     * @param id     id пользователя, со стеной которого происходит взаимодействие
     * @param postId id поста, с которого нужно получить комментарии
     * @return cписок информации о комментариях с поста
     * @throws AuthorizationErrorException если текущий пользователь не имеет доступа к стене выбранного пользователя
     */

    @GetMapping("/id{id}/wall/post{postId}/comments")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<CommentDto> getCommentsFromPost(@PathVariable Long id, @PathVariable Long postId) {
        Post requestedPost = postService.getById(postId);
        User wallOwner = userService.getById(id);
        if (!currentUserIsAuthorizedToAccessOtherWallById(wallOwner.getId())) {
            throw new AuthorizationErrorException("Only friends can get this user wall!");
        }
        return commentMapper.toListDto(requestedPost.getComments());
    }

    /**
     * @param id      id пользователя, со стеной которого происходит взаимодействие
     * @param postId  id поста, который нужно прокомментировать
     * @param message текст комментария
     * @return информация о созданном посте
     */

    @PostMapping("/id{id}/wall/post{postId}/comments")
    @PreAuthorize("hasAuthority('WRITING')")
    public CommentDto commentPost(@PathVariable Long id, @PathVariable Long postId, @RequestBody String message) {
        Post requestedPost = postService.getById(postId);
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User wallOwner = userService.getById(id);
        Comment newComment = postService.addWallPostComment(currentUser, wallOwner, requestedPost, message);
        return commentMapper.toDto(newComment);
    }

    /**
     * @param id        id пользователя, со стеной которого происходит взаимодействие
     * @param postId    id поста, у которого нужно удалить комментарий
     * @param commentId id комментария, который нужно удалить
     * @return статус действия
     * @throws AuthorizationErrorException пользователь не имеет прав на удаление комментария
     */

    @DeleteMapping("/id{id}/wall/post{postId}/comments")
    @PreAuthorize("hasAuthority('WRITING')")
    public ResponseMessage deleteComment(@PathVariable Long id, @PathVariable Long postId, @RequestParam Long commentId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User wallOwner = userService.getById(id);
        Post post = postService.getById(postId);
        Comment comment = postService.getCommentById(commentId);

        if (currentUser.getRole().getName().equals(RoleType.ADMIN)) {
            log.info("Admin action: Comment with id" + commentId + " deleted successfully!");
            postService.deleteCommentById(commentId);
            return new ResponseMessage("Admin action: Comment with id" + commentId + " deleted successfully!");
        }

        if (comment.getAuthor().getId().equals(currentUser.getId()) || currentUser.getId().equals(wallOwner.getId())) {
            postService.deleteCommentById(commentId);
        } else {
            throw new AuthorizationErrorException("Only author, wall owner or global admins can delete this comment!");
        }
        return new ResponseMessage("Comment deleted successfully!");
    }

    /**
     * @param id        id пользователя, со стеной которого происходит взаимодействие
     * @param postId    id поста, с которым происходи взаимодействие
     * @param commentId id комментария, лайки с которого нужно получить
     * @return информация об установленных лайках
     * @throws AuthorizationErrorException если текущий пользователь не имеет доступа к стене выбранного пользователя
     */

    @GetMapping("/id{id}/wall/post{postId}/comment{commentId}/likes")
    @PreAuthorize("hasAuthority('WRITING')")
    public List<LikeDto> getLikesFromComment(@PathVariable Long id, @PathVariable Long postId, @PathVariable Long commentId) {
        Comment requestedComment = postService.getCommentById(commentId);
        User wallOwner = userService.getById(id);
        if (!currentUserIsAuthorizedToAccessOtherWallById(wallOwner.getId())) {
            throw new AuthorizationErrorException("Only friends can get this users wall!");
        }
        return likeMapper.toListDto(requestedComment.getCommentLikes());
    }

    /**
     * @param id        id пользователя, со стеной которого происходит взаимодействие
     * @param postId    id поста, с которым происходи взаимодействие
     * @param commentId id комментария, на который устанавливается лайк
     * @return информация об установленном лайке
     */

    @PostMapping("/id{id}/wall/post{postId}/comment{commentId}/likes")
    @PreAuthorize("hasAuthority('WRITING')")
    public LikeDto likeCommentAtPost(@PathVariable Long id, @PathVariable Long postId, @PathVariable Long commentId) {
        Comment requestedComment = postService.getCommentById(commentId);
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        Like like = postService.addWallCommentLike(currentUser, requestedComment, userService.getById(id));
        if (like == null) {
            throw new LikeRevokedException();
        }
        return likeMapper.toDto(like);
    }


    /**
     * @param userId id пользователя, доступ к чьей стене нужно проверить у текущего пользователя
     * @return логическое значение доступа к стене другого пользователя
     */

    private Boolean currentUserIsAuthorizedToAccessOtherWallById(Long userId) {
        User currentUser = userService.getUserByUsername(getAuthenticationName());
        User wallOwner = userService.getById(userId);

        if (currentUser.getId().equals(userId)) {
            return true;
        }

        if (!wallOwner.isOpenUser()) {
            return userService.isFriendOf(currentUser.getId(), wallOwner.getId());
        }
        return true;
    }

    private String getAuthenticationName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
