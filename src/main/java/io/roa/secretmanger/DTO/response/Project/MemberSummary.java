package io.roa.secretmanger.DTO.response.Project;

import java.util.UUID;

public record MemberSummary(
        UUID id,
        String name,
        String email,
        String role
) {
}
