package com.spareparts.spareparts_backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelOrderRequestDto {
    @Size(max = 255, message = "Cancel reason must not exceed 255 characters")
    private String reason;
}
