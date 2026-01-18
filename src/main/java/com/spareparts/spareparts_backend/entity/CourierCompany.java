package com.spareparts.spareparts_backend.entity;

import com.spareparts.spareparts_backend.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

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

    @Column(unique = true)
    private String businessRegistrationNumber;

    @Column(nullable = false)
    private String contactNumber;

    private String address;
    private String serviceArea;

    @ElementCollection(targetClass = VehicleType.class)
    @CollectionTable(name = "courier_vehicles", joinColumns = @JoinColumn(name = "courier_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private Set<VehicleType> vehicleTypes; // වාහන වර්ග කිහිපයක් මෙහි ගබඩා වේ

    private Double rating = 0.0;
    private boolean isActive = true;
    private boolean isVerified = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
