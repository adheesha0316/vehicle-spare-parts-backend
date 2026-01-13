package com.spareparts.spareparts_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"order_id", "spare_item_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ================= ORDER =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // ================= SPARE ITEM =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_item_id", nullable = false)
    private SpareItem spareItem;

    // ================= SNAPSHOT DATA =================
    @Column(nullable = false)
    private String spareItemName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
