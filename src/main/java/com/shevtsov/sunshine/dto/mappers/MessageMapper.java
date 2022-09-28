package com.shevtsov.sunshine.dto.mappers;

import com.shevtsov.sunshine.dto.MessageDto;
import com.shevtsov.sunshine.dao.entities.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper extends ListMapper<MessageDto, Message> {

    @Override
    public MessageDto toDto(Message entity) {
        return new MessageDto(entity.getId(), entity.getSenderName(), entity.getText(), entity.getSendingTime());
    }
}
