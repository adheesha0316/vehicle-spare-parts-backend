package com.spareparts.spareparts_backend.service.Impl;

import com.spareparts.spareparts_backend.dto.ManagerDto;
import com.spareparts.spareparts_backend.entity.Manager;
import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.ManagerStatus;
import com.spareparts.spareparts_backend.enums.Role;
import com.spareparts.spareparts_backend.enums.UserStatus;
import com.spareparts.spareparts_backend.exception.ResourceNotFoundException;
import com.spareparts.spareparts_backend.repo.ManagerRepo;
import com.spareparts.spareparts_backend.repo.UserRepo;
import com.spareparts.spareparts_backend.service.ManagerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {

    private final ManagerRepo managerRepo;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;

    private final String UPLOAD_DIR = "uploads/manager/";


    @Override
    public ManagerDto createManager(Integer userId, ManagerDto managerDto, MultipartFile nicFront, MultipartFile nicBack, MultipartFile profileImage) {
        // Fetch user or throw if not found
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id " + userId
                ));

        // Check user role
        if (user.getRole() != Role.MANAGER) {
            throw new RuntimeException("User with id " + userId + " is not a MANAGER");
        }

        // Check user approval status
        if (user.getStatus() != UserStatus.APPROVED) {
            throw new RuntimeException("User with id " + userId + " is not approved by ADMIN");
        }

        // Map DTO to entity
        Manager manager = modelMapper.map(managerDto, Manager.class);
        manager.setUser(user);
        manager.setStatus(ManagerStatus.PENDING);
        manager.setCreatedAt(LocalDateTime.now());

        // Store files if present
        if (nicFront != null && !nicFront.isEmpty()) {
            manager.setNicFrontImage(storeFile(nicFront, "nicFront"));
        }
        if (nicBack != null && !nicBack.isEmpty()) {
            manager.setNicBackImage(storeFile(nicBack, "nicBack"));
        }
        if (profileImage != null && !profileImage.isEmpty()) {
            manager.setProfileImage(storeFile(profileImage, "profileImg"));
        }

        // Save and return DTO
        return modelMapper.map(managerRepo.save(manager), ManagerDto.class);
    }

    @Override
    public ManagerDto updateManager(Integer managerId, ManagerDto managerDto, MultipartFile nicFront, MultipartFile nicBack, MultipartFile profileImage) {
        // Fetch existing manager or throw exception
        Manager manager = managerRepo.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manager not found with id " + managerId
                ));

        // Update only safe fields manually (do not modify manager.user)
        manager.setFullName(managerDto.getFullName());
        manager.setPhone(managerDto.getPhone());
        manager.setAddress(managerDto.getAddress());
        manager.setNicNumber(managerDto.getNicNumber());
        manager.setUpdatedAt(LocalDateTime.now());
        manager.setStatus(ManagerStatus.PENDING); // require admin approval again

        // Update images if provided
        if (nicFront != null && !nicFront.isEmpty()) {
            manager.setNicFrontImage(storeFile(nicFront, "nicFront"));
        }
        if (nicBack != null && !nicBack.isEmpty()) {
            manager.setNicBackImage(storeFile(nicBack, "nicBack"));
        }
        if (profileImage != null && !profileImage.isEmpty()) {
            manager.setProfileImage(storeFile(profileImage, "profileImg"));
        }

        // Save and return updated manager as DTO
        Manager updated = managerRepo.save(manager);
        return modelMapper.map(updated, ManagerDto.class);
    }

    @Override
    public void softDeleteManager(Integer managerId) {
        // Fetch manager or throw exception
        Manager manager = managerRepo.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manager not found with id " + managerId
                ));

        // Soft delete: mark as DELETED and update timestamp
        manager.setStatus(ManagerStatus.DELETED);
        manager.setUpdatedAt(LocalDateTime.now());

        // Save changes
        managerRepo.save(manager);
    }

    @Override
    public ManagerDto restoreManagerProfile(Integer managerId) {
        // Fetch manager or throw exception
        Manager manager = managerRepo.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manager not found with id " + managerId
                ));

        // Ensure the manager is actually deleted
        if (manager.getStatus() != ManagerStatus.DELETED) {
            throw new IllegalStateException("Manager is not deleted and cannot be restored");
        }

        // Restore manager profile
        manager.setStatus(ManagerStatus.PENDING); // Or APPROVED if you want auto-approval
        manager.setUpdatedAt(LocalDateTime.now());

        // Save and return DTO
        return modelMapper.map(managerRepo.save(manager), ManagerDto.class);
    }


    @Override
    public ManagerDto getManagerById(Integer managerId) {
        Manager manager = managerRepo
                .findByManagerIdAndStatusNot(managerId, ManagerStatus.DELETED)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Manager not found with id " + managerId
                        )
                );

        return modelMapper.map(manager, ManagerDto.class);
    }

    @Override
    public List<ManagerDto> getAllManagers() {
        return managerRepo.findByStatusNot(ManagerStatus.DELETED)
                .stream()
                .map(manager -> modelMapper.map(manager, ManagerDto.class))
                .toList();
    }

    @Override
    public ManagerDto approveManagerProfile(Integer managerId) {
        // Fetch manager or throw exception if not found
        Manager manager = managerRepo.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manager not found with id " + managerId
                ));

        // Approve the manager
        manager.setStatus(ManagerStatus.APPROVED);
        manager.setUpdatedAt(LocalDateTime.now()); // Update timestamp

        // Save and return as DTO
        return modelMapper.map(managerRepo.save(manager), ManagerDto.class);
    }

    @Override
    public Resource downloadNICImagesAsZip(Integer managerId) {
        Manager manager = managerRepo.findByManagerIdAndStatusNot(managerId, ManagerStatus.DELETED)
                .orElseThrow(() -> new RuntimeException("Cannot download NIC images for a deleted manager"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            addFileToZip(manager.getNicFrontImage(), "NIC_Front.jpg", zos);
            addFileToZip(manager.getNicBackImage(), "NIC_Back.jpg", zos);

            zos.finish();
            return new ByteArrayResource(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Error creating ZIP", e);
        }
    }

    // ---------------- HELPERS ---------------- //
    private String storeFile(MultipartFile file, String folder) {
        try {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            Path dir = Paths.get(UPLOAD_DIR + folder);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            Path filePath = dir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    private void addFileToZip(String filePath, String zipEntryName, ZipOutputStream zos) throws IOException {
        if (filePath == null) return;
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return;

        zos.putNextEntry(new ZipEntry(zipEntryName));
        Files.copy(path, zos);
        zos.closeEntry();
    }

}
