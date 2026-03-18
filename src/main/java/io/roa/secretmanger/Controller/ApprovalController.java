package io.roa.secretmanger.Controller;


import io.roa.secretmanger.Controller.docs.ApprovalEndpointDoc;
import io.roa.secretmanger.DTO.request.ApprovalRequest.CastVoteRequest;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.ApprovalRequest.ApprovalRequestSummary;
import io.roa.secretmanger.DTO.response.ApprovalRequest.VoteCastResponse;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.Service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController implements ApprovalEndpointDoc {

    private final ApprovalService approvalService;


    @PostMapping("/{requestId}/vote")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD', 'PROJECT_MANAGER')")
    public ApiRes<VoteCastResponse> castVote(@PathVariable UUID requestId,
                                             @Valid @RequestBody CastVoteRequest request) {
        return ApiRes.success("Vote cast", approvalService.castVote(requestId, request));
    }


    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD', 'PROJECT_MANAGER')")
    public ApiRes<PageResponse<ApprovalRequestSummary>> getPending(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiRes.success(approvalService.getPendingForCurrentUser(pageable));
    }
}
