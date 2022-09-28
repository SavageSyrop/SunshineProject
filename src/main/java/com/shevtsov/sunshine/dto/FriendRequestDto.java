package com.shevtsov.sunshine.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequestDto extends AbstractDto {
    private Long senderId;
    private Long recipientId;
    private Boolean isAccepted;

    public FriendRequestDto(Long id, Long senderId, Long recipientId, Boolean isAccepted) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.isAccepted = isAccepted;
    }
}
