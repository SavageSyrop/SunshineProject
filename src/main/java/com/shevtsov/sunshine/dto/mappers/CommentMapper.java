package com.shevtsov.sunshine.dto.mappers;

import com.shevtsov.sunshine.dto.CommentDto;
import com.shevtsov.sunshine.dao.entities.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper extends ListMapper<CommentDto, Comment> {
    @Autowired
    private LikeMapper likeMapper;

    @Override
    public CommentDto toDto(Comment entity) {
        return new CommentDto(entity.getId(), entity.getAuthor().getUsername(), entity.getText(), entity.getSendingTime(), likeMapper.toListDto(entity.getCommentLikes()));
    }
}