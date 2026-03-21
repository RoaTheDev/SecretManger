package io.roa.secretmanger.Service.Impl;

import io.roa.secretmanger.Annotation.Audited;
import io.roa.secretmanger.Config.CacheConfig;
import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Project.DeletionVoteStatus;
import io.roa.secretmanger.DTO.response.Project.ProjectDeletionVoteSummary;
import io.roa.secretmanger.Exception.ResourceNotFoundException;
import io.roa.secretmanger.Exception.UnauthorizedException;
import io.roa.secretmanger.Exception.ValidationException;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Model.Value.ApprovalType;
import io.roa.secretmanger.Model.Value.UserRole;
import io.roa.secretmanger.Repo.ProjectRepo;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.AdminService;
import io.roa.secretmanger.Service.ApprovalService;
import io.roa.secretmanger.Service.ProjectService;
import io.roa.secretmanger.Util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepo userRepo;
    private final CacheManager cacheManager;
    private final SecurityContextUtil securityContext;
    private final ProjectService projectService;
    private final AuthenticationManager authManager;

    private final ApprovalService approvalService;
    private final ProjectRepo projectRepo;
    @Transactional(readOnly = true)
    public PageResponse<UserSummaryProjection> getAllUsers(Pageable pageable) {
        return PageResponse.of(userRepo.findAllProjectedBy(pageable));
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new ValidationException("User is already deactivated");
        }

        if (user.getRole() == UserRole.DEVELOPER) {
            user.setActive(false);
            userRepo.save(user);
            evictMembershipCache(userId);
            return;
        }

        approvalService.requestUserAction(userId, ApprovalType.USER_DEACTIVATION);
    }
    @Transactional(readOnly = true)
    public List<ProjectDeletionVoteSummary> getOngoingDeletionVotes() {
        UUID currentUserId = securityContext.getCurrentUserId();
        Cache cache = cacheManager.getCache(CacheConfig.DELETION_VOTES);
        List<User> activeAdmins = userRepo.findAllByRoleAndActiveTrue(UserRole.ADMIN);

        return projectRepo.findAll().stream()
                .map(project -> {
                    long votedCount = activeAdmins.stream()
                            .filter(a -> (cache != null
                                    ? cache.get(project.getId() + ":" + a.getId())
                                    : null) != null)
                            .count();

                    if (votedCount == 0) return null;

                    boolean hasVoted = cache.get(project.getId() + ":" + currentUserId) != null;

                    return new ProjectDeletionVoteSummary(
                            project.getId(),
                            project.getName(),
                            votedCount,
                            activeAdmins.size(),
                            hasVoted
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }
    @Transactional
    public void activateUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isActive()) {
            throw new ValidationException("User is already active");
        }

        if (user.getRole() == UserRole.DEVELOPER) {
            user.setActive(true);
            userRepo.save(user);
            return;
        }

        approvalService.requestUserAction(userId, ApprovalType.USER_ACTIVATION);
    }

    private void evictMembershipCache(UUID userId) {
        Cache cache = cacheManager.getCache(CacheConfig.MEMBERSHIP);
        if (cache != null) {
            cache.evictIfPresent(userId.toString());
        }
    }

    @Audited(action = "PROJECT_DELETION_VOTED", targetType = "PROJECT")
    @Transactional
    public DeletionVoteStatus voteDeletion(UUID projectId, String rawPassword) {
        User currentUser = securityContext.getCurrentUser();

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(currentUser.getEmail(), rawPassword)
            );
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid password");
        }

        Cache cache = cacheManager.getCache(CacheConfig.DELETION_VOTES);
        if (cache != null) {
            cache.put(projectId + ":" + currentUser.getId(), true);
        }

        List<User> activeAdmins = userRepo.findAllByRoleAndActiveTrue(UserRole.ADMIN);

        Set<UUID> votedAdminIds = activeAdmins.stream()
                .map(User::getId)
                .filter(id -> (cache != null ? cache.get(projectId + ":" + id) : null) != null)
                .collect(java.util.stream.Collectors.toSet());

        long votedCount = votedAdminIds.size();

        if (votedCount >= activeAdmins.size()) {
            activeAdmins.forEach(a -> {
                if (cache != null) cache.evict(projectId + ":" + a.getId());
            });
            projectService.executeProjectDeletion(projectId, votedAdminIds);
        }

        return new DeletionVoteStatus(votedCount, activeAdmins.size());
    }

    public DeletionVoteStatus getVoteStatus(UUID projectId) {
        Cache cache = cacheManager.getCache(CacheConfig.DELETION_VOTES);
        List<User> activeAdmins = userRepo.findAllByRoleAndActiveTrue(UserRole.ADMIN);

        long votedCount = activeAdmins.stream()
                .filter(a -> (cache != null ? cache.get(projectId + ":" + a.getId()) : null) != null)
                .count();
        return new DeletionVoteStatus(votedCount, activeAdmins.size());
    }
}
