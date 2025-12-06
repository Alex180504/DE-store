package com.destore.auth.controller;

import com.destore.auth.model.dto.LoginRequest;
import com.destore.auth.model.dto.LoginResponse;
import com.destore.auth.model.dto.RegisterRequest;
import com.destore.auth.model.dto.UserResponse;
import com.destore.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * @file AuthController.java
 * @brief REST controller for authentication endpoints
 * 
 * Provides endpoints for login and user information retrieval
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * @brief Register a new user
     * @param registerRequest Registration details
     * @return UserResponse with user details
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account (manager)")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * @brief Login endpoint
     * @param loginRequest Login credentials
     * @return LoginResponse with JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * @brief Get current user information
     * @param authentication Spring Security authentication (contains username from JWT)
     * @return UserResponse with user details
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get information about the currently authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserResponse response = authService.getCurrentUser(username);
        return ResponseEntity.ok(response);
    }

    /**
     * @brief Health check endpoint
     * @return Simple health status
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the auth service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
