package com.spareparts.spareparts_backend.entity;

import com.spareparts.spareparts_backend.enums.*;
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

    @Enumerated(EnumType.STRING)
    private ItemSize itemSize;

    @Column(nullable = false)
    private boolean deleted = false;

    // ================= IMAGES =================
    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "spare_item_images",
            joinColumns = @JoinColumn(name = "spare_item_id")
    )
    @Column(name = "image_path")
    private List<String> images = new ArrayList<>();

    // ================= STATUS =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpareItemStatus status;

    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;

    // ================= OWNERSHIP =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnershipStatus ownership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partner partner; // only if ownership == PARTNER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @Column(length = 500)
    private String rejectionReason;

    private LocalDateTime approvedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ================= PENDING UPDATE =================
    private String pendingName;
    private String pendingBrand;
    private String pendingDescription;
    private Double pendingPrice;
    private Integer pendingQuantity;

    @Enumerated(EnumType.STRING)
    private SpareItemCategory pendingCategory;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "spare_item_pending_images",
            joinColumns = @JoinColumn(name = "spare_item_id")
    )
    @Column(name = "pending_image_path")
    private List<String> pendingImages = new ArrayList<>();
}
