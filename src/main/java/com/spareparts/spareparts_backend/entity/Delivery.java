package com.spareparts.spareparts_backend.entity;

import com.spareparts.spareparts_backend.enums.OrderStatus;
import com.spareparts.spareparts_backend.utill.TrackingNumberGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deliveryId;

    // ================= LINK TO ORDER =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // ================= DELIVERY STATUS =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // ================= TRACKING =================
    @Column(unique = true, updatable = false)
    private String trackingNumber; // TRK + 9 digits

    private String courierName;    // optional

    // ================= AUDIT =================
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
