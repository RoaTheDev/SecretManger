package io.roa.secretmanger.DTO.response.ApprovalRequest;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApprovalRequestSummary(
        UUID id,
        UUID credentialId,
        String credentialName,
        String requestedBy,
        AccessTier accessTier,
        ApprovalStatus status,
        int quorumRequired,
        long approveCount,
        long rejectCount,
        LocalDateTime createdAt
) {
}
