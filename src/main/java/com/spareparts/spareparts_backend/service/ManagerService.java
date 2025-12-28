package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.ManagerDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ManagerService {
    // ---------------- CREATE ---------------- //
    ManagerDto createManager(Integer userId, ManagerDto managerDto,
                             MultipartFile nicFront, MultipartFile nicBack, MultipartFile profileImage);


    // ---------------- UPDATE ---------------- //
    ManagerDto updateManager(Integer managerId, ManagerDto managerDto,
                             MultipartFile nicFront, MultipartFile nicBack, MultipartFile profileImage);


    // ---------------- SOFT DELETE ---------------- //
    void softDeleteManager(Integer managerId);


    // ---------------- RESTORE DELETED MANAGER ---------------- //
    ManagerDto restoreManagerProfile(Integer managerId);


    // ---------------- GET ---------------- //
    ManagerDto getManagerById(Integer managerId);


    // ---------------- GET ALL ---------------- //
    List<ManagerDto> getAllManagers();


    // ---------------- ADMIN APPROVE ---------------- //
    ManagerDto approveManagerProfile(Integer managerId);


    // ---------------- NIC ZIP DOWNLOAD ---------------- //
    Resource downloadNICImagesAsZip(Integer managerId);
}
