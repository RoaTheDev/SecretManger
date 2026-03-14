package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.request.CredentialDto;
import io.roa.secretmanger.DTO.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CredentialService {
    CredentialDto.CredentialCreatedResponse create(CredentialDto.CreateCredentialRequest request);
    PageResponse<CredentialDto.CredentialSummary> listByProject(UUID projectId, Pageable pageable);
    CredentialDto.CredentialDetail getDetail(UUID credentialId);
    CredentialDto.CredentialRevealResponse reveal(UUID credentialId);
    void delete(UUID credentialId);
}
