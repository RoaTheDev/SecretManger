package io.roa.secretmanger.Repo;

import io.roa.secretmanger.DTO.projection.ApprovalRequestSummaryProjection;
import io.roa.secretmanger.Model.Entity.ApprovalRequest;
import io.roa.secretmanger.Model.Value.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalRequestRepo extends JpaRepository<ApprovalRequest, UUID> {

    @Query("""
            SELECT ar.id                        AS id,
                   ar.accessTier               AS accessTier,
                   ar.status                   AS status,
                   ar.quorumRequired           AS quorumRequired,
                   ar.quorumReached            AS quorumReached,
                   ar.createdAt                AS createdAt,
                   ar.resolvedAt               AS resolvedAt,
                   ar.credential               AS credential,
                   ar.requestedBy              AS requestedBy
            FROM ApprovalRequest ar
            WHERE ar.status = 'PENDING'
            AND ar.requestedBy.id != :userId
            AND NOT EXISTS (
                SELECT v FROM ApprovalVote v
                WHERE v.request.id = ar.id
                AND v.voter.id = :userId
            )
            ORDER BY ar.createdAt ASC
            """)
    Page<ApprovalRequestSummaryProjection> findPendingForVoter(@Param("userId") UUID userId,
                                                               Pageable pageable);

    @Query("""
            SELECT ar.id                        AS id,
                   ar.accessTier               AS accessTier,
                   ar.status                   AS status,
                   ar.quorumRequired           AS quorumRequired,
                   ar.quorumReached            AS quorumReached,
                   ar.createdAt                AS createdAt,
                   ar.resolvedAt               AS resolvedAt,
                   ar.credential               AS credential,
                   ar.requestedBy              AS requestedBy
            FROM ApprovalRequest ar
            WHERE ar.credential.id = :credentialId
            AND ar.requestedBy.id = :requestedBy
            ORDER BY ar.createdAt DESC
            """)
    Page<ApprovalRequestSummaryProjection> findByCredentialAndRequester(
            @Param("credentialId") UUID credentialId,
            @Param("requestedBy") UUID requestedBy,
            Pageable pageable);

    Optional<ApprovalRequest> findByCredentialIdAndRequestedByIdAndStatus(
            UUID credentialId,
            UUID requestedById,
            ApprovalStatus status);

//    Optional<ApprovalRequest> findById(UUID id);
}