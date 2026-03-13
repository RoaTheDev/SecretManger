package io.roa.secretmanger.Model.Entity;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "approval_requests", indexes = {
        @Index(name = "idx_approval_requests_credential", columnList = "credential_id"),
        @Index(name = "idx_approval_requests_status",    columnList = "status"),
        @Index(name = "idx_approval_requests_requester", columnList = "requested_by")
})
@Getter
@Setter
@FieldNameConstants
public class ApprovalRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id", nullable = false)
    private Credential credential;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_tier", nullable = false, length = 20)
    private AccessTier accessTier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "quorum_required", nullable = false)
    private int quorumRequired;

    @Column(name = "quorum_reached", nullable = false)
    private boolean quorumReached = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY)
    private List<ApprovalVote> votes;
}
