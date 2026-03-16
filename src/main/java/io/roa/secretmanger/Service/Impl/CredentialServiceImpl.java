package io.roa.secretmanger.Service.Impl;

import io.roa.secretmanger.Annotation.Audited;
import io.roa.secretmanger.Config.CacheConfig;
import io.roa.secretmanger.DTO.request.ApprovalRequest.CreateCredentialRequest;
import io.roa.secretmanger.DTO.response.*;
import io.roa.secretmanger.DTO.response.Shamir.CredentialCreatedResponse;
import io.roa.secretmanger.DTO.response.Shamir.CredentialDetail;
import io.roa.secretmanger.DTO.response.Shamir.CredentialRevealResponse;
import io.roa.secretmanger.DTO.response.Shamir.CredentialSummary;
import io.roa.secretmanger.Exception.ProjectAccessDeniedException;
import io.roa.secretmanger.Exception.QuorumNotReachedException;
import io.roa.secretmanger.Exception.ResourceNotFoundException;
import io.roa.secretmanger.Mapper.CredentialMapper;
import io.roa.secretmanger.Model.Entity.Credential;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Model.Value.ApprovalStatus;
import io.roa.secretmanger.Repo.ApprovalRequestRepo;
import io.roa.secretmanger.Repo.CredentialRepo;
import io.roa.secretmanger.Repo.ProjectRepo;
import io.roa.secretmanger.Service.CredentialService;
import io.roa.secretmanger.Service.CryptoService;
import io.roa.secretmanger.Service.ProjectService;
import io.roa.secretmanger.Util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final CredentialRepo credentialRepo;
    private final ProjectRepo projectRepo;
    private final ApprovalRequestRepo approvalRequestRepo;
    private final CryptoService cryptoService;
    private final CredentialMapper credentialMapper;
    private final SecurityContextUtil securityContext;
    private final ProjectService projectService;

    @Transactional
    public CredentialCreatedResponse create(CreateCredentialRequest request) {
        User currentUser = securityContext.getCurrentUser();

        var project = projectRepo.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Credential credential = new Credential();
        credential.setProject(project);
        credential.setName(request.name());
        credential.setType(request.type());
        credential.setEncryptedValue(cryptoService.encrypt(request.value()));
        credential.setAccessTier(request.accessTier());
        credential.setCreatedBy(currentUser);

        return new CredentialCreatedResponse(credentialRepo.save(credential).getId());
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CREDENTIAL, key = "#credentialId")
    public void delete(UUID credentialId) {
        credentialRepo.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found"));
        credentialRepo.deleteById(credentialId);
    }


    @Transactional(readOnly = true)
    public PageResponse<CredentialSummary> listByProject(UUID projectId, Pageable pageable) {
        guardMembership(projectId);
        return PageResponse.of(
                credentialRepo.findSummariesByProject(projectId, pageable)
                        .map(credentialMapper::toSummary)
        );
    }

    @Cacheable(value = CacheConfig.CREDENTIAL, key = "#credentialId")
    @Transactional(readOnly = true)
    public CredentialDetail getDetail(UUID credentialId) {
        var projection = credentialRepo.findDetailById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found"));

        guardMembership(projection.getId());
        return credentialMapper.toDetail(projection);
    }

    @Audited(action = "CREDENTIAL_ACCESSED", targetType = "CREDENTIAL")
    @Transactional(readOnly = true)
    public CredentialRevealResponse reveal(UUID credentialId) {
        UUID currentUserId = securityContext.getCurrentUserId();

        var approvedRequest = approvalRequestRepo
                .findByCredentialIdAndRequestedByIdAndStatus(
                        credentialId, currentUserId, ApprovalStatus.APPROVED)
                .orElseThrow(() -> new QuorumNotReachedException(
                        "Access not approved yet. Submit a request and wait for quorum."));

        if (approvedRequest.getExpiresAt() != null
                && LocalDateTime.now().isAfter(approvedRequest.getExpiresAt())) {
            throw new QuorumNotReachedException(
                    "Your access has expired. Please submit a new request.");
        }

        Credential credential = credentialRepo.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found"));

        return new CredentialRevealResponse(
                credential.getId(),
                credential.getName(),
                credential.getType(),
                cryptoService.decrypt(credential.getEncryptedValue()),
                approvedRequest.getExpiresAt()
        );
    }

    private void guardMembership(UUID projectId) {
        UUID userId = securityContext.getCurrentUserId();
        if (!projectService.isMember(projectId, userId)) {
            throw new ProjectAccessDeniedException("You are not a member of this project");
        }
    }
}
