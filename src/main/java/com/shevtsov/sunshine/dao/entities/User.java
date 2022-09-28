package com.shevtsov.sunshine.dao.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Entity
@Table(name = "users")
@Getter
@ToString
@Setter
@NoArgsConstructor
public class User extends AbstractEntity implements UserDetails {

    @OneToOne
    @JoinColumn(name = "user_info_id")
    private UserInfo userInfo;

    @OneToOne
    @JoinColumn(name = "wall_id")
    private Wall wall;

    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;

    /**
     * код активации аккаунта, устанавливаемый при создании пользователя или изменении эмейла
     */
    @Column
    private String activationCode;


    /**
     * уникальный код восстановления пароля, устанавливаемый по запросу
     */
    @Column
    private String restorePasswordCode;

    @Column
    private Boolean isBanned;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ChatParticipation> chatParticipations;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<GroupMembership> groups;

    @OneToMany(mappedBy = "senderUser", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<FriendRequest> friendRequests;


    public User(UserInfo userInfo, Wall wall, Role role) {
        this.userInfo = userInfo;
        this.role = role;
        this.wall = wall;
        this.isBanned = false;
    }

    public Boolean isOpenUser() {
        return userInfo.getOpenProfile();
    }

    public String getFullName() {
        return userInfo.getFirstName() + " " + userInfo.getFirstName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Permission permission : this.getRole().getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getName().name()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return userInfo.getPassword();
    }

    @Override
    public String getUsername() {
        return userInfo.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
