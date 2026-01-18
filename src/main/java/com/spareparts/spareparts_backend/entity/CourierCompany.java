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
@Table(name = "courier_companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer courierId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String contactNumber;

    private String serviceArea; // All Island, Western Province, etc.

    @Column(nullable = false)
    private boolean isActive = true;

    // Linking to User account for login capabilities
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
