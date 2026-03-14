package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.request.ApprovalDto;
import io.roa.secretmanger.DTO.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ApprovalService {
    ApprovalDto.AccessRequestedResponse requestAccess(UUID credentialId);
    ApprovalDto.VoteCastResponse castVote(UUID requestId, ApprovalDto.CastVoteRequest voteRequest);
    PageResponse<ApprovalDto.ApprovalRequestSummary> getPendingForCurrentUser(Pageable pageable);
}
