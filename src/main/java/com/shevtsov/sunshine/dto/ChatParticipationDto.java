package com.shevtsov.sunshine.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatParticipationDto extends AbstractDto {
    private Long userId;
    private Long chatId;

    public ChatParticipationDto(Long id, Long userId, Long chatId) {
        this.id = id;
        this.userId = userId;
        this.chatId = chatId;
    }
}
