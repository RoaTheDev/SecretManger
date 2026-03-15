package io.roa.secretmanger.Controller;

import io.roa.secretmanger.DTO.request.ApprovalDto;
import io.roa.secretmanger.DTO.request.CredentialDto;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.Service.ApprovalService;
import io.roa.secretmanger.Service.CredentialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
public class CredentialController {

    private final CredentialService credentialService;
    private final ApprovalService   approvalService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<CredentialDto.CredentialCreatedResponse> create(
            @Valid @RequestBody CredentialDto.CreateCredentialRequest request) {
        return ApiRes.success("Credential created", credentialService.create(request));
    }

    @DeleteMapping("/{credentialId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<Void> delete(@PathVariable UUID credentialId) {
        credentialService.delete(credentialId);
        return ApiRes.success("Credential deleted", null);
    }

    @PostMapping("/{credentialId}/request-access")
    @PreAuthorize("isAuthenticated()")
    public ApiRes<ApprovalDto.AccessRequestedResponse> requestAccess(@PathVariable UUID credentialId) {
        return ApiRes.success("Access request submitted",
                approvalService.requestAccess(credentialId));
    }

    @GetMapping("/{credentialId}/reveal")
    @PreAuthorize("isAuthenticated()")
    public ApiRes<CredentialDto.CredentialRevealResponse> reveal(@PathVariable UUID credentialId) {
        return ApiRes.success(credentialService.reveal(credentialId));
    }


    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ApiRes<PageResponse<CredentialDto.CredentialSummary>> listByProject(
            @PathVariable UUID projectId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiRes.success(credentialService.listByProject(projectId, pageable));
    }

    @GetMapping("/{credentialId}")
    @PreAuthorize("isAuthenticated()")
    public ApiRes<CredentialDto.CredentialDetail> getDetail(@PathVariable UUID credentialId) {
        return ApiRes.success(credentialService.getDetail(credentialId));
    }
}
