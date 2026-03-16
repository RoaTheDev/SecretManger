package io.roa.secretmanger.DTO.response.Project;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectSummary(
        UUID id,
        String name,
        String description,
        int memberCount,
        LocalDateTime createdAt
) {
}
