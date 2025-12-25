package com.spareparts.spareparts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagerRequestDto {
    private Integer userId; // linked User
    private String fullName;
    private String phone;
    private String address;
    private String nicNumber;
    private String nicFrontImage; // path or base64
    private String nicBackImage;  // path or base64
    private String profileImage;  // path or base64
}
