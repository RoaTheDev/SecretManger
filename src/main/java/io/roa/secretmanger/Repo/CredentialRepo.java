package io.roa.secretmanger.Repo;

import io.roa.secretmanger.DTO.projection.CredentialDetailProjection;
import io.roa.secretmanger.DTO.projection.CredentialSummaryProjection;
import io.roa.secretmanger.Model.Entity.Credential;
import io.roa.secretmanger.Model.Value.AccessTier;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialRepo extends JpaRepository<Credential, UUID> {

    @Query("""
            SELECT c.id          AS id,
                   c.name        AS name,
                   c.type        AS type,
                   c.accessTier  AS accessTier,
                   c.createdAt   AS createdAt
            FROM Credential c
            WHERE c.project.id = :projectId
            ORDER BY c.createdAt DESC
            """)
    Page<CredentialSummaryProjection> findSummariesByProject(@Param("projectId") UUID projectId,
                                                             Pageable pageable);

    @Query("""
            SELECT c.id          AS id,
                   c.name        AS name,
                   c.type        AS type,
                   c.accessTier  AS accessTier,
                   c.createdAt   AS createdAt
            FROM Credential c
            WHERE c.project.id = :projectId
            AND c.accessTier = :tier
            ORDER BY c.createdAt DESC
            """)
    Page<CredentialSummaryProjection> findSummariesByProjectAndTier(@Param("projectId") UUID projectId,
                                                                    @Param("tier") AccessTier tier,
                                                                    Pageable pageable);

    @Query("""
            SELECT c.id         AS id,
                   c.name       AS name,
                   c.type       AS type,
                   c.accessTier AS accessTier,
                   c.createdAt  AS createdAt,
                   c.createdBy  AS createdBy
            FROM Credential c
            WHERE c.id = :id
            """)
    Optional<CredentialDetailProjection> findDetailById(@Param("id") UUID id);

//    Optional<Credential> findById(@NonNull UUID id);
}