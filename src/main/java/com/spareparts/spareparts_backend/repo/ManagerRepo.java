package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerRepo extends JpaRepository<Manager,Integer> {
    // Find manager by linked user
    Optional<Manager> findByUserUserId(Integer userId);

}
