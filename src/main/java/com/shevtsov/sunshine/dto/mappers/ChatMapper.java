package com.shevtsov.sunshine.dto.mappers;


import com.shevtsov.sunshine.dao.UserDao;
import com.shevtsov.sunshine.dto.ChatDto;
import com.shevtsov.sunshine.dto.MessageDto;
import com.shevtsov.sunshine.dao.entities.Chat;
import com.shevtsov.sunshine.dao.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChatMapper extends ListMapper<ChatDto, Chat> {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserDao userDao;

    @Override
    public ChatDto toDto(Chat entity) {
        List<MessageDto> messageDtoList = new ArrayList<>();
        for (Message message : entity.getMessages()) {
            messageDtoList.add(messageMapper.toDto(message));
        }
        return new ChatDto(entity.getId(), entity.getChatType(), entity.getName(), messageDtoList);
    }
}
