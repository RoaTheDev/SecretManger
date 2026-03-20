package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Project.DeletionVoteStatus;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface AdminService {
    PageResponse<UserSummaryProjection> getAllUsers(Pageable pageable);

    void deactivateUser(UUID userId, Set<UUID> requestingAdminIds);

    void activateUser(UUID userId);

    DeletionVoteStatus getVoteStatus(UUID projectId);

    DeletionVoteStatus voteDeletion(UUID projectId, String rawPassword);
}
