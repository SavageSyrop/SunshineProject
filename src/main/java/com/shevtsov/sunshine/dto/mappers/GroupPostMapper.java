package com.shevtsov.sunshine.dto.mappers;

import com.shevtsov.sunshine.dto.CommentDto;
import com.shevtsov.sunshine.dto.GroupPostDto;
import com.shevtsov.sunshine.dto.LikeDto;
import com.shevtsov.sunshine.dao.entities.Comment;
import com.shevtsov.sunshine.dao.entities.Like;
import com.shevtsov.sunshine.dao.entities.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupPostMapper extends ListMapper<GroupPostDto, Post> {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private CommentMapper commentsMapper;

    @Override
    public GroupPostDto toDto(Post entity) {
        List<LikeDto> likes = new ArrayList<>();
        for (Like like : entity.getPostLikes()) {
            likes.add(likeMapper.toDto(like));
        }
        List<CommentDto> comments = new ArrayList<>();
        for (Comment comment : entity.getComments()) {
            comments.add(commentsMapper.toDto(comment));
        }
        return new GroupPostDto(entity.getId(), entity.getText(), entity.getSendingTime(), entity.getAuthor().getUsername(), likes, comments, entity.getGroup().getId(), entity.getIsPublished());
    }
}
