package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Manager;
import com.spareparts.spareparts_backend.enums.ManagerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepo extends JpaRepository<Manager,Integer> {
    Optional<Manager> findByUserUserId(Integer userId);

    // Fetch all managers except soft-deleted ones
    List<Manager> findByStatusNot(ManagerStatus status);

}
