package io.roa.secretmanger.DTO.projection;

import io.roa.secretmanger.Model.Value.UserRole;

import java.util.UUID;

public interface UserSummaryProjection {
    UUID getId();

    String getName();

    String getEmail();

    UserRole getRole();

    boolean getIsActive();
}
