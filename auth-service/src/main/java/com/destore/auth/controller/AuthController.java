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
 * REST controller for authentication endpoints.
 * <p>
 * Provides endpoints for login and user information retrieval.
 * </p>
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
     * Registers a new user.
     *
     * @param registerRequest the registration details
     * @return the {@link UserResponse} with user details
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account (manager)")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login endpoint.
     *
     * @param loginRequest the login credentials
     * @return the {@link LoginResponse} with JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets current user information.
     *
     * @param authentication the Spring Security authentication (contains username from JWT)
     * @return the {@link UserResponse} with user details
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get information about the currently authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserResponse response = authService.getCurrentUser(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     *
     * @return a simple health status
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the auth service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
