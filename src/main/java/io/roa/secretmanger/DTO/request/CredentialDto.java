package io.roa.secretmanger.DTO.request;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.CredentialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public class CredentialDto {


    public record CreateCredentialRequest(
            @NotNull
            UUID projectId,

            @NotBlank @Size(max = 255)
            String name,

            @NotNull
            CredentialType type,

            @NotBlank
            String value,

            @NotNull
            AccessTier accessTier
    ) {}

    public record CredentialSummary(
            UUID id,
            String name,
            CredentialType type,
            AccessTier accessTier,
            LocalDateTime createdAt
    ) {}

    public record CredentialDetail(
            UUID id,
            String name,
            CredentialType type,
            AccessTier accessTier,
            String createdBy,
            LocalDateTime createdAt
    ) {}

    public record CredentialRevealResponse(
            UUID id,
            String name,
            CredentialType type,
            String value
    ) {}

    public record CredentialCreatedResponse(UUID id) {}
}