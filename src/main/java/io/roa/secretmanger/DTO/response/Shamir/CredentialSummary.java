package io.roa.secretmanger.DTO.response.Shamir;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.CredentialType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CredentialSummary(
        UUID id,
        String name,
        CredentialType type,
        AccessTier accessTier,
        LocalDateTime createdAt
) {
}
