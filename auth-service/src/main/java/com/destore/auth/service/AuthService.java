package com.destore.auth.service;

import com.destore.auth.model.dto.LoginRequest;
import com.destore.auth.model.dto.LoginResponse;
import com.destore.auth.model.dto.RegisterRequest;
import com.destore.auth.model.dto.UserResponse;
import com.destore.auth.model.entity.User;
import com.destore.auth.model.enums.UserRole;
import com.destore.auth.repository.UserRepository;
import com.destore.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @file AuthService.java
 * @brief Service class for authentication operations
 * 
 * Handles user login and JWT token generation
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * @brief Authenticate user and generate JWT token
     * @param loginRequest Login credentials
     * @return LoginResponse with JWT token and user info
     * @throws UsernameNotFoundException if user not found
     * @throws BadCredentialsException if password is incorrect
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        // Find active user
        User user = userRepository.findByUsernameAndIsActiveTrue(loginRequest.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found or inactive - {}", loginRequest.getUsername());
                    return new UsernameNotFoundException("Invalid username or password");
                });

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: incorrect password for user - {}", loginRequest.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name(),
                user.getStoreId()
        );

        log.info("Login successful for user: {} (role: {})", user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .storeId(user.getStoreId())
                .build();
    }

    /**
     * @brief Get current user information
     * @param username Username from JWT token
     * @return UserResponse with user details
     * @throws UsernameNotFoundException if user not found
     */
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .storeId(user.getStoreId())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * @brief Register a new user
     * @param registerRequest Registration details
     * @return UserResponse with user details (no token - must login after registration)
     * @throws IllegalArgumentException if username exists or validation fails
     */
    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {
        log.info("Registration attempt for username: {}", registerRequest.getUsername());

        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            log.warn("Registration failed: username already exists - {}", registerRequest.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        // Validate role
        UserRole role;
        try {
            role = UserRole.valueOf(registerRequest.getRole());
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: invalid role - {}", registerRequest.getRole());
            throw new IllegalArgumentException("Invalid role. Must be NETWORK_MANAGER or STORE_MANAGER");
        }

        // Validate role and store_id relationship
        if (role == UserRole.NETWORK_MANAGER && registerRequest.getStoreId() != null) {
            throw new IllegalArgumentException("Network managers cannot have a store ID");
        }
        if (role == UserRole.STORE_MANAGER && registerRequest.getStoreId() == null) {
            throw new IllegalArgumentException("Store managers must have a store ID");
        }

        //TODO: Call store-service to verify store exists

        // Hash password
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // Create user entity
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash(hashedPassword)
                .fullName(registerRequest.getFullName())
                .role(role)
                .storeId(registerRequest.getStoreId())
                .isActive(true)
                .build();

        // Save to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} (role: {})", savedUser.getUsername(), savedUser.getRole());

        return UserResponse.builder()
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .storeId(savedUser.getStoreId())
                .isActive(savedUser.getIsActive())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }
}
