package io.roa.secretmanger.Service.Impl;

import io.roa.secretmanger.Annotation.Audited;
import io.roa.secretmanger.Config.CacheConfig;
import io.roa.secretmanger.DTO.request.ApprovalDto;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.Exception.*;
import io.roa.secretmanger.Mapper.ApprovalMapper;
import io.roa.secretmanger.Model.Entity.ApprovalRequest;
import io.roa.secretmanger.Model.Entity.ApprovalVote;
import io.roa.secretmanger.Model.Entity.Credential;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalStatus;
import io.roa.secretmanger.Model.Value.UserRole;
import io.roa.secretmanger.Model.Value.VoteChoice;
import io.roa.secretmanger.Repo.ApprovalRequestRepo;
import io.roa.secretmanger.Repo.ApprovalVoteRepo;
import io.roa.secretmanger.Repo.CredentialRepo;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.ApprovalService;
import io.roa.secretmanger.Util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRequestRepo approvalRequestRepo;
    private final ApprovalVoteRepo approvalVoteRepo;
    private final CredentialRepo credentialRepo;
    private final UserRepo userRepo;
    private final ApprovalMapper approvalMapper;
    private final SecurityContextUtil securityContext;


    @Transactional
    public ApprovalDto.AccessRequestedResponse requestAccess(UUID credentialId) {
        User currentUser = securityContext.getCurrentUser();

        approvalRequestRepo.findByCredentialIdAndRequestedByIdAndStatus(credentialId, currentUser.getId(), ApprovalStatus.PENDING).ifPresent(r -> {
            throw new PendingRequestExistsException("You already have a pending request for this credential");
        });

        Credential credential = credentialRepo.findById(credentialId).orElseThrow(() -> new ResourceNotFoundException("Credential not found"));

        int quorum = calculateQuorum(credential.getAccessTier());

        ApprovalRequest request = new ApprovalRequest();
        request.setCredential(credential);
        request.setRequestedBy(currentUser);
        request.setAccessTier(credential.getAccessTier());
        request.setQuorumRequired(quorum);
        request.setStatus(ApprovalStatus.PENDING);

        ApprovalRequest saved = approvalRequestRepo.save(request);

        return new ApprovalDto.AccessRequestedResponse(saved.getId(), saved.getStatus(), saved.getQuorumRequired());
    }

    @Audited(action = "VOTE_CAST", targetType = "APPROVAL_REQUEST")
    @Transactional
    @CacheEvict(value = CacheConfig.APPROVAL, key = "#requestId")
    public ApprovalDto.VoteCastResponse castVote(UUID requestId, ApprovalDto.CastVoteRequest voteRequest) {
        User currentUser = securityContext.getCurrentUser();

        if (approvalVoteRepo.existsByRequestIdAndVoterId(requestId, currentUser.getId())) {
            throw new AlreadyVotedException("You have already voted on this request");
        }

        ApprovalRequest request = approvalRequestRepo.findById(requestId).orElseThrow(() -> new ResourceNotFoundException("Approval request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new ValidationException("This request is no longer pending");
        }

        if (request.getRequestedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You cannot vote on your own request");
        }

        ApprovalVote vote = new ApprovalVote();
        vote.setRequest(request);
        vote.setVoter(currentUser);
        vote.setVote(voteRequest.vote());
        approvalVoteRepo.save(vote);

        updateRequestStatus(request);

        return new ApprovalDto.VoteCastResponse(request.getId(), request.getStatus(), request.isQuorumReached());
    }


    @Transactional(readOnly = true)
    public PageResponse<ApprovalDto.ApprovalRequestSummary> getPendingForCurrentUser(Pageable pageable) {
        UUID currentUserId = securityContext.getCurrentUserId();

        return PageResponse.of(approvalRequestRepo.findPendingForVoter(currentUserId, pageable).map(projection -> {
            ApprovalDto.ApprovalRequestSummary summary = approvalMapper.toSummary(projection);

            long approveCount = approvalVoteRepo.countByRequestIdAndVote(projection.getId(), VoteChoice.APPROVE);
            long rejectCount = approvalVoteRepo.countByRequestIdAndVote(projection.getId(), VoteChoice.REJECT);

            return new ApprovalDto.ApprovalRequestSummary(summary.id(), summary.credentialId(), summary.credentialName(), summary.requestedBy(), summary.accessTier(), summary.status(), summary.quorumRequired(), approveCount, rejectCount, summary.createdAt());
        }));
    }


    private void updateRequestStatus(ApprovalRequest request) {
        long approveCount = approvalVoteRepo.countByRequestIdAndVote(request.getId(), VoteChoice.APPROVE);
        long rejectCount = approvalVoteRepo.countByRequestIdAndVote(request.getId(), VoteChoice.REJECT);

        if (approveCount >= request.getQuorumRequired()) {
            request.setStatus(ApprovalStatus.APPROVED);
            request.setQuorumReached(true);
            request.setResolvedAt(LocalDateTime.now());
        } else if (rejectCount > 0) {
            // Any single rejection kills the request
            request.setStatus(ApprovalStatus.REJECTED);
            request.setResolvedAt(LocalDateTime.now());
        }

        approvalRequestRepo.save(request);
    }

    private int calculateQuorum(AccessTier tier) {
        if (tier == AccessTier.ADMIN) {
            return (int) userRepo.countByRole(UserRole.ADMIN);
        }
        return 3; // TEAM_LEAD + PROJECT_MANAGER + 1 ADMIN
    }
}