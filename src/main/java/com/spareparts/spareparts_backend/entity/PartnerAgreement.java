package com.spareparts.spareparts_backend.entity;

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

    private String filePath;      // Uploaded PDF path
    private String version;       // e.g., v1.0, v2.0
    private LocalDateTime createdAt;
    private String description;   // Optional notes about this version

    // Partners who agreed to this version (optional for audit)
    @OneToMany(mappedBy = "agreedAgreement")
    private List<Partner> partners = new ArrayList<>();
}
