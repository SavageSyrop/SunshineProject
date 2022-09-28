package com.shevtsov.sunshine.dao.entities;

import com.shevtsov.sunshine.common.UserWallPermissionType;
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
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "walls")
public class Wall extends AbstractEntity {
    @Column
    @Enumerated(EnumType.STRING)
    private UserWallPermissionType postPermission;

    @Column
    @Enumerated(EnumType.STRING)
    private UserWallPermissionType commentPermission;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    @OrderBy("id asc")
    private List<Post> posts;

    public Wall(UserWallPermissionType postPermission, UserWallPermissionType commentPermission) {
        this.postPermission = postPermission;
        this.commentPermission = commentPermission;
    }
}
