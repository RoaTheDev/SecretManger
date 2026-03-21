package io.roa.secretmanger.DTO.request.ApprovalRequest;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalPolicy;
import io.roa.secretmanger.Model.Value.CredentialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

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
        AccessTier accessTier,

        @NotNull
        ApprovalPolicy approvalPolicy
) {
}
