package io.roa.secretmanger.Model.Entity;

import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalPolicy;
import io.roa.secretmanger.Model.Value.CredentialType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Entity
@Table(name = "credentials", indexes = {
        @Index(name = "idx_credentials_project", columnList = "project_id"),
        @Index(name = "idx_credentials_tier", columnList = "access_tier")
})
@Getter
@Setter
@FieldNameConstants
public class Credential extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private CredentialType type;

    @Column(name = "encrypted_value", nullable = false, columnDefinition = "TEXT")
    private String encryptedValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_tier", nullable = false, length = 20)
    private AccessTier accessTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_policy", nullable = false, length = 20)
    private ApprovalPolicy approvalPolicy = ApprovalPolicy.STANDARD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "credential", fetch = FetchType.LAZY)
    private List<ApprovalRequest> approvalRequests;
}