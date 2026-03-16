package io.roa.secretmanger.Controller;


import io.roa.secretmanger.DTO.request.Project.AddMemberRequest;
import io.roa.secretmanger.DTO.request.Project.CreateProjectRequest;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Project.ProjectCreatedResponse;
import io.roa.secretmanger.DTO.response.Project.ProjectDetail;
import io.roa.secretmanger.DTO.response.Project.ProjectSummary;
import io.roa.secretmanger.Service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<ProjectCreatedResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        return ApiRes.success("Project created", projectService.create(request));
    }

    @PostMapping("/{projectId}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<Void> addMember(@PathVariable UUID projectId,
                                  @Valid @RequestBody AddMemberRequest request) {
        projectService.addMember(projectId, request);
        return ApiRes.success("Member added", null);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiRes<Void> removeMember(@PathVariable UUID projectId,
                                     @PathVariable UUID userId) {
        projectService.removeMember(projectId, userId);
        return ApiRes.success("Member removed", null);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiRes<PageResponse<ProjectSummary>> getMyProjects(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiRes.success(projectService.getMyProjects(pageable));
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ApiRes<ProjectDetail> getDetail(@PathVariable UUID projectId) {
        return ApiRes.success(projectService.getDetail(projectId));
    }
}
