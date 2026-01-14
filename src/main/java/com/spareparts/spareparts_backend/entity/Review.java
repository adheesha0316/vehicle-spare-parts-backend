package com.spareparts.spareparts_backend.entity;

import com.spareparts.spareparts_backend.enums.ReviewTargetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"customer_id", "spare_item_id", "partner_id", "delivery_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    // ================= CUSTOMER =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // ================= TARGET ENTITIES =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_item_id")
    private SpareItem spareItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // Platform review (no entity)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewTargetType targetType;

    // ================= REVIEW DATA =================
    @Column(nullable = false)
    private int rating; // 1–5

    @Column(length = 500)
    private String comment;

    // ================= APPROVAL =================
    private boolean approved = false;           // Admin/Manager approval
    private boolean approvalRequired = true;    // Default true
    @Column(length = 500)
    private String rejectionReason;

    // ================= AUDIT =================
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime reviewedAt;          // When admin/manager approved/rejected
}
