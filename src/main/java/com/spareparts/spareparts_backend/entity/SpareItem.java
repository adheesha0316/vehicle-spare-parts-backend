package com.spareparts.spareparts_backend.entity;

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

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String category;

    // Customers care about brand
    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer quantity;

    // 1–5 image file paths
    @ElementCollection
    @CollectionTable(
            name = "spare_item_images",
            joinColumns = @JoinColumn(name = "spare_item_id")
    )
    @Column(name = "image_path")
    private List<String> images = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private SpareItemStatus status;
    // PENDING, APPROVED, DELETED

    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;
    // IN_STOCK, OUT_OF_STOCK, LOW_STOCK

    // Who added the item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Manager manager;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ================= PENDING UPDATE FIELDS =================
    // Used when MANAGER updates → ADMIN approval required

    private String pendingName;
    private String pendingDescription;
    private Double pendingPrice;
    private Integer pendingQuantity;

    @ElementCollection
    @CollectionTable(
            name = "spare_item_pending_images",
            joinColumns = @JoinColumn(name = "spare_item_id")
    )
    @Column(name = "pending_image_path")
    private List<String> pendingImages = new ArrayList<>();
}
