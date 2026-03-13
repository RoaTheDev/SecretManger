package io.roa.secretmanger.DTO.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProjectDto {


    public record CreateProjectRequest(
            @NotBlank @Size(max = 255)
            String name,

            String description
    ) {
    }

    public record AddMemberRequest(
            UUID userId
    ) {
    }

    public record ProjectSummary(
            UUID id,
            String name,
            String description,
            int memberCount,
            LocalDateTime createdAt
    ) {
    }

    public record ProjectDetail(
            UUID id,
            String name,
            String description,
            List<MemberSummary> members,
            LocalDateTime createdAt
    ) {
    }

    public record MemberSummary(
            UUID id,
            String name,
            String email,
            String role
    ) {
    }

    public record ProjectCreatedResponse(UUID id) {
    }
}