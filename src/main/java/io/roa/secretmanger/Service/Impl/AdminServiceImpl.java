package io.roa.secretmanger.Service.Impl;

import io.roa.secretmanger.Annotation.Audited;
import io.roa.secretmanger.Config.CacheConfig;
import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Project.DeletionVoteStatus;
import io.roa.secretmanger.Exception.ResourceNotFoundException;
import io.roa.secretmanger.Exception.UnauthorizedException;
import io.roa.secretmanger.Exception.ValidationException;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Model.Value.UserRole;
import io.roa.secretmanger.Repo.ShamirShareRepo;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.AdminService;
import io.roa.secretmanger.Service.ProjectService;
import io.roa.secretmanger.Service.ShamirService;
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
    private final ShamirService shamirService;
    private final ShamirShareRepo shamirShareRepo;
    @Transactional(readOnly = true)
    public PageResponse<UserSummaryProjection> getAllUsers(Pageable pageable) {
        return PageResponse.of(userRepo.findAllProjectedBy(pageable));
    }

    @Transactional
    public void deactivateUser(UUID userId, Set<UUID> requestingAdminIds) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new ValidationException("User is already deactivated");
        }

        if (user.getRole() == UserRole.ADMIN) {
            long activeAdminCount = userRepo.countByRoleAndActiveTrue(UserRole.ADMIN);
            if (activeAdminCount <= 1) {
                throw new ValidationException("Cannot deactivate the last active admin");
            }

            if (requestingAdminIds == null || requestingAdminIds.isEmpty()) {
                throw new ValidationException("Admin deactivation requires quorum approval");
            }

            if (requestingAdminIds.contains(userId)) {
                throw new UnauthorizedException("The admin being deactivated cannot be part of the quorum");
            }

            shamirService.reconstructMasterKey(requestingAdminIds);
            shamirShareRepo.deleteByAdminId(userId);
        }

        user.setActive(false);
        userRepo.save(user);

        if (user.getRole() == UserRole.ADMIN) {
            shamirService.splitAndDistribute();
        }

        evictMembershipCache(userId);
    }

    @Transactional
    public void activateUser(UUID userId) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(true);
        userRepo.save(user);
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

        List<User> admins = userRepo.findAllByRole(UserRole.ADMIN);

        Set<UUID> votedAdminIds = admins.stream()
                .map(User::getId)
                .filter(id -> (cache != null ? cache.get(projectId + ":" + id) : null) != null)
                .collect(java.util.stream.Collectors.toSet());

        long votedCount = votedAdminIds.size();

        if (votedCount >= admins.size()) {
            admins.forEach(a -> {
                if (cache != null) cache.evict(projectId + ":" + a.getId());
            });

            projectService.executeProjectDeletion(projectId, votedAdminIds);
        }

        return new DeletionVoteStatus(votedCount, admins.size());
    }
    public DeletionVoteStatus getVoteStatus(UUID projectId) {
        Cache cache = cacheManager.getCache(CacheConfig.DELETION_VOTES);
        List<User> admins = userRepo.findAllByRole(UserRole.ADMIN);

        long votedCount = admins.stream()
                .filter(a -> (cache != null ? cache.get(projectId + ":" + a.getId()) : null) != null)
                .count();
        return new DeletionVoteStatus(votedCount, admins.size());
    }
}
