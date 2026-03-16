package io.roa.secretmanger.Service.Impl;

import io.roa.secretmanger.Annotation.Audited;
import io.roa.secretmanger.Config.CacheConfig;
import io.roa.secretmanger.DTO.request.ApprovalRequest.AccessRequestedResponse;
import io.roa.secretmanger.DTO.request.ApprovalRequest.CastVoteRequest;
import io.roa.secretmanger.DTO.response.ApprovalRequest.ApprovalRequestSummary;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.ApprovalRequest.VoteCastResponse;
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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.access.project-tier-ttl-hours:1}")
    private int projectTierTtlHours;

    @Value("${app.access.admin-tier-ttl-hours:1}")
    private int adminTierTtlHours;

    @Transactional
    public AccessRequestedResponse requestAccess(UUID credentialId) {
        User currentUser = securityContext.getCurrentUser();

        approvalRequestRepo.findByCredentialIdAndRequestedByIdAndStatus(
                        credentialId, currentUser.getId(), ApprovalStatus.PENDING)
                .ifPresent(r -> {
                    throw new PendingRequestExistsException(
                            "You already have a pending request for this credential");
                });
        Credential credential = credentialRepo.findById(credentialId).orElseThrow(() -> new ResourceNotFoundException("Credential not found"));

        int quorum = calculateQuorum(credential);

        ApprovalRequest request = new ApprovalRequest();
        request.setCredential(credential);
        request.setRequestedBy(currentUser);
        request.setAccessTier(credential.getAccessTier());
        request.setQuorumRequired(quorum);
        request.setStatus(ApprovalStatus.PENDING);

        ApprovalRequest saved = approvalRequestRepo.save(request);

        return new AccessRequestedResponse(saved.getId(), saved.getStatus(), saved.getQuorumRequired());
    }

    @Audited(action = "VOTE_CAST", targetType = "APPROVAL_REQUEST")
    @Transactional
    @CacheEvict(value = CacheConfig.APPROVAL, key = "#requestId")
    public VoteCastResponse castVote(UUID requestId, CastVoteRequest voteRequest) {
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

        return new VoteCastResponse(request.getId(), request.getStatus(), request.isQuorumReached());
    }


    @Transactional(readOnly = true)
    public PageResponse<ApprovalRequestSummary> getPendingForCurrentUser(Pageable pageable) {
        UUID currentUserId = securityContext.getCurrentUserId();

        return PageResponse.of(
                approvalRequestRepo
                        .findPendingForVoter(currentUserId, pageable)
                        .map(projection -> {
                            ApprovalRequestSummary summary = approvalMapper.toSummary(projection);

                            long approveCount = approvalVoteRepo
                                    .countByRequestIdAndVote(projection.getId(), VoteChoice.APPROVE);
                            long rejectCount = approvalVoteRepo
                                    .countByRequestIdAndVote(projection.getId(), VoteChoice.REJECT);

                            return new ApprovalRequestSummary(
                                    summary.id(), summary.credentialId(), summary.credentialName(),
                                    summary.requestedBy(), summary.accessTier(), summary.status(),
                                    summary.quorumRequired(), approveCount, rejectCount,
                                    summary.createdAt()
                            );
                        })
        );
    }


    private void updateRequestStatus(ApprovalRequest request) {
        long approveCount = approvalVoteRepo
                .countByRequestIdAndVote(request.getId(), VoteChoice.APPROVE);
        long rejectCount = approvalVoteRepo
                .countByRequestIdAndVote(request.getId(), VoteChoice.REJECT);

        if (approveCount >= request.getQuorumRequired()) {
            request.setStatus(ApprovalStatus.APPROVED);
            request.setQuorumReached(true);
            request.setResolvedAt(LocalDateTime.now());

            int ttlHours = request.getAccessTier() == AccessTier.ADMIN
                    ? adminTierTtlHours
                    : projectTierTtlHours;
            request.setExpiresAt(LocalDateTime.now().plusHours(ttlHours));

        } else if (rejectCount > 0) {
            request.setStatus(ApprovalStatus.REJECTED);
            request.setResolvedAt(LocalDateTime.now());
        }

        approvalRequestRepo.save(request);
    }

    private int calculateQuorum(Credential credential) {
        if (credential.getAccessTier() == AccessTier.ADMIN) {
            return (int) userRepo.countByRole(UserRole.ADMIN);
        }

        long activeApprovers = userRepo.countActiveProjectApprovers();
        if (activeApprovers == 0) {
            throw new ValidationException(
                    "No active approvers available. Please contact your administrator.");
        }

        return switch (credential.getApprovalPolicy()) {
            case RELAXED  -> 1;
            case STANDARD -> (int) Math.min(2, activeApprovers);
            case STRICT   -> (int) Math.min(3, activeApprovers);
        };
    }
}