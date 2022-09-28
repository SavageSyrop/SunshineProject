package com.shevtsov.sunshine.service.impl;

import com.shevtsov.sunshine.dao.AbstractDao;
import com.shevtsov.sunshine.dao.entities.AbstractEntity;
import com.shevtsov.sunshine.dao.entities.Chat;
import com.shevtsov.sunshine.dao.entities.ChatParticipation;
import com.shevtsov.sunshine.dao.entities.Comment;
import com.shevtsov.sunshine.dao.entities.FriendRequest;
import com.shevtsov.sunshine.dao.entities.Group;
import com.shevtsov.sunshine.dao.entities.GroupMembership;
import com.shevtsov.sunshine.dao.entities.Like;
import com.shevtsov.sunshine.dao.entities.Message;
import com.shevtsov.sunshine.dao.entities.Post;
import com.shevtsov.sunshine.dao.entities.Role;
import com.shevtsov.sunshine.dao.entities.User;
import com.shevtsov.sunshine.dao.entities.UserInfo;
import com.shevtsov.sunshine.dao.entities.Wall;
import com.shevtsov.sunshine.common.GenderType;
import com.shevtsov.sunshine.common.GroupRole;
import com.shevtsov.sunshine.common.GroupWallType;
import com.shevtsov.sunshine.common.RoleType;
import com.shevtsov.sunshine.common.UserWallPermissionType;
import com.shevtsov.sunshine.dao.impl.ChatDaoImpl;
import com.shevtsov.sunshine.dao.impl.ChatParticipationDaoImpl;
import com.shevtsov.sunshine.dao.impl.CommentDaoImpl;
import com.shevtsov.sunshine.dao.impl.FriendRequestDaoImpl;
import com.shevtsov.sunshine.dao.impl.GroupDaoImpl;
import com.shevtsov.sunshine.dao.impl.GroupMembershipDaoImpl;
import com.shevtsov.sunshine.dao.impl.LikeDaoImpl;
import com.shevtsov.sunshine.dao.impl.MessageDaoImpl;
import com.shevtsov.sunshine.dao.impl.PostDaoImpl;
import com.shevtsov.sunshine.dao.impl.RoleDaoImpl;
import com.shevtsov.sunshine.dao.impl.UserDaoImpl;
import com.shevtsov.sunshine.dao.impl.UserInfoDaoImpl;
import com.shevtsov.sunshine.dao.impl.WallDaoImpl;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;


