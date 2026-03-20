package io.roa.secretmanger.Model.Entity;


import io.roa.secretmanger.Model.Value.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_role", columnList = "role")
})
@Getter
@Setter
@FieldNameConstants
public class User extends BaseEntity implements UserDetails {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;


    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Credential> credentials;

    @OneToMany(mappedBy = "requestedBy", fetch = FetchType.LAZY)
    private List<ApprovalRequest> approvalRequests;

    @OneToMany(mappedBy = "voter", fetch = FetchType.LAZY)
    private List<ApprovalVote> votes;

//    @OneToOne(mappedBy = "admin", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    private ShamirShare shamirShare;

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getGrantedAuthorities();
    }

    @Override
    public @NonNull String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}