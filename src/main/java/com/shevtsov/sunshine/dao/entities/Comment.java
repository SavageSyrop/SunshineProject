package com.shevtsov.sunshine.dao.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "comments")
public class Comment extends AbstractEntity {

    @OneToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column
    private String text;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime sendingTime;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;


    @OneToMany(mappedBy = "comment",fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Like> commentLikes = new ArrayList<>();

    public Comment(User author, String text, Post post) {
        this.author = author;
        this.text = text;
        this.sendingTime = LocalDateTime.now();
        this.post = post;
    }
}