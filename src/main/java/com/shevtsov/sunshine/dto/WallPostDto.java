package com.shevtsov.sunshine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shevtsov.sunshine.common.TimeFormatPatterns;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WallPostDto extends AbstractDto {
    protected String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormatPatterns.TIMESTAMP_PATTERN)
    protected LocalDateTime sendingTime;
    protected String authorName;
    protected List<LikeDto> postLikes;
    protected List<CommentDto> comments;

    public WallPostDto(Long id, String text, LocalDateTime sendingTime, String authorName, List<LikeDto> postLikes, List<CommentDto> comments) {
        this.id = id;
        this.text = text;
        this.sendingTime = sendingTime;
        this.authorName = authorName;
        this.postLikes = postLikes;
        this.comments = comments;
    }
}
