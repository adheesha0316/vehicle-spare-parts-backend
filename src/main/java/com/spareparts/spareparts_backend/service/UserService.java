package com.spareparts.spareparts_backend.service;

import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.Role;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user); // Register partner, manager, customer

    User loginUser(String email, String password); // Basic login

    Optional<User> getUserById(Integer id);

    List<User> getAllUsers();

    // Admin approves or disapproves a user (partner or manager)
    User approveUser(Integer userId);

    User disapproveUser(Integer userId);

    // Change role (only admin)
    User changeUserRole(Integer userId, Role role);
}
