package io.roa.secretmanger.Model.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "shamir_shares")
@Getter
@Setter
@FieldNameConstants
public class ShamirShare extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false, unique = true)
    private User admin;

    @Column(name = "share_index", nullable = false)
    private int shareIndex;

    @Column(name = "encrypted_share", nullable = false, columnDefinition = "TEXT")
    private String encryptedShare;
}
