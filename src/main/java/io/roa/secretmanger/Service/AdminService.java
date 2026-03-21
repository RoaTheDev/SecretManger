package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Project.DeletionVoteStatus;
import io.roa.secretmanger.DTO.response.Project.ProjectDeletionVoteSummary;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    PageResponse<UserSummaryProjection> getAllUsers(Pageable pageable);

    void deactivateUser(UUID userId);

    void activateUser(UUID userId);
    List<ProjectDeletionVoteSummary> getOngoingDeletionVotes();
    DeletionVoteStatus getVoteStatus(UUID projectId);

    DeletionVoteStatus voteDeletion(UUID projectId, String rawPassword);
}
