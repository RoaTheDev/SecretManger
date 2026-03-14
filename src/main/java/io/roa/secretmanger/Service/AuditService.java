package io.roa.secretmanger.Service;

import java.util.Map;
import java.util.UUID;

public interface AuditService {
    void log(UUID actorId, String action, String targetType,
             UUID targetId, Map<String, Object> metadata);
}
