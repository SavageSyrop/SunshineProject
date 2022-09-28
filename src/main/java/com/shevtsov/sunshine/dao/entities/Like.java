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
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "likes")
public class Like extends AbstractEntity {

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime sendingTime;

    public Like(User user, Comment comment) {
        this.user = user;
        this.sendingTime = LocalDateTime.now();
        this.comment = comment;
    }

    public Like(User user, Post post) {
        this.user = user;
        this.sendingTime = LocalDateTime.now();
        this.post = post;
    }
}
