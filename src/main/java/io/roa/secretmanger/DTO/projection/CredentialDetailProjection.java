package io.roa.secretmanger.DTO.projection;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.CredentialType;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CredentialDetailProjection {
    UUID getId();

    String getName();

    CredentialType getType();

    AccessTier getAccessTier();

    LocalDateTime getCreatedAt();

    CreatedByProjection getCreatedBy();

    interface CreatedByProjection {
        String getName();

        String getEmail();
    }
}
