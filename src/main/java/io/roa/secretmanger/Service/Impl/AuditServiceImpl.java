package io.roa.secretmanger.Service.Impl;


import io.roa.secretmanger.Model.Entity.AuditLog;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Repo.AuditLogRepo;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepo auditLogRepo;
    private final UserRepo userRepo;

    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UUID actorId, String action, String targetType,
                    UUID targetId, Map<String, Object> metadata) {
        try {
            User actor = userRepo.findById(actorId).orElse(null);

            AuditLog auditLog = new AuditLog();
            auditLog.setActor(actor);
            auditLog.setAction(action);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setMetadata(metadata);

            auditLogRepo.save(auditLog);
        } catch (Exception e) {
            log.error("Async audit logging failed [action={}, actor={}, target={}]: {}",
                    action, actorId, targetId, e.getMessage());
        }
    }
}
