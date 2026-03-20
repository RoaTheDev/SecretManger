package io.roa.secretmanger.DTO.request.Project;

import jakarta.validation.constraints.NotBlank;

public record VoteDeletionRequest(
        @NotBlank
        String password
) {
}
