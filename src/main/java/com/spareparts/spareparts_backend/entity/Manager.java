package com.spareparts.spareparts_backend.entity;

import com.spareparts.spareparts_backend.enums.ManagerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "managers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer managerId;

    // 🔗 Linked User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String fullName;
    private String phone;
    private String address;
    private String nicNumber;

    // Image paths
    private String nicFrontImage;
    private String nicBackImage;
    private String profileImage;

    @Enumerated(EnumType.STRING)
    private ManagerStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
