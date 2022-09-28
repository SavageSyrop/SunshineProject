package com.shevtsov.sunshine.dto.mappers;

import com.shevtsov.sunshine.dto.LikeDto;
import com.shevtsov.sunshine.dao.entities.Like;
import org.springframework.stereotype.Component;

@Component
public class LikeMapper extends ListMapper<LikeDto, Like> {
    @Override
    public LikeDto toDto(Like entity) {
        return new LikeDto(entity.getId(), entity.getUser().getUsername(), entity.getSendingTime());
    }
}
