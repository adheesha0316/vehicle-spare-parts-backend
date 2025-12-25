package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.ManagerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagerDto {
    private Integer managerId;
    private Integer userId;       // Linked user
    private String fullName;
    private String phone;
    private String address;
    private String nicNumber;

    // Image paths
    private String nicFrontImage;
    private String nicBackImage;
    private String profileImage;

    private ManagerStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional: For pending update logic
    private String pendingFullName;
    private String pendingPhone;
    private String pendingAddress;
    private String pendingNicNumber;
    private String pendingNicFrontImage;
    private String pendingNicBackImage;
    private String pendingProfileImage;
}
