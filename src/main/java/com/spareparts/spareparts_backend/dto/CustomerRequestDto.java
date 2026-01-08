package com.spareparts.spareparts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDto {
    private String fullName;
    private String nicNumber;
    private String phone;
    private String address;
}
