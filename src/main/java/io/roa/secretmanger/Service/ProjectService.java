package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.request.ProjectDto;
import io.roa.secretmanger.DTO.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectService {
    ProjectDto.ProjectCreatedResponse create(ProjectDto.CreateProjectRequest request);

    void addMember(UUID projectId, ProjectDto.AddMemberRequest request);

    void removeMember(UUID projectId, UUID userId);

    PageResponse<ProjectDto.ProjectSummary> getMyProjects(Pageable pageable);

    ProjectDto.ProjectDetail getDetail(UUID projectId);

    boolean isMember(UUID projectId, UUID userId);
}
