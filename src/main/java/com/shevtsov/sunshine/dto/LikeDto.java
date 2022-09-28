package com.shevtsov.sunshine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shevtsov.sunshine.common.TimeFormatPatterns;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LikeDto extends AbstractDto {
    private String username;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormatPatterns.TIMESTAMP_PATTERN)
    private LocalDateTime sendingTime;

    public LikeDto(Long id, String username, LocalDateTime sendingTime) {
        this.id = id;
        this.username = username;
        this.sendingTime = sendingTime;
    }
}
