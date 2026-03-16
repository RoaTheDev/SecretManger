package io.roa.secretmanger.DTO.response.ApprovalRequest;

import io.roa.secretmanger.Model.Value.ApprovalStatus;

import java.util.UUID;

public record VoteCastResponse(
        UUID requestId,
        ApprovalStatus currentStatus,
        boolean quorumReached
) {
}
