package io.roa.secretmanger.DTO.request.Project;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalPolicy;
import io.roa.secretmanger.Model.Value.CredentialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCredentialRequest(
        @NotBlank String name,
        @NotNull CredentialType type,
        @NotBlank String value,
        @NotNull AccessTier accessTier,
        @NotNull ApprovalPolicy approvalPolicy
) {}
