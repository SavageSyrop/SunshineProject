package com.shevtsov.sunshine.dao.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "friend_requests")
public class FriendRequest extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User senderUser;

    @OneToOne
    @JoinColumn(name = "recipient_id")
    private User recipientUser;

    @Column
    private Boolean isAccepted;

    public FriendRequest(User senderUser, User recipientUser, Boolean isAccepted) {
        this.senderUser = senderUser;
        this.recipientUser = recipientUser;
        this.isAccepted = isAccepted;
    }
}
