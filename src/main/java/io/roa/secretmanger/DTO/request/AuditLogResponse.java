package io.roa.secretmanger.DTO.request;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


public record AuditLogResponse(
        UUID id,
        String actorName,
        String actorEmail,
        String action,
        String targetType,
        UUID targetId,
        Map<String, Object> metadata,
        LocalDateTime performedAt
) {
}