@Getter
@Setter
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractServiceImplTest {
    public Map<Long, Chat> chatMap = new HashMap<>();
    private Long chatId = 1L;
    public Map<Long, ChatParticipation> chatParticipationMap = new HashMap<>();
    private Long chatParticipationId = 1L;
    public Map<Long, Comment> commentMap = new HashMap<>();
    private Long commentId = 1L;
    public Map<Long, FriendRequest> friendRequestMap = new HashMap<>();
    private Long friendRequestId = 1L;
    public Map<Long, Group> groupMap = new HashMap<>();
    private Long groupId = 1L;
    public Map<Long, GroupMembership> groupMembershipMap = new HashMap<>();
    private Long groupMembershipId = 1L;
    public Map<Long, Like> likeMap = new HashMap<>();
    private Long likeId = 1L;
    public Map<Long, Message> messageMap = new HashMap<>();
    private Long messageId = 1L;
    public Map<Long, Post> postMap = new HashMap<>();
    private Long postId = 1L;
    public Map<Long, Role> roleMap = new HashMap<>();
    public Map<Long, User> userMap = new HashMap<>();
    private Long userId = 1L;
    public Map<Long, UserInfo> userInfoMap = new HashMap<>();
    private Long userInfoId = 1L;
    public Map<Long, Wall> wallMap = new HashMap<>();
    private Long wallId = 1L;

    @Mock
    private ChatDaoImpl chatDao;
    @Mock
    private ChatParticipationDaoImpl chatParticipationDao;
    @Mock
    private CommentDaoImpl commentDao;
    @Mock
    private FriendRequestDaoImpl friendRequestDao;
    @Mock
    private GroupDaoImpl groupDao;
    @Mock
    private GroupMembershipDaoImpl groupMembershipDao;
    @Mock
    private LikeDaoImpl likeDao;
    @Mock
    private MessageDaoImpl messageDao;
    @Mock
    private PostDaoImpl postDao;
    @Mock
    private RoleDaoImpl roleDao;
    @Mock
    private UserDaoImpl userDao;
    @Mock
    private UserInfoDaoImpl userInfoDao;
    @Mock
    private WallDaoImpl wallDao;
    @Mock
    private MailService mailService;
    @Spy
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @BeforeEach
    public void setup() {
        setupChatDao();
        setupChatParticipationDao();
        setupCommentDao();
        setupFriendRequestDao();
        setupGroupDao();
        setupGroupMembershipDao();
        setupLikeDao();
        setupMessageDao();
        setupPostDao();
        setupRoleDao();
        setupUserDao();
        setupUserInfoDao();
        setupWallDao();
        setupMailService();
    }

    @BeforeAll
    public void fillDB() {
        fillRoleTable();
        fillUserInfoTable();
        fillWallTable();
        fillUserTable();
        fillGroupTable();
        fillGroupMembershipTable();
    }

    public <T extends AbstractEntity> void setupGetById(Map<Long, T> hashmap, AbstractDao<T> dao, Class<T> className) {
        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (hashmap.containsKey(id)) {
                return hashmap.get(id);
            }
            String[] splittedClassName = className.getName().split("\\.");
            throw new EntityNotFoundException(splittedClassName[splittedClassName.length - 1] + " with id " + id + " is not found!");
        }).when(dao).getById(anyLong());
    }

    public <T extends AbstractEntity> void setupDeleteById(Map<Long, T> hashmap, AbstractDao<T> dao, Class<T> className) {
        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (hashmap.containsKey(id)) {
                hashmap.remove(id);
                return null;
            }
            String[] splittedClassName = className.getName().split("\\.");
            throw new EntityNotFoundException(splittedClassName[splittedClassName.length - 1] + " with id " + id + " is not found!");
        }).when(dao).deleteById(anyLong());
    }

    public <T extends AbstractEntity> void setupCreate(Map<Long, T> hashmap, AbstractDao<T> dao, Class<T> className) {
        lenient().doAnswer(invocationOnMock -> {
            T entity = invocationOnMock.getArgument(0);
            Long id = (long) hashmap.size();
            while (hashmap.containsKey(id)) {
                id++;
            }
            entity.setId(id);
            hashmap.put(id, entity);
            return entity;
        }).when(dao).create(any(className));
    }

    public void setupChatDao() {
        setupGetById(chatMap, chatDao, Chat.class);
        setupDeleteById(chatMap, chatDao, Chat.class);
        lenient().doAnswer(invocationOnMock -> {
            Chat chat = invocationOnMock.getArgument(0);
            chat.setId(chatId);
            chat.setMessages(new ArrayList<>());
            chatMap.put(chatId, chat);
            chatId++;
            return chat;
        }).when(chatDao).create(any(Chat.class));
    }

    public void setupChatParticipationDao() {
        setupGetById(chatParticipationMap, chatParticipationDao, ChatParticipation.class);

        lenient().doAnswer(invocationOnMock -> {
            ChatParticipation chatParticipation = invocationOnMock.getArgument(0);
            chatParticipation.setId(chatParticipationId);
            chatParticipationMap.put(chatParticipationId, chatParticipation);
            chatParticipationId++;
            return chatParticipation;
        }).when(chatParticipationDao).create(any(ChatParticipation.class));

        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (chatParticipationMap.containsKey(id)) {
                ChatParticipation participation = chatParticipationMap.get(id);
                participation.getUser().getChatParticipations().remove(participation);
                chatParticipationMap.remove(id);
                return null;
            }
            throw new EntityNotFoundException("ChatParticipation with id " + id + " is not found!");
        }).when(chatParticipationDao).deleteById(anyLong());

        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (chatParticipationMap.containsKey(id)) {
                ChatParticipation participation = chatParticipationMap.get(id);
                participation.getUser().getChatParticipations().remove(participation);
                chatParticipationMap.remove(id);
                return null;
            }
            throw new EntityNotFoundException("ChatParticipation with id " + id + " is not found!");
        }).when(chatParticipationDao).deleteById(anyLong());

        lenient().doAnswer(invocationOnMock -> {
            Long chatId = invocationOnMock.getArgument(0);
            Long userId = invocationOnMock.getArgument(1);
            for (ChatParticipation chatParticipation : chatParticipationMap.values()) {
                if (chatParticipation.getChat().getId().equals(chatId) && chatParticipation.getUser().getId().equals(userId)) {
                    return chatParticipation;
                }
            }
            return null;
        }).when(chatParticipationDao).getUserParticipationInChatByChatId(anyLong(), anyLong());

        lenient().doAnswer(invocationOnMock -> {
            Long chatId = invocationOnMock.getArgument(0);
            List<ChatParticipation> chatParticipationList = new ArrayList<>();
            for (ChatParticipation chatParticipation : chatParticipationMap.values()) {
                if (chatParticipation.getChat().getId().equals(chatId)) {
                    chatParticipationList.add(chatParticipation);
                }
            }
            return chatParticipationList;
        }).when(chatParticipationDao).getChatParticipantsByChatId(anyLong());


    }

    public void setupCommentDao() {
        setupGetById(commentMap, commentDao, Comment.class);
        lenient().doAnswer(invocationOnMock -> {
            Comment comment = invocationOnMock.getArgument(0);
            comment.setId(commentId);
            comment.setCommentLikes(new ArrayList<>());
            commentMap.put(commentId, comment);
            commentId++;
            return comment;
        }).when(commentDao).create(any(Comment.class));
        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (commentMap.containsKey(id)) {
                Comment comment = commentMap.get(id);
                comment.getPost().getComments().remove(comment);
                commentMap.remove(id);
                return null;
            }
            throw new EntityNotFoundException("Comment with id " + id + " is not found!");
        }).when(commentDao).deleteById(anyLong());
    }

    public void setupFriendRequestDao() {
        setupGetById(friendRequestMap, friendRequestDao, FriendRequest.class);
        lenient().doAnswer(invocationOnMock -> {
            FriendRequest friendRequest = invocationOnMock.getArgument(0);
            friendRequest.setId(friendRequestId);
            User senderUser = friendRequest.getSenderUser();
            senderUser.getFriendRequests().add(friendRequest);
            friendRequestMap.put(friendRequestId, friendRequest);
            friendRequestId++;
            return friendRequest;
        }).when(friendRequestDao).create(any(FriendRequest.class));

        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (friendRequestMap.containsKey(id)) {
                FriendRequest friendRequest = friendRequestMap.get(id);
                friendRequest.getSenderUser().getFriendRequests().remove(friendRequest);
                friendRequestMap.remove(id);
                return null;
            }
            throw new EntityNotFoundException("FriendRequest with id " + id + " is not found!");
        }).when(friendRequestDao).deleteById(anyLong());

        lenient().doAnswer(invocationOnMock -> {
            Long currentUserId = invocationOnMock.getArgument(0);
            Long recipientUserId = invocationOnMock.getArgument(1);
            for (FriendRequest friendRequest : friendRequestMap.values()) {
                if (currentUserId.equals(friendRequest.getSenderUser().getId()) && recipientUserId.equals(friendRequest.getRecipientUser().getId())) {
                    return friendRequest;
                }
            }
            return null;
        }).when(friendRequestDao).getFriendRequestByUserIds(anyLong(), anyLong());


        lenient().doAnswer(invocationOnMock -> {
            Long currentUserId = invocationOnMock.getArgument(0);
            List<FriendRequest> friends = new ArrayList<>();
            for (FriendRequest friendRequest : friendRequestMap.values()) {
                if (currentUserId.equals(friendRequest.getSenderUser().getId()) && friendRequest.getIsAccepted()) {
                    friends.add(friendRequest);
                }
            }
            return friends;
        }).when(friendRequestDao).getUserFriends(anyLong());


        lenient().doAnswer(invocationOnMock -> {
            Long currentUserId = invocationOnMock.getArgument(0);
            List<FriendRequest> friends = new ArrayList<>();
            for (FriendRequest friendRequest : friendRequestMap.values()) {
                if (currentUserId.equals(friendRequest.getSenderUser().getId()) && !friendRequest.getIsAccepted()) {
                    friends.add(friendRequest);
                }
            }
            return friends;
        }).when(friendRequestDao).getUserSentFriendRequests(anyLong());

        lenient().doAnswer(invocationOnMock -> {
            Long currentUserId = invocationOnMock.getArgument(0);
            Long recipientUserId = invocationOnMock.getArgument(1);
            for (FriendRequest friendRequest : friendRequestMap.values()) {
                if (friendRequest.getIsAccepted() && friendRequest.getSenderUser().getId().equals(currentUserId) && friendRequest.getRecipientUser().getId().equals(recipientUserId)) {
                    return true;
                }
            }
            return false;
        }).when(friendRequestDao).isFriendOf(anyLong(), anyLong());
    }

    public void setupGroupDao() {
        setupGetById(groupMap, groupDao, Group.class);
        lenient().doAnswer(invocationOnMock -> {
            Group group = invocationOnMock.getArgument(0);
            group.setId(groupId);
            group.setGroupMemberships(new ArrayList<>());
            group.setWall(new ArrayList<>());
            groupMap.put(groupId, group);
            groupId++;
            return group;
        }).when(groupDao).create(any(Group.class));
        setupDeleteById(groupMap, groupDao, Group.class);

        lenient().doAnswer(invocationOnMock -> {
            Long groupId = invocationOnMock.getArgument(0);
            List<Post> posts = new ArrayList<>();
            for (Post post : postMap.values()) {
                if (post.getIsPublished() && post.getGroup() != null && post.getGroup().getId().equals(groupId)) {
                    posts.add(post);
                }
            }
            return posts;
        }).when(groupDao).getGroupPublishedPosts(anyLong());

        lenient().doAnswer(invocationOnMock -> {
            Long groupId = invocationOnMock.getArgument(0);
            List<Post> posts = new ArrayList<>();
            for (Post post : postMap.values()) {
                if (!post.getIsPublished() && post.getGroup() != null && post.getGroup().getId().equals(groupId)) {
                    posts.add(post);
                }
            }
            return posts;
        }).when(groupDao).getGroupOfferedPosts(anyLong());
    }

    public void setupGroupMembershipDao() {
        setupGetById(groupMembershipMap, groupMembershipDao, GroupMembership.class);

        lenient().doAnswer(invocationOnMock -> {
            GroupMembership groupMembership = invocationOnMock.getArgument(0);
            groupMembership.setId(groupMembershipId);
            groupMembershipMap.put(groupMembershipId, groupMembership);
            groupDao.getById(groupMembership.getGroup().getId()).getGroupMemberships().add(groupMembership);
            groupMembershipId++;
            return groupMembership;
        }).when(groupMembershipDao).create(any(GroupMembership.class));

        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (groupMembershipMap.containsKey(id)) {
                GroupMembership groupMembership = groupMembershipMap.get(id);
                groupMembership.getGroup().getGroupMemberships().remove(groupMembership);
                groupMembership.getUser().getGroups().remove(groupMembership);
                groupMembershipMap.remove(id);
                return null;
            }
            throw new EntityNotFoundException("GroupMembership with id " + id + " is not found!");
        }).when(groupMembershipDao).deleteById(anyLong());

        lenient().doAnswer(invocationOnMock -> {
            Long currentUserId = invocationOnMock.getArgument(0);
            Long groupId = invocationOnMock.getArgument(1);
            for (GroupMembership groupMembership : groupMembershipMap.values()) {
                if (groupMembership.getGroup().getId().equals(groupId) && groupMembership.getUser().getId().equals(currentUserId)) {
                    return groupMembership;
                }
            }
            return null;
        }).when(groupMembershipDao).getGroupMembershipByUserAndGroupIds(anyLong(), anyLong());

        lenient().doAnswer(invocationOnMock -> {
            Long groupId = invocationOnMock.getArgument(0);
            for (GroupMembership groupMembership : groupMembershipMap.values()) {
                if (groupMembership.getGroup().getId().equals(groupId) && groupMembership.getGroupRole() == GroupRole.OWNER) {
                    return groupMembership;
                }
            }
            return null;
        }).when(groupMembershipDao).getGroupOwnerMembership(anyLong());


        lenient().doAnswer(invocationOnMock -> {
            Long userId = invocationOnMock.getArgument(0);
            List<GroupMembership> memberships = new ArrayList<>();
            for (GroupMembership groupMembership : groupMembershipMap.values()) {
                if (groupMembership.getUser().getId().equals(userId) && groupMembership.getGroupRole() != GroupRole.AWAITING_CHECK) {
                    memberships.add(groupMembership);
                }
            }
            return memberships;
        }).when(groupMembershipDao).getGroupMemberships(anyLong());


        lenient().doAnswer(invocationOnMock -> {
            Long groupId = invocationOnMock.getArgument(0);
            List<GroupMembership> memberships = new ArrayList<>();
            for (GroupMembership groupMembership : groupMap.get(groupId).getGroupMemberships()) {
                if (groupMembership.getGroupRole()!=GroupRole.AWAITING_CHECK) {
                    memberships.add(groupMembership);
                }
            }
            return memberships;
        }).when(groupMembershipDao).getSubscribeRequests(anyLong());

    }


    public void setupLikeDao() {
        setupGetById(likeMap, likeDao, Like.class);

        lenient().doAnswer(invocationOnMock -> {
            Like like = invocationOnMock.getArgument(0);
            like.setId(likeId);

            if (like.getComment() != null) {
                like.getComment().getCommentLikes().add(like);
            }

            if (like.getPost() != null) {
                like.getPost().getPostLikes().add(like);
            }

            likeMap.put(likeId, like);
            likeId++;
            return like;
        }).when(likeDao).create(any(Like.class));

        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (likeMap.containsKey(id)) {
                Like like = likeMap.get(id);
                if (like.getComment() != null) {
                    like.getComment().getCommentLikes().remove(like);
                }
                if (like.getPost() != null) {
                    like.getPost().getPostLikes().remove(like);
                }
                likeMap.remove(id);
                return null;
            }
            throw new EntityNotFoundException("Like with id " + id + " is not found!");
        }).when(likeDao).deleteById(anyLong());

        lenient().doAnswer(invocationOnMock -> {
            Long commentId = invocationOnMock.getArgument(0);
            Long userId = invocationOnMock.getArgument(1);
            for (Like like : likeMap.values()) {
                if (like.getComment() != null && like.getComment().getId().equals(commentId) && like.getUser().getId().equals(userId)) {
                    return like;
                }
            }
            return null;
        }).when(likeDao).getCommentLikeByUserId(anyLong(), anyLong());


        lenient().doAnswer(invocationOnMock -> {
            Long postId = invocationOnMock.getArgument(0);
            Long userId = invocationOnMock.getArgument(1);
            for (Like like : likeMap.values()) {
                if (like.getPost() != null && like.getPost().getId().equals(postId) && like.getUser().getId().equals(userId)) {
                    return like;
                }
            }
            return null;
        }).when(likeDao).getPostLikeByUserId(anyLong(), anyLong());

    }

    public void setupMessageDao() {
        setupGetById(messageMap, messageDao, Message.class);

        lenient().doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setId(messageId);
            messageMap.put(messageId, message);
            messageId++;
            return message;
        }).when(messageDao).create(any(Message.class));

        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (messageMap.containsKey(id)) {
                Message message = messageMap.get(id);
                if (message.getChat()!=null){
                    message.getChat().getMessages().remove(message);
                }
                messageMap.remove(id);
                return null;
            }
            throw new EntityNotFoundException("Message with id " + id + " is not found!");
        }).when(messageDao).deleteById(anyLong());

        lenient().doAnswer(invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);
            for (Message message : messageMap.values()) {
                if (message.getChat() == null && message.getSender().getId().equals(user.getId())) {
                    return message;
                }
            }
            return null;
        }).when(messageDao).getUserSupportRequest(any(User.class));

        lenient().doAnswer(invocationOnMock -> {
            List<Message> messages = new ArrayList<>();
            for (Message message : messageMap.values()) {
                if (message.getChat() == null) {
                    messages.add(message);
                }
            }
            return messages;
        }).when(messageDao).getSupportRequests();

    }

    public void setupPostDao() {
        setupGetById(postMap, postDao, Post.class);
        lenient().doAnswer(invocationOnMock -> {
            Post post = invocationOnMock.getArgument(0);
            post.setId(postId);
            post.setPostLikes(new ArrayList<>());
            post.setComments(new ArrayList<>());
            if (post.getWall() != null) {
                post.getWall().getPosts().add(post);
            }
            if (post.getGroup() != null) {
                post.getGroup().getWall().add(post);
            }
            postMap.put(postId, post);
            postId++;
            return post;
        }).when(postDao).create(any(Post.class));
        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (postMap.containsKey(id)) {
                Post post = postMap.get(id);
                if (post.getGroup() != null) {
                    post.getGroup().getWall().remove(post);
                }
                if (post.getWall() != null) {
                    post.getWall().getPosts().remove(post);
                }
                postMap.remove(id);
                return null;
            }
            throw new EntityNotFoundException("Post with id " + id + " is not found!");
        }).when(postDao).deleteById(anyLong());
    }

    public void setupRoleDao() {
        setupGetById(roleMap, roleDao, Role.class);
        lenient().doAnswer(invocationOnMock -> {
            Role role = invocationOnMock.getArgument(0);
            Long id = (long) roleMap.size();
            while (roleMap.containsKey(id)) {
                id++;
            }
            role.setId(id);
            role.setPermissions(new ArrayList<>());
            roleMap.put(id, role);
            return role;
        }).when(roleDao).create(any(Role.class));
        setupDeleteById(roleMap, roleDao, Role.class);
    }

    public void setupUserDao() {
        setupGetById(userMap, userDao, User.class);
        lenient().doAnswer(invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);
            user.setId(userId);
            user.setGroups(new ArrayList<>());
            user.setChatParticipations(new ArrayList<>());
            user.setFriendRequests(new ArrayList<>());
            user.setGroups(new ArrayList<>());
            userMap.put(userId, user);
            userId++;
            return user;
        }).when(userDao).create(any(User.class));

        setupDeleteById(userMap, userDao, User.class);

        lenient().doAnswer(invocationOnMock -> {
            String username = invocationOnMock.getArgument(0);
            for (User user : userMap.values()) {
                if (user.getUsername().equals(username)) {
                    return user;
                }
            }
            return null;
        }).when(userDao).getByUsername(any(String.class));

        lenient().doAnswer(invocationOnMock -> {
            String restoreCode = invocationOnMock.getArgument(0);
            for (User user : userMap.values()) {
                if (restoreCode.equals(user.getRestorePasswordCode())) {
                    return user;
                }
            }
            return null;
        }).when(userDao).getUserByRestorePasswordCode(any(String.class));

        lenient().doAnswer(invocationOnMock -> {
            String activationCode = invocationOnMock.getArgument(0);
            for (User user : userMap.values()) {
                if (activationCode.equals(user.getActivationCode())) {
                    return user;
                }
            }
            return null;
        }).when(userDao).getUserByActivationCode(any(String.class));


    }

    public void setupUserInfoDao() {
        setupGetById(userInfoMap, userInfoDao, UserInfo.class);

        lenient().doAnswer(invocationOnMock -> {
            UserInfo userInfo = invocationOnMock.getArgument(0);
            userInfo.setId(userInfoId);
            userInfoMap.put(userInfoId, userInfo);
            userInfoId++;
            return userInfo;
        }).when(userInfoDao).create(any(UserInfo.class));

        setupDeleteById(userInfoMap, userInfoDao, UserInfo.class);
    }

    public void setupWallDao() {
        setupGetById(wallMap, wallDao, Wall.class);
        lenient().doAnswer(invocationOnMock -> {
            Wall wall = invocationOnMock.getArgument(0);
            wall.setId(wallId);
            wall.setPosts(new ArrayList<>());
            wallMap.put(wallId, wall);
            wallId++;
            return wall;
        }).when(wallDao).create(any(Wall.class));
    }

    public void setupMailService() {
        lenient().doAnswer(invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);
            return user.getActivationCode();
        }).when(mailService).sendActivationEmail(any(User.class));

        lenient().doAnswer(invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);
            return user.getRestorePasswordCode();
        }).when(mailService).sendForgotPasswordEmail(any(User.class));
    }

    public void fillRoleTable() {
        Role admin = new Role();
        admin.setName(RoleType.ADMIN);
        Role user = new Role();
        user.setName(RoleType.USER);
        roleMap.put(1L, admin);
        roleMap.put(2L, user);
    }

    public void addWall(Long id, UserWallPermissionType postPermission, UserWallPermissionType commentPermission) {
        Wall wall = new Wall(postPermission, commentPermission);
        wall.setId(id);
        wall.setPosts(new ArrayList<>());
        this.wallMap.put(id, wall);
        wallId++;
    }

    public void addUserInfo(Long id, String username, String password, String email, String firstName, String lastName, GenderType genderType, Date dateOfBirth, String city, Boolean openProfile) {
        UserInfo userInfo = new UserInfo(username, password, email, firstName, lastName, genderType, dateOfBirth, city, openProfile);
        userInfo.setId(id);
        this.userInfoMap.put(id, userInfo);
        userInfoId++;
    }

    public void addUser(Long id, UserInfo userInfo, Wall wall, Long roleId) {
        Role role = roleMap.get(roleId);
        User user = new User(userInfo, wall, role);
        user.setId(id);
        user.setFriendRequests(new ArrayList<>());
        user.setChatParticipations(new ArrayList<>());
        user.setGroups(new ArrayList<>());
        this.userMap.put(id, user);
        userId++;
    }

    public void addGroupMembership(Long id, User currentUser, Group group, GroupRole role) {
        GroupMembership groupMembership = new GroupMembership(currentUser, group, role);
        groupMembership.setId(id);
        groupMembership.getGroup().getGroupMemberships().add(groupMembership);
        this.groupMembershipMap.put(id, groupMembership);
        groupMembershipId++;
    }

    public void addGroup(Long id, String name, String info, GroupWallType wallType, Boolean openToJoin) {
        Group group = new Group(name, info, wallType, openToJoin);
        group.setId(id);
        group.setWall(new ArrayList<>());
        group.setGroupMemberships(new ArrayList<>());
        this.groupMap.put(id, group);
        groupId++;
    }

    public void createFriendship(User currentUser, User recipientUser) {
        FriendRequest friendRequestCur = new FriendRequest(currentUser, recipientUser, true);
        FriendRequest friendRequestRec = new FriendRequest(recipientUser, currentUser, true);
        getFriendRequestDao().create(friendRequestCur);
        getFriendRequestDao().create(friendRequestRec);
    }


    private void fillUserInfoTable() {
        addUserInfo(1L, "desertfox", "$2a$10$dCKE0qv1SW3dKBTXkauFburkrCGOznBAhdXaV3Km9yre7qysphk1u", "fess.2002@mail.ru", "Yaroslav", "Shevtsov", GenderType.MALE, new Date(9, Calendar.NOVEMBER, 2001), "Tomsk", true);
        addUserInfo(2L, "aleoonka", "$2a$10$Uimw7bv5iTa.5miRSn4M4uGosxfyh1d89aVEHIkSNsPFkz6NmgOLq", "project.sunshine@mail.ru", "Alena", "Tsemkalo", GenderType.FEMALE, new Date(23, Calendar.JULY, 2002), "Moscow", true);
        addUserInfo(3L, "Gump", "$2a$10$s.HMSOhwXDkV6aK87ArD2.SBooCd4S9T1pAtMF.GygkMPRZaldyDK", "project.sunshine@mail.ru", "Forrest", "Gump", GenderType.KROGAN, new Date(9, Calendar.MAY, 1945), "Greenbow", true);
        addUserInfo(4L, "Belka", "$2a$10$R0o2eqVOcKpt.320Dr6rqujuXTSV87Pt.o0NydQafz3kDIzPnpzAO", "project.sunshine@mail.ru", "Squirel", "Thompson", GenderType.FEMALE, new Date(7, Calendar.SEPTEMBER, 2022), "Las-Vegas", false);
        addUserInfo(5L, "BOT", "$2a$10$ksNrhvJfhTKJ46mz2wLpa.dDJMjs0PtPh1tr7TjkqNRUwvcsPhNMO", "project.sunshine@mail.ru", "Roflan", "Rabotyaga", GenderType.KROGAN, new Date(1, Calendar.JANUARY, 2000), "Vinnitsa", false);
        addUserInfo(6L, "Reaper", "$2a$10$O1alPtSNaqQ1FXvEGc/Y/.VJb9N2rZbRNN72uDEhXd4FLGO/isPZy", "project.sunshine@mail.ru", "The", "Sovereign", GenderType.MALE, new Date(13, Calendar.MARCH, 37), "Citadel", true);

    }

    private void fillWallTable() {
        addWall(1L, UserWallPermissionType.OWNER, UserWallPermissionType.OWNER);
        addWall(2L, UserWallPermissionType.FRIENDS, UserWallPermissionType.FRIENDS);
        addWall(3L, UserWallPermissionType.ALL, UserWallPermissionType.ALL);
        addWall(4L, UserWallPermissionType.OWNER, UserWallPermissionType.FRIENDS);
        addWall(5L, UserWallPermissionType.FRIENDS, UserWallPermissionType.OWNER);
        addWall(6L, UserWallPermissionType.OWNER, UserWallPermissionType.ALL);
    }


    private void fillUserTable() {
        addUser(1L, userInfoMap.get(1L), wallMap.get(1L), 1L);
        addUser(2L, userInfoMap.get(2L), wallMap.get(2L), 2L);
        addUser(3L, userInfoMap.get(3L), wallMap.get(3L), 2L);
        addUser(4L, userInfoMap.get(4L), wallMap.get(4L), 2L);
        addUser(5L, userInfoMap.get(5L), wallMap.get(5L), 1L);
        addUser(6L, userInfoMap.get(6L), wallMap.get(6L), 2L);
    }

    private void fillGroupTable() {
        addGroup(1L, "Naggers", "Group for test", GroupWallType.ALL, true);
        addGroup(2L, "Pregloga", "Group for test of offered posts", GroupWallType.OFFERED_POSTS, true);
        addGroup(3L, "Subics", "Group for test of subscribe type", GroupWallType.SUBSCRIBERS, false);
        addGroup(4L, "Admins", "Group for test of admins", GroupWallType.ADMINS, true);
        addGroup(5L, "Pregloga", "Pedloga", GroupWallType.OFFERED_POSTS, false);
        addGroup(6L, "Owner Paradise", "Group for owner", GroupWallType.OWNER, true);

    }

    private void fillGroupMembershipTable() {
        addGroupMembership(1L, userMap.get(1L), groupMap.get(1L), GroupRole.OWNER);

    }

}