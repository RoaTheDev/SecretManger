package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.DTO.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {
    PageResponse<UserSummaryProjection> getAllUsers(Pageable pageable);
    void deactivateUser(UUID userId);
    void activateUser(UUID userId);
}
