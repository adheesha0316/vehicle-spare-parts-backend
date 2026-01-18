package com.spareparts.spareparts_backend.repo;

import com.spareparts.spareparts_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    // For Authentication and Profile fetching
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // ================= PERFECT ADDITIONS =================

    /**
     * Essential for Registration logic.
     * Prevents Duplicate Account Creation.
     */
    boolean existsByEmail(String email);

    /**
     * Useful for checking username availability
     * if you allow custom usernames.
     */
    boolean existsByUsername(String username);
}
