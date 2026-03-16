package io.roa.secretmanger.Service.Impl;

import io.roa.secretmanger.Config.CacheConfig;
import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.Exception.ResourceNotFoundException;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepo userRepo;
    private final CacheManager cacheManager;

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryProjection> getAllUsers(Pageable pageable) {
        return PageResponse.of(userRepo.findAllProjectedBy(pageable));
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        userRepo.save(user);

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
}
