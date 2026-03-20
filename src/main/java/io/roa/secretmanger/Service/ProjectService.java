package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.request.Project.AddMemberRequest;
import io.roa.secretmanger.DTO.request.Project.CreateProjectRequest;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Project.ProjectCreatedResponse;
import io.roa.secretmanger.DTO.response.Project.ProjectDetail;
import io.roa.secretmanger.DTO.response.Project.ProjectSummary;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface ProjectService {
    ProjectCreatedResponse create(CreateProjectRequest request);

    void addMember(UUID projectId, AddMemberRequest request);

    void removeMember(UUID projectId, UUID userId);

    PageResponse<ProjectSummary> getMyProjects(Pageable pageable);
    PageResponse<ProjectSummary> getAllProjects(Pageable pageable);
    ProjectDetail getDetail(UUID projectId);
    void delete(UUID projectId, Set<UUID> adminIds);
    boolean isMember(UUID projectId, UUID userId);
    void executeProjectDeletion(UUID projectId);

}
