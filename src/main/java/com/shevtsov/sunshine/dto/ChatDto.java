package com.shevtsov.sunshine.dto;

import com.shevtsov.sunshine.common.ChatType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatDto extends AbstractDto {
    private List<MessageDto> messages;
    private ChatType chatType;
    private String name;

    public ChatDto(Long id,ChatType chatType, String name, List<MessageDto> messages) {
        this.id = id;
        this.chatType = chatType;
        this.name = name;
        this.messages = messages;
    }
}
