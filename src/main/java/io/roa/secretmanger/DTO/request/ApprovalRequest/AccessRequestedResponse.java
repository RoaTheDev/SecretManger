package io.roa.secretmanger.DTO.request.ApprovalRequest;

import io.roa.secretmanger.Model.Value.ApprovalStatus;

import java.util.UUID;

public record AccessRequestedResponse(
        UUID requestId,
        ApprovalStatus status,
        int quorumRequired
) {
}
