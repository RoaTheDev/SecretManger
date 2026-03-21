package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.response.ApprovalRequest.AccessRequestedResponse;
import io.roa.secretmanger.DTO.request.ApprovalRequest.CastVoteRequest;
import io.roa.secretmanger.DTO.response.ApprovalRequest.ApprovalRequestSummary;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.ApprovalRequest.VoteCastResponse;
import io.roa.secretmanger.Model.Value.ApprovalType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ApprovalService {
    AccessRequestedResponse requestAccess(UUID credentialId);
    VoteCastResponse castVote(UUID requestId, CastVoteRequest voteRequest);
    PageResponse<ApprovalRequestSummary> getPendingForCurrentUser(Pageable pageable);
    AccessRequestedResponse requestUserAction(UUID targetUserId, ApprovalType type);
}
