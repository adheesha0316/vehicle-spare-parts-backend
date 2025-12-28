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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
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



    // ============================
    // DELETE MANAGER PROFILE (ADMIN)
    // ============================
    @DeleteMapping("/delete/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteManager(@PathVariable Integer managerId) {
        try {
            managerService.softDeleteManager(managerId);
            return ResponseEntity.ok("Manager deleted successfully");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    // ============================
    // RESTORE MANAGER PROFILE (ADMIN)
    // ============================
    @PatchMapping("/restore/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> restoreManager(@PathVariable Integer managerId) {
        try {
            managerService.restoreManagerProfile(managerId);
            return ResponseEntity.ok("Manager restored successfully");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    // ============================
    // GET MANAGER BY ID (ADMIN)
    // ============================
    @GetMapping("/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManagerDto> getManagerById(@PathVariable Integer managerId) {
        try {
            ManagerDto manager = managerService.getManagerById(managerId);
            return ResponseEntity.ok(manager);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    // ============================
    // GET ALL MANAGERS (ADMIN)
    // ============================
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ManagerDto>> getAllManagers() {
        List<ManagerDto> managers = managerService.getAllManagers();
        return ResponseEntity.ok(managers);
    }

    // ============================
    // APPROVE MANAGER PROFILE (ADMIN)
    // ============================
    @PutMapping("/approve/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManagerDto> approveManager(@PathVariable Integer managerId) {
        try {
            ManagerDto approved = managerService.approveManagerProfile(managerId);
            return ResponseEntity.ok(approved);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

    // ============================
    // DOWNLOAD NIC IMAGES (ADMIN)
    // ============================
    @GetMapping("/downloadNIC/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadNICImages(@PathVariable Integer managerId) {
        try {
            Resource resource = managerService.downloadNICImagesAsZip(managerId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=NIC_Images.zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }
}
