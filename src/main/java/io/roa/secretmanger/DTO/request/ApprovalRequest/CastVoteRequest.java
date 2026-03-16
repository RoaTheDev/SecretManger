package io.roa.secretmanger.DTO.request.ApprovalRequest;

import io.roa.secretmanger.Model.Value.VoteChoice;
import jakarta.validation.constraints.NotNull;

public record CastVoteRequest(
        @NotNull
        VoteChoice vote
) {
}
