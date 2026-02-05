package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UpdatePasswordRequest;
import com.example.demo.dto.VerifyPasswordRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;


import java.util.Map;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public AuthController(AuthService authService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Username already exists"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole(Role.ROLE_USER);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Register success"));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User not found");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid password");
        }

        return ResponseEntity.ok(user);
    }

    @PutMapping("/settings/password/verify")
    public ResponseEntity<?> verifyPassword(
            @RequestBody VerifyPasswordRequest request,
            Authentication auth) {

        User user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Wrong password"));
        }

        return ResponseEntity.ok(Map.of("verified", true));
    }


    @PutMapping("/settings/password")
    public ResponseEntity<?> changePassword(
            @RequestBody UpdatePasswordRequest request,
            Authentication auth) {

        User user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow();

        // 1. เช็ครหัสเดิมก่อน (กันยิงตรง)
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Wrong current password"));
        }

        // 2. ตั้งรหัสใหม่
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Password updated")
        );
    }


    // UPGRADE ROLE
    @PutMapping("/settings/upgrade")
    public ResponseEntity<?> upgrade(Authentication auth) {

        User user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow();

        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Upgraded to ADMIN"));
    }

}