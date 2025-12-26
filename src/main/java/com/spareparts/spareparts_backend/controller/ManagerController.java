package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.dto.ManagerDto;
import com.spareparts.spareparts_backend.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
@CrossOrigin
public class ManagerController {

    private final ManagerService managerService;

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
            @RequestPart("manager") ManagerDto managerDto,
            @RequestPart(value = "nicFront", required = false) MultipartFile nicFront,
            @RequestPart(value = "nicBack", required = false) MultipartFile nicBack,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        ManagerDto created = managerService.createManager(
                userId,
                managerDto,
                nicFront,
                nicBack,
                profileImage
        );
        return ResponseEntity.ok(created);
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
            @RequestPart("manager") ManagerDto managerDto,
            @RequestPart(value = "nicFront", required = false) MultipartFile nicFront,
            @RequestPart(value = "nicBack", required = false) MultipartFile nicBack,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        ManagerDto updated = managerService.updateManager(
                managerId,
                managerDto,
                nicFront,
                nicBack,
                profileImage
        );
        return ResponseEntity.ok(updated);
    }

    // ============================
    // DELETE MANAGER PROFILE (ADMIN)
    // ============================
    @DeleteMapping("/delete/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteManager(@PathVariable Integer managerId) {
        managerService.deleteManagerProfile(managerId);
        return ResponseEntity.noContent().build();
    }

    // ============================
    // GET MANAGER BY ID (ADMIN)
    // ============================
    @GetMapping("/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManagerDto> getManagerById(
            @PathVariable Integer managerId
    ) {
        ManagerDto manager = managerService.getManagerById(managerId);
        return ResponseEntity.ok(manager);
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
    public ResponseEntity<ManagerDto> approveManager(
            @PathVariable Integer managerId
    ) {
        ManagerDto approved = managerService.approveManagerProfile(managerId);
        return ResponseEntity.ok(approved);
    }

    // ============================
    // DOWNLOAD NIC IMAGES (ADMIN)
    // ============================
    @GetMapping("/downloadNIC/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadNICImages(
            @PathVariable Integer managerId
    ) {
        Resource resource = managerService.downloadNICImagesAsZip(managerId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=NIC_Images.zip"
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
