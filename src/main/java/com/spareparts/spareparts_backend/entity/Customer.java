package com.spareparts.spareparts_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    // ================= USER =================
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ================= PERSONAL INFO =================
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, length = 12, unique = true)
    private String nicNumber;

    @Column(nullable = false)
    private String phone;

    private String address;

    // ================= PROFILE IMAGE =================
    private String profileImagePath;   // uploads/customer/profile/xxx.jpg

    // ================= AUDIT =================
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
