package com.shevtsov.sunshine.dao.entities;

import com.shevtsov.sunshine.common.GroupRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "group_memberships")
public class GroupMembership extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column
    @Enumerated(EnumType.STRING)
    private GroupRole groupRole;

    public GroupMembership(User currentUser, Group group, GroupRole role) {
        this.user = currentUser;
        this.group = group;
        this.groupRole = role;
    }
}
