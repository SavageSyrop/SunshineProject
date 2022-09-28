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
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "posts")
public class Post extends AbstractEntity {

    @Column
    private String text;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime sendingTime;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "wall_id")
    private Wall wall;

    @Column(name = "is_published")
    private Boolean isPublished;


    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    protected List<Like> postLikes = new ArrayList<>();


    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    protected List<Comment> comments = new ArrayList<>();


    public Post(String text, User author, Wall wall) {
        this.text = text;
        this.sendingTime = LocalDateTime.now();
        this.author = author;
        this.wall = wall;
    }

    public Post(String text, User author, Group group) {
        this.text = text;
        this.sendingTime = LocalDateTime.now();
        this.author = author;
        this.group = group;
    }

    @PrePersist
    private void updatePublished() {
        if (isPublished == null) {
            isPublished = true;
        }
    }
}
