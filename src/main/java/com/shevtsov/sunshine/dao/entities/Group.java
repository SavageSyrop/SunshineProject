package com.shevtsov.sunshine.dao.entities;

import com.shevtsov.sunshine.common.GroupWallType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "table_groups")
public class Group extends AbstractEntity {

    @Column
    private String name;

    @Column
    private String info;

    @Column
    @Enumerated(EnumType.STRING)
    private GroupWallType wallType;

    /**
     * определяет нужно ли одобрение администратора для вступления в группу
     */
    @Column
    private Boolean openToJoin;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<GroupMembership> groupMemberships = new ArrayList<>();

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("id asc")
    private List<Post> wall = new ArrayList<>();

    public Group(String name, String info, GroupWallType wallType, Boolean openToJoin) {
        this.name = name;
        this.info = info;
        this.wallType = wallType;
        this.openToJoin = openToJoin;
    }
}
