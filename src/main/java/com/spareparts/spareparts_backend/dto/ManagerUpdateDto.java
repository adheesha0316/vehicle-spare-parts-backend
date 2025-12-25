package com.spareparts.spareparts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagerUpdateDto {
    private String fullName;
    private String phone;
    private String address;
    private String nicNumber;
    private String nicFrontImage; // optional, if updated
    private String nicBackImage;  // optional, if updated
    private String profileImage;  // optional, if updated
}
