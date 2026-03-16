package io.roa.secretmanger.DTO.response;

import io.roa.secretmanger.Model.Value.UserRole;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String name,
        String email,
        UserRole role
) {
}
