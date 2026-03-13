package io.roa.secretmanger.DTO.projection;


import java.time.LocalDateTime;
import java.util.UUID;


public interface AuditLogProjection {
    UUID getId();

    String getAction();

    String getTargetType();

    UUID getTargetId();

    LocalDateTime getPerformedAt();

    ActorProjection getActor();

    interface ActorProjection {
        UUID getId();

        String getName();

        String getEmail();
    }
}