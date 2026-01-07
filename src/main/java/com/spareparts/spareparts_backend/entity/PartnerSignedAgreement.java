package com.spareparts.spareparts_backend.entity;


import com.spareparts.spareparts_backend.enums.AgreementApprovalStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "partner_signed_agreements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerSignedAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ================= Partner =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    // ================= Agreement Template =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_id", nullable = false)
    private PartnerAgreement agreement;

    // ================= Signed Info =================
    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "version", nullable = false)
    private String version;

    // ================= Approval Flow =================

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private AgreementApprovalStatus approvalStatus;
    // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy; // ADMIN or MANAGER

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}
