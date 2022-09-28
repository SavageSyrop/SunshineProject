package com.shevtsov.sunshine.dto.mappers;

import com.shevtsov.sunshine.dto.FriendRequestDto;
import com.shevtsov.sunshine.dao.entities.FriendRequest;
import org.springframework.stereotype.Component;

@Component
public class FriendRequestMapper extends ListMapper<FriendRequestDto, FriendRequest>{
    @Override
    public FriendRequestDto toDto(FriendRequest entity) {
        return new FriendRequestDto(entity.getId(), entity.getSenderUser().getId(), entity.getRecipientUser().getId(),entity.getIsAccepted());
    }
}
