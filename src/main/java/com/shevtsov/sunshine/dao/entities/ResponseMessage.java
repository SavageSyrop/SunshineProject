package com.shevtsov.sunshine.dao.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shevtsov.sunshine.common.TimeFormatPatterns;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

@NoArgsConstructor
public class ResponseMessage {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormatPatterns.TIMESTAMP_PATTERN)
    private LocalDateTime timestamp;

    private String message;

    public ResponseMessage(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }
}
