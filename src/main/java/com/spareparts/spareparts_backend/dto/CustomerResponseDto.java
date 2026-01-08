package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDto {
    private Integer customerId;

    private String fullName;
    private String nicNumber;
    private String phone;
    private String address;

    private String profileImage;

    private CustomerStatus status;

    private LocalDateTime createdAt;
}
