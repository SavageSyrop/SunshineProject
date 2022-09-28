package com.shevtsov.sunshine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shevtsov.sunshine.common.TimeFormatPatterns;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CommentDto extends AbstractDto {
    private String username;
    private String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormatPatterns.TIMESTAMP_PATTERN)
    private LocalDateTime sendingTime;
    private List<LikeDto> commentLikes;

    public CommentDto(Long id, String user, String text, LocalDateTime sendingTime, List<LikeDto> commentLikes) {
        this.id = id;
        this.username = user;
        this.text = text;
        this.sendingTime = sendingTime;
        this.commentLikes = commentLikes;
    }
}
