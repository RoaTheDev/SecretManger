package io.roa.secretmanger.DTO.response.Shamir;

import io.roa.secretmanger.Model.Value.CredentialType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CredentialRevealResponse(
        UUID id,
        String name,
        CredentialType type,
        String value,
        LocalDateTime expiresAt
) {}