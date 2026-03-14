package io.roa.secretmanger.Util;

import io.roa.secretmanger.Annotation.Audited;
import io.roa.secretmanger.Service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final SecurityContextUtil securityContext;

    @AfterReturning(pointcut = "@annotation(audited)", argNames = "joinPoint,audited")
    public void logAction(JoinPoint joinPoint, Audited audited) {
        try {
            UUID actorId = securityContext.getCurrentUserId();

            UUID targetId = null;
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0 && args[0] instanceof UUID id) {
                targetId = id;
            }

            auditService.log(actorId, audited.action(), audited.targetType(), targetId, null);
        } catch (Exception e) {
            log.error("Audit logging failed for action {}: {}", audited.action(), e.getMessage());
        }
    }
}