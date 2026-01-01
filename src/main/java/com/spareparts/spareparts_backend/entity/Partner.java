package com.spareparts.spareparts_backend.entity;

import com.spareparts.spareparts_backend.enums.PartnerAgreementStatus;
import com.spareparts.spareparts_backend.enums.PartnerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer partnerId;

    // 🔗 Linked User account
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ================= Personal Info =================
    private String fullName;
    private String phone;
    private String nicNumber;

    // ================= Shop Info =================
    private String shopName;
    private String shopAddress;
    private String branchName;

    // ================= Profile Images =================
    private String nicFrontImage;
    private String nicBackImage;
    private String profileImage;

    // ================= Partner Status =================
    @Enumerated(EnumType.STRING)
    private PartnerStatus status; // PENDING, APPROVED, DELETED

    // Admin who approved this Partner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_admin_id")
    private User approvedByAdmin;

    // ================= Agreement Management =================

    // Latest agreement that partner has agreed to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreed_agreement_id")
    private PartnerAgreement agreedAgreement;

    @Enumerated(EnumType.STRING)
    private PartnerAgreementStatus agreementStatus;
    // REQUIRED, SUBMITTED, APPROVED, REJECTED, EXPIRED

    private LocalDateTime agreementSignedAt;

    // ================= Pending Update Fields =================
    // Used when Partner updates profile → Admin approval required

    private String pendingFullName;
    private String pendingPhone;
    private String pendingNicNumber;

    private String pendingShopName;
    private String pendingShopAddress;

    private String pendingNicFrontImage;
    private String pendingNicBackImage;
    private String pendingProfileImage;

    // ================= Spare Items =================
    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpareItem> spareItems = new ArrayList<>();

    // ================= Audit =================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
