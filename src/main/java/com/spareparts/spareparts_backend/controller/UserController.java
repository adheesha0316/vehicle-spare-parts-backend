package com.spareparts.spareparts_backend.controller;

import com.spareparts.spareparts_backend.entity.User;
import com.spareparts.spareparts_backend.enums.Role;
import com.spareparts.spareparts_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    // ---------------- GET ALL USERS (ADMIN) ---------------- //
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // ---------------- GET USER BY ID ---------------- //
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------- APPROVE USER (ADMIN) ---------------- //
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> approveUser(@PathVariable Integer id) {
        User user = userService.approveUser(id);
        return ResponseEntity.ok(user);
    }

    // ---------------- DISAPPROVE USER (ADMIN) ---------------- //
    @PutMapping("/{id}/disapprove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> disapproveUser(@PathVariable Integer id) {
        User user = userService.disapproveUser(id);
        return ResponseEntity.ok(user);
    }

    // ---------------- CHANGE USER ROLE (ADMIN) ---------------- //
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> changeUserRole(@PathVariable Integer id, @RequestParam Role role) {
        User updatedUser = userService.changeUserRole(id, role);
        return ResponseEntity.ok(updatedUser);
    }
}
