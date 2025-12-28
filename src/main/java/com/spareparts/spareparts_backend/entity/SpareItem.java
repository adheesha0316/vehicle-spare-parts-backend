package com.spareparts.spareparts_backend.entity;

import com.spareparts.spareparts_backend.enums.SpareItemStatus;
import com.spareparts.spareparts_backend.enums.StockStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    private String name;
    private String description;
    private Double price;
    private Integer quantity;

    // Stores file paths of 1-5 images
    @ElementCollection
    private List<String> images;

    @Enumerated(EnumType.STRING)
    private SpareItemStatus status;

    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional: for pending updates before admin approval
    private String pendingName;
    private String pendingDescription;
    private Double pendingPrice;
    private Integer pendingQuantity;
    @ElementCollection
    private List<String> pendingImages;
}
