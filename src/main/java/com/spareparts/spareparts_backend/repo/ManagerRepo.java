package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Manager;
import com.spareparts.spareparts_backend.enums.ManagerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepo extends JpaRepository<Manager,Integer> {
    // 🔹 Find manager by linked user (exclude deleted)
    Optional<Manager> findByUserUserIdAndStatusNot(Integer userId, ManagerStatus status);

    // 🔹 Get all non-deleted managers
    List<Manager> findByStatusNot(ManagerStatus status);

    // 🔹 Get manager by id (exclude deleted)
    Optional<Manager> findByManagerIdAndStatusNot(Integer managerId, ManagerStatus status);

}
