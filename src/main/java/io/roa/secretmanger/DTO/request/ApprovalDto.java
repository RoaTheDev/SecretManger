package io.roa.secretmanger.DTO.request;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalStatus;
import io.roa.secretmanger.Model.Value.VoteChoice;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class ApprovalDto{


    public record CastVoteRequest(
            @NotNull
            VoteChoice vote
    ) {}

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
    ) {}

    public record VoteCastResponse(
            UUID requestId,
            ApprovalStatus currentStatus,
            boolean quorumReached
    ) {}

    public record AccessRequestedResponse(
            UUID requestId,
            ApprovalStatus status,
            int quorumRequired
    ) {}
}