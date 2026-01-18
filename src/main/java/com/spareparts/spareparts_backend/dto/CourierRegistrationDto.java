package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.VehicleType;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CourierRegistrationDto {
    private String companyName;
    private String businessRegistrationNumber;
    private String email;
    private String password;
    private String contactNumber;
    private String address;
    private String serviceArea;
    private Set<VehicleType> vehicleTypes;
}
