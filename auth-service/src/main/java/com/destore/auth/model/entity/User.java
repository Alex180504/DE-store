package com.destore.auth.model.entity;

import com.destore.auth.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a user in the auth service.
 * <p>
 * Users can have one of two roles:
 * <ul>
 * <li>NETWORK_MANAGER: Manages global pricing rules (store_id is null)</li>
 * <li>STORE_MANAGER: Manages pricing rules for a specific store (store_id is set)</li>
 * </ul>
 * Password storage uses BCrypt which automatically handles salt generation
 * and storage within the hash itself.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Unique user identifier (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    /**
     * Unique username for login.
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Email address.
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * BCrypt hashed password (includes embedded salt).
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Full name of the user.
     */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /**
     * User role (NETWORK_MANAGER or STORE_MANAGER).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    /**
     * Store ID for store managers (null for network managers).
     * This is a reference to a store in the pricing service.
     */
    @Column(name = "store_id")
    private Integer storeId;

    /**
     * Whether the user account is active.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Timestamp when the user was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
