package com.spareparts.spareparts_backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spareparts.spareparts_backend.dto.ManagerDto;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.ManagerStatus;
import com.spareparts.spareparts_backend.enums.UserStatus;
import com.spareparts.spareparts_backend.service.ManagerService;
import com.spareparts.spareparts_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
@CrossOrigin
public class ManagerController {

    private final ManagerService managerService;
    private final UserService userService;
    private final ObjectMapper mapper;

    // =========================================================
    // CREATE MANAGER PROFILE (ONLY MANAGER ROLE - APPROVED USER)
    // =========================================================
    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ManagerDto> createManager(
            @RequestParam("userId") Integer userId,
            @RequestPart("manager") String managerJson,
            @RequestPart(value = "nicFront", required = false) MultipartFile nicFront,
            @RequestPart(value = "nicBack", required = false) MultipartFile nicBack,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws Exception {

        ManagerDto managerDto = mapper.readValue(managerJson, ManagerDto.class);
        User user = userService.getUserEntityById(userId);

        if (user.getStatus() != UserStatus.APPROVED) {
            throw new AccessDeniedException("Manager account not approved by admin yet.");
        }

        return ResponseEntity.ok(
                managerService.createManager(userId, managerDto, nicFront, nicBack, profileImage)
        );
    }


    // ======================================
    // UPDATE MANAGER PROFILE (MANAGER / ADMIN)
    // ======================================
    @PutMapping(
            value = "/update/{managerId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('MANAGER') and @managerSecurity.isOwner(#managerId)")
    public ResponseEntity<ManagerDto> updateManager(
            @PathVariable Integer managerId,
            @RequestPart("manager") String managerJson,
            @RequestPart(value = "nicFront", required = false) MultipartFile nicFront,
            @RequestPart(value = "nicBack", required = false) MultipartFile nicBack,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws JsonProcessingException {

        ManagerDto managerDto = mapper.readValue(managerJson, ManagerDto.class);

        ManagerDto existingManager = managerService.getManagerById(managerId);
        if (existingManager.getStatus() == ManagerStatus.DELETED) {
            throw new ResponseStatusException(NOT_FOUND, "Cannot update a deleted manager");
        }

        ManagerDto updatedManager = managerService.updateManager(managerId, managerDto, nicFront, nicBack, profileImage);
        return ResponseEntity.ok(updatedManager);
    }

}
