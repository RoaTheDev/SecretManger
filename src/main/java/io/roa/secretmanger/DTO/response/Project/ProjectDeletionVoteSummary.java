package io.roa.secretmanger.DTO.response.Project;

import java.util.UUID;

public record ProjectDeletionVoteSummary(
        UUID projectId,
        String projectName,
        long votedCount,
        long totalAdmins,
        boolean hasVoted
) {}