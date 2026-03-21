package io.roa.secretmanger.DTO.response.ApprovalRequest;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalStatus;
import io.roa.secretmanger.Model.Value.ApprovalType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApprovalRequestSummary(
        UUID id,
        UUID credentialId,
        String credentialName,
        UUID targetUserId,
        String targetUserName,
        String requestedBy,
        AccessTier accessTier,
        ApprovalStatus status,
        ApprovalType type,
        int quorumRequired,
        long approveCount,
        long rejectCount,
        boolean hasVoted,
        LocalDateTime createdAt
) {}
