package com.spareparts.spareparts_backend.dto;

import com.spareparts.spareparts_backend.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDtoReturn {
    private String email;
    private UserStatus status;}
