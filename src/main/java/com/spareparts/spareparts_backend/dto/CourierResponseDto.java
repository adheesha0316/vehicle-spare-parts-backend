package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierResponseDto {

    private Integer courierId;

    // Linked User ID (useful for profile management)
    private Integer userId;

    private String companyName;

    private String email;

    private String contactNumber;

    private String businessRegistrationNumber;

    private String address;

    private String serviceArea;

    // The collection of vehicle types they own
    private Set<VehicleType> vehicleTypes;

    private Double rating;

    // Reputation indicators
    private boolean isVerified;

    private boolean isActive;

    private LocalDateTime createdAt;
}
