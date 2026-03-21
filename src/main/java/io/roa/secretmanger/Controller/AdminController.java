package io.roa.secretmanger.Controller;


import io.roa.secretmanger.Controller.docs.AdminEndpointDoc;
import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.DTO.request.Project.VoteDeletionRequest;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.AuditLogResponse;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Project.DeletionVoteStatus;
import io.roa.secretmanger.DTO.response.Project.ProjectDeletionVoteSummary;
import io.roa.secretmanger.DTO.response.Project.ProjectSummary;
import io.roa.secretmanger.DTO.response.Shamir.ShamirStatusResponse;
import io.roa.secretmanger.Mapper.AuditMapper;
import io.roa.secretmanger.Repo.AuditLogRepo;
import io.roa.secretmanger.Service.AdminService;
import io.roa.secretmanger.Service.ProjectService;
import io.roa.secretmanger.Service.ShamirService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController implements AdminEndpointDoc {

    private final AdminService adminService;
    private final ShamirService shamirService;
    private final AuditLogRepo auditLogRepo;
    private final AuditMapper auditMapper;
    private final ProjectService projectService;

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEAD','PROJECT_MANAGER')")
    public ApiRes<PageResponse<UserSummaryProjection>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiRes.success(adminService.getAllUsers(pageable));
    }


    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<ApiRes<Void>> deactivateUser(@PathVariable UUID userId) {
        adminService.deactivateUser(userId);
        return ResponseEntity.ok(ApiRes.success("Deactivation requested", null));
    }

    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<ApiRes<Void>> activateUser(@PathVariable UUID userId) {
        adminService.activateUser(userId);
        return ResponseEntity.ok(ApiRes.success("Activation requested", null));
    }


    @PostMapping("/shamir/init")
    public ApiRes<Void> initShamir() {
        shamirService.splitAndDistribute();
        return ApiRes.success("Master key split and distributed to all admins", null);
    }

    @GetMapping("/shamir/status")
    public ApiRes<ShamirStatusResponse> getShamirStatus() {
        return ApiRes.success(new ShamirStatusResponse(
                shamirService.isInitialized(),
                shamirService.getTotalShares()
        ));
    }


    @GetMapping("/audit-logs")
    public ApiRes<PageResponse<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @PageableDefault(size = 50, sort = "performedAt") Pageable pageable) {
        return ApiRes.success(
                PageResponse.of(
                        auditLogRepo.findFiltered(actorId, action, targetType, pageable)
                                .map(auditMapper::toDto)));
    }

    @DeleteMapping("/projects/{projectId}")
    public ApiRes<Void> deleteProject(
            @PathVariable UUID projectId,
            @RequestBody Set<UUID> adminIds) {
        projectService.delete(projectId, adminIds);
        return ApiRes.success("Project and all associated credentials deleted", null);
    }
    @GetMapping("/deletion-votes")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<List<ProjectDeletionVoteSummary>> getOngoingDeletionVotes() {
        return ApiRes.success(adminService.getOngoingDeletionVotes());
    }
    @GetMapping("/projects/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<PageResponse<ProjectSummary>> getAllProjects(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiRes.success(projectService.getAllProjects(pageable));
    }

    @PostMapping("/projects/{projectId}/deletion-vote")
    public ApiRes<DeletionVoteStatus> voteDeletion(
            @PathVariable UUID projectId,
            @Valid @RequestBody VoteDeletionRequest request) {
        return ApiRes.success(adminService.voteDeletion(projectId, request.password()));
    }

    @GetMapping("/projects/{projectId}/deletion-vote")
    public ApiRes<DeletionVoteStatus> getDeletionVoteStatus(@PathVariable UUID projectId) {
        return ApiRes.success(adminService.getVoteStatus(projectId));
    }
}
