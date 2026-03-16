package io.roa.secretmanger.DTO.response.Project;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectDetail(
        UUID id,
        String name,
        String description,
        List<MemberSummary> members,
        LocalDateTime createdAt
) {
}
