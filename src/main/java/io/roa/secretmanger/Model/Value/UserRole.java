package io.roa.secretmanger.Model.Value;


import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum UserRole {

    DEVELOPER(Set.of(
            "ROLE_DEVELOPER",
            "credential:read",
            "credential:request"
    )),

    PROJECT_MANAGER(Set.of(
            "ROLE_PROJECT_MANAGER",
            "credential:read",
            "credential:request",
            "approval:vote:project"
    )),

    TEAM_LEAD(Set.of(
            "ROLE_TEAM_LEAD",
            "credential:read",
            "credential:request",
            "approval:vote:project"
    )),

    ADMIN(Set.of(
            "ROLE_ADMIN",
            "credential:create",
            "credential:read",
            "credential:request",
            "approval:vote:project",
            "approval:vote:admin",
            "user:manage",
            "shamir:manage",
            "audit:read"
    ));

    private final Set<String> authorities;

    UserRole(Set<String> authorities) {
        this.authorities = authorities;
    }

    public Set<GrantedAuthority> getGrantedAuthorities() {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}