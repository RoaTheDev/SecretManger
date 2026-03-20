package io.roa.secretmanger.DTO.projection;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalPolicy;
import io.roa.secretmanger.Model.Value.CredentialType;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CredentialDetailProjection {
    UUID getId();
    UUID getProjectId();
    String getName();
    CredentialType getType();
    AccessTier getAccessTier();
    ApprovalPolicy getApprovalPolicy();
    LocalDateTime getCreatedAt();
    CreatedByProjection getCreatedBy();

    interface CreatedByProjection {
        String getName();
        String getEmail();
    }
}