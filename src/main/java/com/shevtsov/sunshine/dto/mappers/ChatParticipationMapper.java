package com.shevtsov.sunshine.dto.mappers;

import com.shevtsov.sunshine.dto.ChatParticipationDto;
import com.shevtsov.sunshine.dao.entities.ChatParticipation;
import org.springframework.stereotype.Component;

@Component
public class ChatParticipationMapper extends ListMapper<ChatParticipationDto, ChatParticipation> {
    @Override
    public ChatParticipationDto toDto(ChatParticipation entity) {
        return new ChatParticipationDto(entity.getId(), entity.getUser().getId(), entity.getChat().getId());
    }
}
