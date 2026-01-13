package com.spareparts.spareparts_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_updates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private String status; // e.g., "SHIPPED", "DELIVERED"

    private String reason;
    private LocalDateTime timestamp;
}
