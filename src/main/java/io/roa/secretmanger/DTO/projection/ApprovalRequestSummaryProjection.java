package io.roa.secretmanger.DTO.projection;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalStatus;
import io.roa.secretmanger.Model.Value.ApprovalType;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ApprovalRequestSummaryProjection {
    UUID getId();
    AccessTier getAccessTier();
    ApprovalStatus getStatus();
    ApprovalType getType();
    int getQuorumRequired();
    boolean getQuorumReached();
    LocalDateTime getCreatedAt();
    LocalDateTime getResolvedAt();
    UUID getTargetUserId();
    TargetUserProjection getTargetUser();
    CredentialInfoProjection getCredential();
    RequesterProjection getRequestedBy();

    interface CredentialInfoProjection {
        UUID getId();
        String getName();
    }

    interface RequesterProjection {
        UUID getId();
        String getName();
        String getEmail();
    }

    interface TargetUserProjection {
        UUID getId();
        String getName();
        String getEmail();
    }
}
