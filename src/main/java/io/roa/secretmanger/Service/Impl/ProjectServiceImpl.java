package io.roa.secretmanger.Service.Impl;

import io.roa.secretmanger.Config.CacheConfig;
import io.roa.secretmanger.DTO.request.ProjectDto;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.Exception.DuplicateResourceException;
import io.roa.secretmanger.Exception.ResourceNotFoundException;
import io.roa.secretmanger.Mapper.ProjectMapper;
import io.roa.secretmanger.Model.Entity.Project;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Repo.ProjectRepo;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.ProjectService;
import io.roa.secretmanger.Util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepo projectRepository;
    private final UserRepo userRepository;
    private final ProjectMapper projectMapper;
    private final SecurityContextUtil securityContext;


    @Transactional
    public ProjectDto.ProjectCreatedResponse create(ProjectDto.CreateProjectRequest request) {
        User currentUser = securityContext.getCurrentUser();

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setCreatedBy(currentUser);
        project.setMembers(List.of(currentUser));

        return new ProjectDto.ProjectCreatedResponse(projectRepository.save(project).getId());
    }

    @Transactional
    @CacheEvict(value = CacheConfig.MEMBERSHIP, key = "#projectId + ':' + #request.userId()")
    public void addMember(UUID projectId, ProjectDto.AddMemberRequest request) {
        Project project = findProjectOrThrow(projectId);

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (project.getMembers().stream().anyMatch(m -> m.getId().equals(request.userId()))) {
            throw new DuplicateResourceException("User is already a member");
        }

        project.getMembers().add(user);
        projectRepository.save(project);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.MEMBERSHIP, key = "#projectId + ':' + #userId")
    public void removeMember(UUID projectId, UUID userId) {
        Project project = findProjectOrThrow(projectId);
        project.getMembers().removeIf(m -> m.getId().equals(userId));
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectDto.ProjectSummary> getMyProjects(Pageable pageable) {
        UUID currentUserId = securityContext.getCurrentUserId();
        return PageResponse.of(
                projectRepository.findSummariesForUser(currentUserId, pageable)
                        .map(projectMapper::toSummary)
        );
    }

    @Transactional(readOnly = true)
    public ProjectDto.ProjectDetail getDetail(UUID projectId) {
        Project project = findProjectOrThrow(projectId);

        List<ProjectDto.MemberSummary> members = projectRepository
                .findMembersByProjectId(projectId)
                .stream()
                .map(projectMapper::toMemberSummary)
                .toList();

        return new ProjectDto.ProjectDetail(
                project.getId(),
                project.getName(),
                project.getDescription(),
                members,
                project.getCreatedAt()
        );
    }
    @Cacheable(value = CacheConfig.MEMBERSHIP, key = "#projectId + ':' + #userId")
    public boolean isMember(UUID projectId, UUID userId) {
        return projectRepository.isMember(projectId, userId);
    }

    private Project findProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }
}