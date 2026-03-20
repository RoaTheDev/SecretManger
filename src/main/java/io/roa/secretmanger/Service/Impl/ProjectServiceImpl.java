package io.roa.secretmanger.Service.Impl;

import io.roa.secretmanger.Annotation.Audited;
import io.roa.secretmanger.Config.CacheConfig;
import io.roa.secretmanger.DTO.request.Project.AddMemberRequest;
import io.roa.secretmanger.DTO.request.Project.CreateProjectRequest;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Project.MemberSummary;
import io.roa.secretmanger.DTO.response.Project.ProjectCreatedResponse;
import io.roa.secretmanger.DTO.response.Project.ProjectDetail;
import io.roa.secretmanger.DTO.response.Project.ProjectSummary;
import io.roa.secretmanger.Exception.DuplicateResourceException;
import io.roa.secretmanger.Exception.ResourceNotFoundException;
import io.roa.secretmanger.Mapper.ProjectMapper;
import io.roa.secretmanger.Model.Entity.Project;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Repo.ProjectRepo;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.ProjectService;
import io.roa.secretmanger.Service.ShamirService;
import io.roa.secretmanger.Util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;
    private final ProjectMapper projectMapper;
    private final SecurityContextUtil securityContext;
    private final ShamirService shamirService;
    private final CacheManager cacheManager;

    @Transactional
    public ProjectCreatedResponse create(CreateProjectRequest request) {
        User currentUser = securityContext.getCurrentUser();

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setCreatedBy(currentUser);
        project.setMembers(List.of(currentUser));

        return new ProjectCreatedResponse(projectRepo.save(project).getId());
    }

    @Transactional
    @CacheEvict(value = CacheConfig.MEMBERSHIP, key = "#projectId + ':' + #request.userId()")
    public void addMember(UUID projectId, AddMemberRequest request) {
        Project project = findProjectOrThrow(projectId);

        User user = userRepo.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (project.getMembers().stream().anyMatch(m -> m.getId().equals(request.userId()))) {
            throw new DuplicateResourceException("User is already a member");
        }

        project.getMembers().add(user);
        projectRepo.save(project);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.MEMBERSHIP, key = "#projectId + ':' + #userId")
    public void removeMember(UUID projectId, UUID userId) {
        Project project = findProjectOrThrow(projectId);
        project.getMembers().removeIf(m -> m.getId().equals(userId));
        projectRepo.save(project);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectSummary> getMyProjects(Pageable pageable) {
        UUID currentUserId = securityContext.getCurrentUserId();
        return PageResponse.of(
                projectRepo.findSummariesForUser(currentUserId, pageable)
                        .map(projectMapper::toSummary)
        );
    }

    @Transactional(readOnly = true)
    public ProjectDetail getDetail(UUID projectId) {
        Project project = findProjectOrThrow(projectId);

        List<MemberSummary> members = projectRepo
                .findMembersByProjectId(projectId)
                .stream()
                .map(projectMapper::toMemberSummary)
                .toList();

        return new ProjectDetail(
                project.getId(),
                project.getName(),
                project.getDescription(),
                members,
                project.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectSummary> getAllProjects(Pageable pageable) {
        return PageResponse.of(
                projectRepo.findAllSummaries(pageable)
                        .map(projectMapper::toSummary)
        );
    }
    @Cacheable(value = CacheConfig.MEMBERSHIP, key = "#projectId + ':' + #userId")
    public boolean isMember(UUID projectId, UUID userId) {
        return projectRepo.isMember(projectId, userId);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.MEMBERSHIP, allEntries = true)
    public void delete(UUID projectId, Set<UUID> adminIds) {
        findProjectOrThrow(projectId);

        shamirService.reconstructMasterKey(adminIds);

        projectRepo.deleteMembersByProjectId(projectId);
        projectRepo.deleteCredentialsByProjectId(projectId);
        projectRepo.deleteById(projectId);
    }

    @Audited(action = "PROJECT_DELETION_APPROVED", targetType = "PROJECT")
    @Transactional
    @CacheEvict(value = CacheConfig.MEMBERSHIP, allEntries = true)
    public void executeProjectDeletion(UUID projectId,Set<UUID> adminIds) {
        shamirService.reconstructMasterKey(adminIds);
        projectRepo.deleteMembersByProjectId(projectId);
        projectRepo.deleteCredentialsByProjectId(projectId);
        projectRepo.deleteById(projectId);
    }

    private Project findProjectOrThrow(UUID projectId) {
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }
}