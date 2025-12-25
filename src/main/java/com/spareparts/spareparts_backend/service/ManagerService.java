package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.dto.ManagerDto;
import org.springframework.core.io.Resource;

import java.util.List;

public interface ManagerService {
    // ---------------- CREATE ---------------- //
    ManagerDto createManagerProfile(ManagerDto managerDto);

    // ---------------- UPDATE ---------------- //
    ManagerDto updateManagerProfile(Integer managerId, ManagerDto managerDto);

    // ---------------- DELETE ---------------- //
    void deleteManagerProfile(Integer managerId);

    // ---------------- GET ---------------- //
    ManagerDto getManagerById(Integer managerId);

    List<ManagerDto> getAllManagers();

    // ---------------- ADMIN APPROVE ---------------- //
    ManagerDto approveManagerProfile(Integer managerId);

    // ---------------- NIC ZIP DOWNLOAD ---------------- //
    Resource downloadNICImagesAsZip(Integer managerId);
}
