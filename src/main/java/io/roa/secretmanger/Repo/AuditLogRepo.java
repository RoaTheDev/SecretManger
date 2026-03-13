package io.roa.secretmanger.Repo;

import io.roa.secretmanger.DTO.projection.AuditLogProjection;
import io.roa.secretmanger.Model.Entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepo extends JpaRepository<AuditLog, UUID> {

    @Query("""
            SELECT a.id           AS id,
                   a.action       AS action,
                   a.targetType   AS targetType,
                   a.targetId     AS targetId,
                   a.performedAt  AS performedAt,
                   a.actor        AS actor
            FROM AuditLog a
            WHERE (:actorId IS NULL OR a.actor.id = :actorId)
            AND (:action IS NULL OR a.action = :action)
            AND (:targetType IS NULL OR a.targetType = :targetType)
            ORDER BY a.performedAt DESC
            """)
    Page<AuditLogProjection> findFiltered(@Param("actorId") UUID actorId,
                                          @Param("action") String action,
                                          @Param("targetType") String targetType,
                                          Pageable pageable);
}