package com.shevtsov.sunshine.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GroupPostDto extends WallPostDto {
    private Long groupId;
    private Boolean isPublished;

    public GroupPostDto(Long id, String text, LocalDateTime sendingTime, String authorName, List<LikeDto> postLikes, List<CommentDto> comments, Long groupId, Boolean isPublished) {
        this.id = id;
        this.text = text;
        this.sendingTime = sendingTime;
        this.authorName = authorName;
        this.postLikes = postLikes;
        this.comments = comments;
        this.groupId = groupId;
        this.isPublished = isPublished;
    }
}
