package io.roa.secretmanger.Controller;

import io.roa.secretmanger.Controller.docs.CredentialEndpointDoc;
import io.roa.secretmanger.DTO.response.ApprovalRequest.AccessRequestedResponse;
import io.roa.secretmanger.DTO.request.ApprovalRequest.CreateCredentialRequest;
import io.roa.secretmanger.DTO.request.Project.UpdateCredentialRequest;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Shamir.CredentialCreatedResponse;
import io.roa.secretmanger.DTO.response.Shamir.CredentialDetail;
import io.roa.secretmanger.DTO.response.Shamir.CredentialRevealResponse;
import io.roa.secretmanger.DTO.response.Shamir.CredentialSummary;
import io.roa.secretmanger.Service.ApprovalService;
import io.roa.secretmanger.Service.CredentialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
public class CredentialController implements CredentialEndpointDoc {

    private final CredentialService credentialService;
    private final ApprovalService approvalService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<CredentialCreatedResponse> create(@Valid @RequestBody CreateCredentialRequest request) {
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
    public ApiRes<AccessRequestedResponse> requestAccess(@PathVariable UUID credentialId) {
        return ApiRes.success("Access request submitted", approvalService.requestAccess(credentialId));
    }

    @GetMapping("/{credentialId}/reveal")
    @PreAuthorize("isAuthenticated()")
    public ApiRes<CredentialRevealResponse> reveal(@PathVariable UUID credentialId) {
        return ApiRes.success(credentialService.reveal(credentialId));
    }

    @PatchMapping("/{credentialId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEAD')")
    public ResponseEntity<ApiRes<CredentialDetail>> update(
            @PathVariable UUID credentialId,
            @RequestBody @Valid UpdateCredentialRequest request) {
        return ResponseEntity.ok(ApiRes.success(credentialService.update(credentialId, request)));
    }
    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ApiRes<PageResponse<CredentialSummary>> listByProject(@PathVariable UUID projectId,
                                                                 @ParameterObject
                                                                 @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiRes.success(credentialService.listByProject(projectId, pageable));
    }

    @GetMapping("/{credentialId}")
    @PreAuthorize("isAuthenticated()")
    public ApiRes<CredentialDetail> getDetail(@PathVariable UUID credentialId) {
        return ApiRes.success(credentialService.getDetail(credentialId));
    }
}
