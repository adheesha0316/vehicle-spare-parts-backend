package com.spareparts.spareparts_backend.entity;

import com.spareparts.spareparts_backend.enums.LoyaltyTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_id", nullable = false)
    private LoyaltyPoints loyaltyPoints;

    @Column(nullable = false)
    private Integer points;   // + earn, - redeem

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoyaltyTransactionType type;

    @Column(nullable = false)
    private Integer pointsAfter;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
