package io.roa.secretmanger.Model.Entity;


import io.roa.secretmanger.Model.Value.VoteChoice;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;



@Entity
@Table(name = "approval_votes", indexes = {
        @Index(name = "idx_approval_votes_request", columnList = "request_id")
},
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_vote_per_request_voter",
                        columnNames = {"request_id", "voter_id"})
        })
@Getter
@Setter
@FieldNameConstants
public class ApprovalVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ApprovalRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VoteChoice vote;

    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt = LocalDateTime.now();
}
