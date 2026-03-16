package io.roa.secretmanger.DTO.request.Project;

import java.util.UUID;

public record AddMemberRequest(
        UUID userId
) {
}
