package io.roa.secretmanger.Controller;


import io.roa.secretmanger.DTO.response.AuditLogResponse;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Shamir.ShamirStatusResponse;
import io.roa.secretmanger.Service.AdminService;
import io.roa.secretmanger.Service.ShamirService;
import io.roa.secretmanger.Repo.AuditLogRepo;
import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.Mapper.AuditMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ShamirService shamirService;
    private final AuditLogRepo  auditLogRepo;
    private final AuditMapper   auditMapper;

    @GetMapping("/users")
    public ApiRes<PageResponse<UserSummaryProjection>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiRes.success(adminService.getAllUsers(pageable));
    }

    @PatchMapping("/users/{userId}/deactivate")
    public ApiRes<Void> deactivateUser(@PathVariable UUID userId) {
        adminService.deactivateUser(userId);
        return ApiRes.success("User deactivated", null);
    }

    @PatchMapping("/users/{userId}/activate")
    public ApiRes<Void> activateUser(@PathVariable UUID userId) {
        adminService.activateUser(userId);
        return ApiRes.success("User activated", null);
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
}