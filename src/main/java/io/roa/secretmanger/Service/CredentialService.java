package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.request.ApprovalRequest.CreateCredentialRequest;
import io.roa.secretmanger.DTO.response.*;
import io.roa.secretmanger.DTO.response.Shamir.CredentialCreatedResponse;
import io.roa.secretmanger.DTO.response.Shamir.CredentialDetail;
import io.roa.secretmanger.DTO.response.Shamir.CredentialRevealResponse;
import io.roa.secretmanger.DTO.response.Shamir.CredentialSummary;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CredentialService {
    CredentialCreatedResponse create(CreateCredentialRequest request);
    PageResponse<CredentialSummary> listByProject(UUID projectId, Pageable pageable);
    CredentialDetail getDetail(UUID credentialId);
    CredentialRevealResponse reveal(UUID credentialId);
    void delete(UUID credentialId);
}
