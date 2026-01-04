package com.spareparts.spareparts_backend.entity;

import com.spareparts.spareparts_backend.enums.PartnerAgreementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partner_agreements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer agreementId;

    @Column(nullable = false)
    private String filePath;      // Uploaded PDF path

    @Column(nullable = false)
    private String version;       // e.g., v1.0, v2.0

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String description;   // Optional notes about this version

    @Column(columnDefinition = "TEXT", nullable = false)
    private String conditions;    // Agreement conditions text

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerAgreementStatus status; // REQUIRED, SUBMITTED, APPROVED, etc.

    @Column(nullable = false)
    private Boolean isLatest;     // true if this is the latest version

    // Partners who agreed to this version (for audit)
    @OneToMany(mappedBy = "agreedAgreement")
    private List<Partner> partners = new ArrayList<>();
}
