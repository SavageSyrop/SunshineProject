package com.shevtsov.sunshine.dao.entities;


import com.shevtsov.sunshine.common.ChatType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chats")
public class Chat extends AbstractEntity {

    @Column
    @Enumerated(EnumType.STRING)
    private ChatType chatType;

    @Column
    private String name;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "chat", cascade = CascadeType.REMOVE)
    private List<Message> messages;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    public Chat(ChatType chatType, String name, User owner) {
        this.chatType = chatType;
        this.name = name;
        this.owner = owner;
    }
}
