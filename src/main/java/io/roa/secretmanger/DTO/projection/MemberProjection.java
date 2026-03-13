package io.roa.secretmanger.DTO.projection;

import io.roa.secretmanger.Model.Value.UserRole;

import java.util.UUID;

public interface MemberProjection {
    UUID getId();

    String getName();

    String getEmail();

    UserRole getRole();
}
