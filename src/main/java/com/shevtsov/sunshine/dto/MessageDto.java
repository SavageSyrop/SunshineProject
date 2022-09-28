package com.shevtsov.sunshine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shevtsov.sunshine.common.TimeFormatPatterns;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageDto extends AbstractDto {
    private String senderName;
    private String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormatPatterns.TIMESTAMP_PATTERN)
    private LocalDateTime sendingTime;

    public MessageDto(Long id, String senderName, String text, LocalDateTime time) {
        this.id = id;
        this.senderName = senderName;
        this.text = text;
        this.sendingTime = time;
    }
}
