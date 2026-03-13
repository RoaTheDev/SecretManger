package io.roa.secretmanger.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ProjectSummaryProjection {
    UUID getId();

    String getName();

    String getDescription();

    LocalDateTime getCreatedAt();
}

