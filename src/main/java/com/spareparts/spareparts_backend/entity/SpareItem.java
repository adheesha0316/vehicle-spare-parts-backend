package com.spareparts.spareparts_backend.entity;

import com.spareparts.spareparts_backend.enums.SpareItemCategory;
import com.spareparts.spareparts_backend.enums.SpareItemStatus;
import com.spareparts.spareparts_backend.enums.StockStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "spare_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpareItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer spareItemId;

    // ================= BASIC INFO =================
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpareItemCategory category;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer quantity;

    // ================= IMAGES =================
    @ElementCollection
    @CollectionTable(
            name = "spare_item_images",
            joinColumns = @JoinColumn(name = "spare_item_id")
    )
    @Column(name = "image_path")
    private List<String> images = new ArrayList<>();

    // ================= STATUS =================
    @Enumerated(EnumType.STRING)
    private SpareItemStatus status;
    // PENDING, APPROVED, REJECTED, UPDATE_PENDING, DELETED

    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;
    // IN_STOCK, OUT_OF_STOCK, LOW_STOCK

    // ================= OWNERSHIP =================

    // Created by MANAGER (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Manager manager;

    // Created by PARTNER (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    // Approved / Rejected by ADMIN or MANAGER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    // Reason if rejected
    @Column(length = 500)
    private String rejectionReason;

    // Approval / rejection time
    private LocalDateTime approvedAt;

    // ================= TIMESTAMPS =================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ================= PENDING UPDATE FIELDS =================
    // Used when update requires approval

    private String pendingName;
    private String pendingBrand;
    private String pendingDescription;
    private Double pendingPrice;
    private Integer pendingQuantity;

    @Enumerated(EnumType.STRING)
    private SpareItemCategory pendingCategory;

    @ElementCollection
    @CollectionTable(
            name = "spare_item_pending_images",
            joinColumns = @JoinColumn(name = "spare_item_id")
    )
    @Column(name = "pending_image_path")
    private List<String> pendingImages = new ArrayList<>();
}
